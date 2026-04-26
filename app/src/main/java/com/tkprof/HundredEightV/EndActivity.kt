package com.tkprof.HundredEightV

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlin.system.exitProcess

class EndActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        MobileAds.initialize(this) {}
        val adView: AdView = findViewById(R.id.adViewEnd)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        
        // Use the passed extra for today's count
        val doneToday = intent.getIntExtra("DONE_TODAY", 0)
        val overall = sharedPref.getString("count_b", "0")

        findViewById<TextView>(R.id.tv_done_today).text = getString(R.string.done_today) + doneToday
        findViewById<TextView>(R.id.tv_overall).text = getString(R.string.overall) + overall

        findViewById<Button>(R.id.btn_restart).setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btn_exit).setOnClickListener {
            finishAffinity()
            exitProcess(0)
        }

        findViewById<Button>(R.id.btn_settings_end_btn).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
