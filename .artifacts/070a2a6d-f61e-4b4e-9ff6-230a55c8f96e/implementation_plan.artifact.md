# Fix: Failed to load asset path base.apk

The error `java.io.IOException: Failed to load asset path /data/app/.../base.apk` with `ApkAssets.nativeLoad` is a low-level resource loading failure. It typically occurs when the Android `ResourcesManager` loses its handle on the APK file, often due to a corrupted incremental deployment (Apply Changes) or a mismatch in the application's package state on the device.

## User Review Required

> [!IMPORTANT]
> This issue is primarily related to the environment and deployment process. While we can make the code more robust, the **Manual Verification** steps are crucial to fully resolve it.

## Proposed Changes

We will implement several defensive measures to ensure the app's resource system is correctly initialized and can handle modern split APK installations (which is how Android Studio often deploys apps).

### [app]

#### [MODIFY] [AppApplication.kt](file:///C:/git_repo/HundredEightV/app/src/main/java/com/tkprof/HundredEightV/AppApplication.kt)
- Add `SplitCompat.install(this)` to support split APK loading, which is common in modern Android deployments and can mitigate path resolution issues.
- Ensure `MobileAds` is initialized safely.

#### [MODIFY] [MainActivity.kt](file:///C:/git_repo/HundredEightV/app/src/main/java/com/tkprof/HundredEightV/MainActivity.kt)
- Adjust the order of `enableEdgeToEdge()` and `super.onCreate()` to ensure the context is fully ready before resource-intensive UI operations start.

#### [MODIFY] [build.gradle.kts](file:///C:/git_repo/HundredEightV/app/build.gradle.kts)
- Add the necessary Play Core library for `SplitCompat` support.

---

## Verification Plan

### Automated Tests
- Run a clean build to ensure no regression in dependency resolution.
- `gradlew clean assembleDebug`

### Manual Verification
1. **Full Reinstall**:
   - Uninstall the app from the device.
   - In Android Studio, go to `Build` -> `Clean Project`.
   - Then `Build` -> `Rebuild Project`.
   - Run the app using the standard **Run** button (Shift+F10), NOT "Apply Changes".
2. **Disable Hot Swap**:
   - If the issue persists, go to `Settings` -> `Deployment` -> `Apply Changes` and uncheck "Enable hot swap of code and resources".
3. **Verify App Start**:
   - Ensure the app starts without the `IOException` and that resources (like sounds in `Util.kt`) load correctly.
