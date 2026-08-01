package com.tkprof.HundredEightV

import android.app.Application
import com.google.android.gms.ads.MobileAds

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
    }
}
