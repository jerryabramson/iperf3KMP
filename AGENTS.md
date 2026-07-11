# AGENTS.md

## Project structure

- **3 modules:** `:shared` (KMP library), `:androidApp` (Android app), `:desktopApp` (JVM desktop app)
- Shared code in `shared/src/`. Source sets: `commonMain`, `commonTest`, `androidMain`, `androidHostTest`, `iosMain`, `iosTest`, `jvmMain`, `jvmTest`
- iOS app entry: `iosApp/iosApp.xcodeproj` (open in Xcode, never build via Gradle)
- Gradle **9.6.1** (wrapper present). Use `./gradlew` — never system `gradle`
- Root project name: `iperf3KMP` (`settings.gradle.kts`)

## Key commands

- Desktop run: `./gradlew :desktopApp:run`
- Desktop hot reload: `./gradlew :desktopApp:hotRun --auto`
- Build debug APK: `./gradlew :androidApp:assembleDebug`
- Desktop tests: `./gradlew :shared:jvmTest`
- Android host tests: `./gradlew :shared:testAndroidHostTest`
- iOS simulator tests: `./gradlew :shared:iosSimulatorArm64Test`

## Gotchas

- Desktop main class: `edu.bu.cs683_jabramson_project.iperf3_network_tester.MainKt` (`desktopApp/build.gradle.kts:20`)
- Shared module targets **both** `iosArm64()` and `iosSimulatorArm64()` (`shared/build.gradle.kts:12-13`)
- Compose Multiplatform **1.11.1** (check `gradle/libs.versions.toml` for exact versions)
- foojay-resolver plugin auto-provisions JDK (no manual install needed)
- Don't edit `iosApp/` Gradle files — iOS build is managed entirely by Xcode
- Android minSdk is **26**, compileSdk/targetSdk is **36**
