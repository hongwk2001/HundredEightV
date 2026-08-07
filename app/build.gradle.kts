plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tkprof.hundredeightv"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tkprof.hundredeightv"
        minSdk = 27
        targetSdk = 37
        versionCode = 22
        versionName = "07/31/2026"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
        resValues = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.appcompat.resources)
    implementation(libs.material)
    implementation(libs.androidx.preference)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Google Ads & Media
    implementation(libs.play.services.ads)
    implementation(libs.interactivemedia)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
