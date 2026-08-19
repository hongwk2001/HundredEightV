package com.tkprof.HundredEightV

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.tkprof.HundredEightV.R
import java.util.Locale

class StartActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var spinner: Spinner
    private var adView: AdView? = null
    private var intervalSec: Double = 9.4

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        findViewById<View>(R.id.start_root_layout)?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        adView = findViewById(R.id.adViewStart)
        adView?.post {
            val adRequest = AdRequest.Builder().build()
            adView?.loadAd(adRequest)
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        Util.migratePreferences(this)
        spinner = findViewById(R.id.spinner_vow_file)
 
        val values = resources.getStringArray(R.array.file_choice_values)
        val displayValues = values.map { it.replace(".json", "") }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set spinner selection to current saved value
        val currentFile = sharedPref.getString("file_name", null)
        val index = values.indexOf(currentFile)
        if (index >= 0) {
            spinner.setSelection(index)
        }

        updateIntervalDisplay()

        findViewById<Button>(R.id.btn_start_faster).setOnClickListener {
            changeInterval(-0.05)
        }

        findViewById<Button>(R.id.btn_start_slower).setOnClickListener {
            changeInterval(0.05)
        }

        findViewById<Button>(R.id.btn_minus_1).setOnClickListener {
            changeInterval(-1.0)
        }

        findViewById<Button>(R.id.btn_plus_1).setOnClickListener {
            changeInterval(1.0)
        }

        findViewById<Button>(R.id.btn_begin).setOnClickListener {
            val selectedIndex = spinner.selectedItemPosition
            val selectedValue = values[selectedIndex]
            
            sharedPref.edit { putString("file_name", selectedValue) }
            
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("AUTO_START", true)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_settings_start_btn).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_exit).setOnClickListener {
            finishAffinity()
        }
    }

    private fun changeInterval(delta: Double) {
        intervalSec += delta
        if (intervalSec < 1.0) intervalSec = 1.0
        intervalSec = kotlin.math.round(intervalSec * 100.0) / 100.0
        saveInterval()
        updateIntervalDisplayOnly()
    }

    private fun updateIntervalDisplay() {
        intervalSec = sharedPref.getString("interval", "9.4")?.toDoubleOrNull() ?: 9.4
        updateIntervalDisplayOnly()
    }

    private fun updateIntervalDisplayOnly() {
        findViewById<TextView>(R.id.tv_interval_value).text = String.format(Locale.US, "%.2f s", intervalSec)
    }

    private fun saveInterval() {
        sharedPref.edit { putString("interval", intervalSec.toString()) }
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
        updateIntervalDisplay()
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
