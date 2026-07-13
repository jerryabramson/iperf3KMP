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
- Desktop tests: `./gradlew :shared:jvmTest`
- Build debug APK: `./gradlew :androidApp:assembleDebug`
- Install debug APK: `./gradlew :androidApp:installDebug`
- Android host tests: `./gradlew :shared:testAndroidHostTest`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run from there, or use the `iosApp` IntelliJ run configuration
- iOS simulator tests: `./gradlew :shared:iosSimulatorArm64Test`
- Test sources: `shared/src/commonTest/`, `shared/src/androidHostTest/`, `shared/src/jvmTest/`, `shared/src/iosTest/`

## Gotchas

- Desktop main class: `iperf3_network_tester_kmp.MainKt` (`desktopApp/build.gradle.kts:20`)
- Shared module targets **both** `iosArm64()` and `iosSimulatorArm64()` (`shared/build.gradle.kts:12-13`)
- Compose Multiplatform **1.11.1** (check `gradle/libs.versions.toml` for exact versions)
- foojay-resolver plugin auto-provisions JDK (no manual install needed)
- Don't edit `iosApp/` Gradle files — iOS build is managed entirely by Xcode
- Android minSdk is **26**, compileSdk/targetSdk is **36**

## iOS simulator console errors

When running the iOS app on a simulator, you may see repeated haptic feedback errors in the console:

```
[UIKBFeedbackGenerator] <_UIKBFeedbackGenerator: 0x...>: Error creating CHHapticPattern:
Error Domain=NSCocoaErrorDomain Code=260 "The file "hapticpatternlibrary.plist" couldn't be
opened because there is no such file."
```

These are **harmless system-level errors** from iOS's keyboard haptic feedback engine trying to load a missing haptic pattern library. They are a known Apple simulator issue that also appears in vanilla Xcode projects. They won't affect your app's functionality or icon display — you can safely ignore them.

## Application icon specification

Icons were sourced from a legacy project at `/Users/jerry/git_storage/iperf3Android/Code/Iperf3NetworkTester` and deployed to all three platform targets.

### Source assets

| Asset | Path (relative to `app/src/main/`) | Purpose |
|-------|--------------------------------------|---------|
| Adaptive foreground bitmaps | `res/mipmap-{mdpi..xxxhdpi}/ic_launcher_foreground.webp` | Density-specific foreground for adaptive icon |
| Legacy launcher bitmaps | `res/mipmap-{mdpi..xxxhdpi}/ic_launcher.webp` | Pre-API-26 fallback icon |
| Legacy round bitmaps | `res/mipmap-{mdpi..xxxhdpi}/ic_launcher_round.webp` | Pre-API-26 fallback round icon |
| Adaptive icon XMLs | `res/mipmap-anydpi-v26/ic_launcher.xml`, `ic_launcher_round.xml` | Compose adaptive icon from color + foreground |
| Background color | `res/values/ic_launcher_background.xml` | Solid `#0F1E31` (dark navy) background |
| Play Store PNG | `ic_launcher-playstore.png` | 512×512 high-res PNG for store listing and iOS/Desktop |

### Android (`androidApp/src/main/res/`)

**Mipmap bitmaps** — copied webp files into 5 density buckets:
- `mipmap-mdpi/` — ic_launcher.webp, ic_launcher_round.webp, ic_launcher_foreground.webp
- `mipmap-hdpi/` — same 3 files (1.5×)
- `mipmap-xhdpi/` — same 3 files (2×)
- `mipmap-xxhdpi/` — same 3 files (3×)
- `mipmap-xxxhdpi/` — same 3 files (4×)

**Adaptive icon definitions** — copied to `mipmap-anydpi-v26/`:
- `ic_launcher.xml` — references `@color/ic_launcher_background` (background) + `@mipmap/ic_launcher_foreground` (foreground + monochrome)
- `ic_launcher_round.xml` — identical to `ic_launcher.xml`

