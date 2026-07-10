# AGENTS.md

## Project structure

- **3 modules:** `:shared` (KMP source), `:androidApp`, `:desktopApp`
- All shared code lives in `shared/src/`. Target folders: `commonMain`, `androidMain`, `iosMain`, `jvmMain`
- iOS app entry: `iosApp/iosApp/` (opened in Xcode, never run via Gradle)
- Gradle version: **9.1.0** (wrapper present). Use `./gradlew` — never system `gradle`

## Key commands

- Build debug APK: `./gradlew :androidApp:assembleDebug`
- Desktop run: `./gradlew :desktopApp:run` (or `:desktopApp:hotRun --auto` for hot reload)
- iOS: open `iosApp/iosApp/iosApp.xcodeproj` in Xcode
- Tests: `./gradlew :shared:jvmTest` (desktop), `./gradlew :shared:testAndroidHostTest` (Android)
- `:shared:iosSimulatorArm64Test` for iOS simulator tests

## Gotchas

- **No unit tests exist yet** — the shared module has no `test/` source sets. Adding tests requires creating `testAndroidHostTest` or `jvmTest` source directories and corresponding build config.
- iOS target is `iosSimulatorArm64` only — no physical device target configured in `build.gradle.kts`.
- The project uses foojay-resolver plugin for toolchain auto-provisioning (no manual JDK install needed).
- Compose Multiplatform 1.7.x (alpha) — API surface differs from stable Compose for Android. Check `libs.versions.toml` for exact versions.
- Don't edit `iosApp/` Gradle files — iOS build is managed entirely by Xcode project.
