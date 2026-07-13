import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "edu.bu.cs683_jabramson_project.iperf3_network_tester.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "edu.bu.cs683_jabramson_project.iperf3_network_tester"
            packageVersion = "1.0.0"

            macOS {
                iconFile.set(project.file("src/main/resources/app-icon.png"))
            }
            windows {
                iconFile.set(project.file("src/main/resources/app-icon.png"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/app-icon.png"))
            }
        }
    }
}