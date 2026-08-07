# Modernize Code and Resolve Deprecations

Update the codebase to use modern Android APIs, replacing deprecated flags and methods, and addressing common linter warnings to improve code quality and maintainability.

## User Review Required

> [!IMPORTANT]
> The plan includes renaming several variables and functions to follow Kotlin naming conventions (camelCase instead of snake_case with underscores). This improves readability and aligns with modern standards.

## Proposed Changes

### UI and Core Logic

#### [MODIFY] [MainActivity.kt](file:///C:/git_repo/HundredEightV/app/src/main/java/com/tkprof/hundredeightv/MainActivity.kt)
- Replace `Math.round(val * 100.0) / 100.0` with `kotlin.math.round`.
- Update `SharedPreferences` usage to use KTX `edit { ... }` consistently.
- Replace `getApplicationContext()` with `applicationContext` property.
- Rename functions and variables that use underscores to camelCase (e.g., `f_Start` -> `startVows`, `ct_remain` -> `countdownRemaining`).
- Fix `getIdentifier` usage or suppress with a comment if it's dynamic by design.

#### [MODIFY] [SettingsActivity.kt](file:///C:/git_repo/HundredEightV/app/src/main/java/com/tkprof/hundredeightv/SettingsActivity.kt)
- Replace deprecated `setTargetFragment` with modern fragment communication if possible, or use `parentFragmentManager` correctly.
- Clean up `@Suppress("DEPRECATION")` where possible.

#### [MODIFY] [StartActivity.kt](file:///C:/git_repo/HundredEightV/app/src/main/java/com/tkprof/hundredeightv/StartActivity.kt)
- Rename variables and functions to camelCase.
- Use `edit { ... }` consistently.

---

### Tests

#### [MODIFY] [SettingsActivityTest.kt](file:///C:/git_repo/HundredEightV/app/src/androidTest/java/com/tkprof/hundredeightv/SettingsActivityTest.kt)
- Replace deprecated `FLAG_TURN_SCREEN_ON` and `FLAG_DISMISS_KEYGUARD` with `Activity.setTurnScreenOn(true)` and `KeyguardManager.requestDismissKeyguard()`.

#### [MODIFY] [MainActivityTest.kt](file:///C:/git_repo/HundredEightV/app/src/androidTest/java/com/tkprof/hundredeightv/MainActivityTest.kt)
- Apply similar test modernization for window flags.

## Verification Plan

### Automated Tests
- Run all instrumented tests:
  `./gradlew :app:connectedDebugAndroidTest`
- Run lint to verify warnings reduction:
  `./gradlew :app:lintDebug`

### Manual Verification
- Deploy to a device and verify:
  - Vow countdown and audio.
  - Settings changes (TTS voice, interval).
  - Screen remains on during active vow session.
