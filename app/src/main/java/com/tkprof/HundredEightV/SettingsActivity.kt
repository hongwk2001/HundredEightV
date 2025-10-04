
package com.tkprof.HundredEightV

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Objects

class SettingsActivity : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null
    private val adUnitId = AD_UNIT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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

        // Initialize AdMob & load ad
        MobileAds.initialize(this) {}
        loadInterstitialAd()
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
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
            mInterstitialAd = null
        }
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        showAdOrFinish()
        return true
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        showAdOrFinish()

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

        private fun isXLargeTablet(context: Context): Boolean {
            return (context.resources.configuration.screenLayout
                    and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }
}

