import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Desktop JVM classifier for the native_format JNI library, mirrored at
// runtime by NativeFormatterBridge.kt (jvmMain) so it can locate the
// matching bundled resource.
val hostOsName: String = System.getProperty("os.name").lowercase().let {
    when {
        it.contains("mac") -> "macos"
        it.contains("win") -> "windows"
        else -> "linux"
    }
}
val hostArchName: String = System.getProperty("os.arch").lowercase().let {
    when (it) {
        "aarch64", "arm64" -> "aarch64"
        else -> "x86_64"
    }
}
val nativeFormatLibFileName: String = when (hostOsName) {
    "macos" -> "libnativeformat.dylib"
    "windows" -> "nativeformat.dll"
    else -> "libnativeformat.so"
}
val nativeFormatResourceDir = layout.buildDirectory.dir("generated/nativeformatResources")

val compileNativeFormatJvm = tasks.register<Exec>("compileNativeFormatJvm") {
    val nativeSrcDir = rootProject.layout.projectDirectory.dir("native")
    val jniSource = nativeSrcDir.file("native_format_jni.c")
    val javaHome = System.getProperty("java.home")
    val jniPlatformDir = when (hostOsName) {
        "macos" -> "darwin"
        "windows" -> "win32"
        else -> "linux"
    }
    val outDir = nativeFormatResourceDir.map { it.dir("nativeformat/$hostOsName-$hostArchName") }
    val outFile = outDir.map { it.file(nativeFormatLibFileName) }

    inputs.file(jniSource)
    inputs.file(nativeSrcDir.file("native_format.h"))
    outputs.file(outFile)

    doFirst { outDir.get().asFile.mkdirs() }

    commandLine(
        "clang",
        "-Wall", "-Wextra",
        if (hostOsName == "macos") "-dynamiclib" else "-shared",
        "-fPIC",
        "-I", nativeSrcDir.asFile.absolutePath,
        "-I", "$javaHome/include",
        "-I", "$javaHome/include/$jniPlatformDir",
        "-o", outFile.get().asFile.absolutePath,
        jniSource.asFile.absolutePath
    )
}

tasks.matching { it.name == "processJvmMainResources" || it.name == "jvmProcessResources" }.configureEach {
    dependsOn(compileNativeFormatJvm)
}

// StringFormatterTest calls the real native_format JNI library, which
// System.loadLibrary() can only resolve on an actual Android
// device/emulator (via the packaged .so) — androidHostTest runs on a plain
// Robolectric JVM host with no such library available, so it can't pass
// here regardless of correctness. Real coverage for this path comes from
// jvmTest (JNI, host-compiled) and iosSimulatorArm64Test (cinterop), both
// of which exercise the same native_format.h logic end to end.
tasks.matching { it.name.contains("AndroidHostTest", ignoreCase = false) }.configureEach {
    if (this is Test) {
        filter {
            excludeTestsMatching("edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.StringFormatterTest")
            isFailOnNoMatchingTests = false
        }
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
        iosTarget.compilations.getByName("main") {
            cinterops {
                create("nativeFormat") {
                    defFile(project.file("src/nativeInterop/cinterop/native_format.def"))
                    compilerOpts("-I${rootProject.layout.projectDirectory.dir("native").asFile.absolutePath}")
                }
            }
        }
    }

    jvm()

    androidLibrary {
        namespace = "edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.shared"
        compileSdk = 36 //libs.versions.android.compileSdk.get().toInt()
        minSdk = 26 //libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        getByName("jvmMain") { resources.srcDir(nativeFormatResourceDir) }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(project(":nativeformat"))
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}