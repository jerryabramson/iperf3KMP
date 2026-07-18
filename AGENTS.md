# AGENTS.md

## Project structure

- **4 modules:** `:shared` (KMP library), `:androidApp` (Android app), `:desktopApp` (JVM desktop app), `:nativeformat` (standalone Android library, CMake/NDK build for JNI native formatting — see below)
- Shared code in `shared/src/`. Source sets: `commonMain`, `commonTest`, `androidMain`, `androidHostTest`, `iosMain`, `iosTest`, `jvmMain`, `jvmTest`
- `native/` (repo root): single source of truth C code for printf-style formatting (`native_format.h`, `native_format_jni.c`), reached via JNI from Android/desktop and cinterop from iOS — see `NATIVE_FORMATTING_JUSTIFICATION.md`
- iOS app entry: `iosApp/iosApp.xcodeproj` (open in Xcode, never build via Gradle)
- Gradle **9.6.1** (wrapper present). Use `./gradlew` — never system `gradle`
- Root project name: `iperf3KMP` (`settings.gradle.kts`)
- Kotlin **2.4.0**, Compose Multiplatform **1.11.1**, AGP **9.0.1**

## Key commands

- Desktop run: `./gradlew :desktopApp:run`
- Desktop hot reload: `./gradlew :desktopApp:hotRun --auto`
- Desktop tests: `./gradlew :shared:jvmTest`
- Build debug APK: `./gradlew :androidApp:assembleDebug`
- Install debug APK: `./gradlew :androidApp:installDebug`
- Android host tests: `./gradlew :shared:testAndroidHostTest` (excludes `StringFormatterTest` — real JNI can't load on a Robolectric host; see `NATIVE_FORMATTING_JUSTIFICATION.md`)
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run from there
- iOS simulator tests: `./gradlew :shared:iosSimulatorArm64Test`
- Test sources: `shared/src/commonTest/`, `shared/src/androidHostTest/`, `shared/src/jvmTest/`, `shared/src/iosTest/`

## Gotchas

- Desktop main class: `edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.MainKt` (`desktopApp/build.gradle.kts:20`)
- Shared module targets **both** `iosArm64()` and `iosSimulatorArm64()` (`shared/build.gradle.kts:12-13`)
- foojay-resolver plugin auto-provisions JDK (no manual install needed)
- Don't edit `iosApp/` Gradle files — iOS build is managed entirely by Xcode
- Android minSdk **26**, compileSdk/targetSdk **36**
- `shared/build.gradle.kts` uses `androidRuntimeClasspath(libs.compose.uiTooling)` for Android debug tooling
- No lint/typecheck commands configured — none needed for this project
- No other instruction files exist (no CLAUDE.md, .cursorrules, copilot-instructions.md, opencode.json)

## iOS simulator console errors

When running the iOS app on a simulator, you may see repeated haptic feedback errors in the console:

```
[UIKBFeedbackGenerator] <_UIKBFeedbackGenerator: 0x...>: Error creating CHHapticPattern:
Error Domain=NSCocoaErrorDomain Code=260 "The file "hapticpatternlibrary.plist" couldn't be
opened because there is no such file."
```

These are **harmless system-level errors** from iOS's keyboard haptic feedback engine. They won't affect functionality — you can safely ignore them.

## Application icon

Icons deployed to all three targets from `ic_launcher-playstore.png` (512×512).

| Target | Icon file |
|--------|-----------|
| Android | `androidApp/src/main/res/mipmap-{density}/ic_launcher*.webp` + `mipmap-anydpi-v26/ic_launcher*.xml` + `values/ic_launcher_background.xml` (#0F1E31 navy) |
| iOS | `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-512.png` |
| Desktop | `desktopApp/src/main/resources/app-icon.png` |

**Note:** 512×512 not 1024×1024. iOS will upscale at runtime; may appear slightly soft on Retina displays.

## Architecture notes

- ViewModel: `shared/src/commonMain/.../viewmodel/Iperf3RunViewModel.kt` — extends `ViewModel`, uses `StateFlow` for UI state
- View: `shared/src/commonMain/.../view/Iperf3View.kt` — Compose UI composable
- Simulated runner: `shared/src/commonMain/.../runner/SimulatedRun.kt` — mock iperf3 execution (not real iperf3 binary)
- Unit conversion: `shared/src/commonMain/.../utils/UnitConverter.kt`
- Theme: `shared/src/commonMain/.../ui/theme/` — Color, Type, Theme, mesloFontFamily
