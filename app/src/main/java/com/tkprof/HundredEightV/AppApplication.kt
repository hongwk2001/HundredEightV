package com.tkprof.HundredEightV

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.splitcompat.SplitCompat

class AppApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "onCreate called")
        MobileAds.initialize(this) {
            Log.d("AppApplication", "MobileAds initialized")
        }
    }
}
