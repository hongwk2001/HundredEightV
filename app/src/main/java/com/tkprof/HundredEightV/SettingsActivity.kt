package com.tkprof.HundredEightV

import android.content.Context
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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Objects

class SettingsActivity : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    private val adUnitId = AD_UNIT_ID

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
            showAdOrFinish()
        }

        // Initialize AdMob & load ad
        MobileAds.initialize(this) {}
        loadInterstitialAd()

        // Handle back press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showAdOrFinish()
            }
        })
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                mInterstitialAd = null
            }
        })
    }

    private fun showAdOrFinish() {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    //mInterstitialAd = null
                    finish()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when ad fails to show.
                    //mInterstitialAd = null
                    finish()
                }
            }
            ad.show(this)
        } else {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        showAdOrFinish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                showAdOrFinish()
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
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("file_line_cnt")))
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("bellsound")))
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("bgcolor")))

            // Set version name dynamically
            val versionPreference: Preference? = findPreference("version_info")
            versionPreference?.summary = "version: " + getAppVersionName(requireContext())
        }

        private fun getAppVersionName(context: Context): String {
            try {
                val packageInfo = context.packageName.let { context.packageManager.getPackageInfo(it, 0) }
                return packageInfo.versionName ?: "N/A"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "N/A"
        }
    }

    companion object {

        private const val AD_UNIT_ID = "ca-app-pub-8979756439452342/7964602504"
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
