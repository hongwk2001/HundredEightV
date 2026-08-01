package com.tkprof.hundredeightv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.tkprof.hundredeightv.R

class EndActivity : AppCompatActivity() {

    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        findViewById<View>(R.id.end_root_layout)?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        adView = findViewById(R.id.adViewEnd)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)

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
        }

        findViewById<Button>(R.id.btn_settings_end_btn).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Handle back press to exit the app completely
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }
}
