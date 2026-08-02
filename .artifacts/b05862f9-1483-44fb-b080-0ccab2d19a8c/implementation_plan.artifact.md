# Fix "Unable to instantiate application" (ClassNotFoundException)

The application fails to start because `com.tkprof.hundredeightv.AppApplication` cannot be found at runtime. This is likely because the Kotlin Android plugin is not correctly applied to the project, preventing Kotlin files from being compiled into the DEX.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/git_repo/HundredEightV/gradle/libs.versions.toml)
- Add `kotlin-android` plugin definition to the `[plugins]` section.

#### [MODIFY] [build.gradle.kts (root)](file:///C:/git_repo/HundredEightV/build.gradle.kts)
- Add `kotlin-android` plugin to the root `plugins` block with `apply false`.

#### [MODIFY] [build.gradle.kts (:app)](file:///C:/git_repo/HundredEightV/app/build.gradle.kts)
- Apply the `kotlin-android` plugin in the `plugins` block.

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure the project builds correctly.
- Verify that `AppApplication.kt` is compiled and included in the APK (can be checked via `Analyze APK` in Android Studio or by running the app).

### Manual Verification
- Deploy the app to a device/emulator and verify it no longer crashes on startup.
