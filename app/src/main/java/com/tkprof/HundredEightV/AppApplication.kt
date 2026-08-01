package com.tkprof.hundredeightv

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "onCreate called")
        MobileAds.initialize(this) {
            Log.d("AppApplication", "MobileAds initialized")
        }
    }
}