**Background color** — copied to `values/`:
- `ic_launcher_background.xml` — defines `<color name="ic_launcher_background">#0F1E31</color>`

**Removed:**
- `drawable/ic_launcher_background.xml` — old default green grid vector
- `drawable-v24/ic_launcher_foreground.xml` — old default robot head vector
- `drawable/` and `drawable-v24/` directories (emptied then deleted)
- `mipmap-{density}/ic_launcher.png` and `ic_launcher_round.png` — old PNG bitmaps replaced by webp

**Resolution chain:**
1. API 26+: `mipmap-anydpi-v26/ic_launcher.xml` composes adaptive icon from solid color background + density-specific foreground webp bitmaps
2. Pre-API 26: falls back to `mipmap-{density}/ic_launcher.webp` or `ic_launcher_round.webp`

### iOS (`iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`)

- Copied `ic_launcher-playstore.png` (512×512, 106 KB) as `app-icon-512.png`
- Updated `Contents.json` to reference `app-icon-512.png` instead of the old `app-icon-1024.png`
- Removed old `app-icon-1024.png`
- Icon is defined as universal idiom with three appearance slots (default, dark, tinted) — all point to the same file; dark and tinted slots have no explicit image set

**Note:** The icon is 512×512, not the ideal 1024×1024. iOS will upscale at runtime; may appear slightly soft on Retina displays. Replace with a 1024×1024 source when available.

### Desktop (`desktopApp/`)

- Copied `ic_launcher-playstore.png` (512×512) as `src/main/resources/app-icon.png`
- Configured `desktopApp/build.gradle.kts` `nativeDistributions` block with `iconFile.set(project.file("src/main/resources/app-icon.png"))` for macOS, Windows, and Linux

### Updating icons in the future

To refresh icons from the source project or a new source:
1. Replace the webp files in `androidApp/src/main/res/mipmap-{density}/` for all 5 density buckets
2. Replace `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` if the adaptive icon structure changes
3. Update `androidApp/src/main/res/values/ic_launcher_background.xml` if the background color changes
4. Replace `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-512.png` and update `Contents.json` filename field if renamed
5. Replace `desktopApp/src/main/resources/app-icon.png` — no config change needed unless the filename changes

## stubbedexec branch changes

### Iperf3RunViewModel (`shared/src/commonMain/.../viewmodel/Iperf3RunViewModel.kt`)

- Class now extends `ViewModel` (was a plain class)
- Added `myInt(s: String, o: Int): Int` — returns parsed int or fallback default `o`
- `runIperf3()` stubbed with simulated test loop:
  - 18 iterations, delays of 400ms (first 4) and 1s (remaining)
  - Mock `iperf3Messages` and `outputLines` from `getSampleUiState()`
  - Progress increments by 0.1 per iteration
  - Return code = `p * 10` rounded to int
- `cancel()` refactored to call `completeTest()` instead of inline state reset
- Renamed methods:
  - `toggleUploadDownload()` → `setUploadDownload()`
  - `updateHostName()` → `seHostName()` (note: typo preserved)
- Updated `setPortNumber()`, `setDuration()`, `setSkip()`, `setParallelStreams()` to use `myInt()` helper
- Added `setContext(context: String)` — called at start of composable view
- Added `toggleDebug()` — toggles debug mode via "show iperf3 output" button

### Iperf3View (`shared/src/commonMain/.../view/Iperf3View.kt`)

- Updated callback references to match renamed viewmodel methods
- Removed unused `viewModel` import

### UnitConverter (`shared/src/commonMain/.../utils/UnitConverter.kt`)

- Fixed string interpolation in `toIntString()` — `${unitConvertedData.value.toInt()}` instead of `$unitConvertedData.value.toInt())`
- Fixed string interpolation in `toWholeNumber()` — `${unitConvertedData.value} ${unitConvertedData.unit}`
- Reformatted `fromHumanUnit()` — moved `rawBitsPerSec` into `when` block body, added early return
- Reformatted `truncateDouble()` — moved inner `if/else` body into block
