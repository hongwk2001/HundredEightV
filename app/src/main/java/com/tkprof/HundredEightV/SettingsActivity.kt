package com.tkprof.HundredEightV

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.util.Objects

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Apply window insets to prevent the UI from being covered by system bars
        val mainView = findViewById<View>(R.id.settings_container)?.parent as? View
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Load Preference fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, GeneralPreferenceFragment())
                .commit()
        }

        // Close Button Setup
        findViewById<Button>(R.id.btn_close_settings).setOnClickListener {
            finish()
        }

        // Initialize AdMob Banner
        MobileAds.initialize(this) {}
        val adView: AdView = findViewById(R.id.adViewSettings)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_general, rootKey)

            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("count_a")))
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("count_b")))
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("interval")))
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("file_name")))
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("bellsound")))
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("bgcolor")))

            // Set version name dynamically
            val versionPreference: Preference? = findPreference("version_info")
            versionPreference?.summary = "version: " + getAppVersionName(requireContext())
        }

        private fun getAppVersionName(context: Context): String {
            return try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                packageInfo.versionName ?: "N/A"
            } catch (e: Exception) {
                e.printStackTrace()
                "N/A"
            }
        }
    }

    companion object {

        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference, value ->
                val stringValue = value.toString()
                if (preference is ListPreference) {
                    val index = preference.findIndexOfValue(stringValue)
                    preference.summary =
                        if (index >= 0) preference.entries[index] else null
                } else {
                    preference.summary = stringValue
                }
                true
            }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "")
            )
        }
    }
}
