package com.tkprof.hundredeightv

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.tkprof.hundredeightv.R

class SettingsActivity : AppCompatActivity() {

    private var adView: AdView? = null

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
        adView = findViewById(R.id.adViewSettings)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat(), VoicePreferenceDialogFragmentCompat.VoiceSamplePlayer {
        private var tts: TextToSpeech? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_general, rootKey)

            // Set version name dynamically
            findPreference<Preference>("version_info")?.summary = "version: ${getAppVersionName(requireContext())}"

            // Handle About page click
            findPreference<Preference>("about_page")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), AboutActivity::class.java)
                startActivity(intent)
                true
            }

            findPreference<ListPreference>("tts_lang")?.setOnPreferenceChangeListener { _, newValue ->
                updateVoicePreference(newValue as String)
                true
            }

            findPreference<ListPreference>("tts_voice")?.setOnPreferenceChangeListener { _, newValue ->
                playVoiceSample(newValue as String)
                true
            }

            findPreference<Preference>("tts_play_sample")?.setOnPreferenceClickListener {
                val voicePreference = findPreference<ListPreference>("tts_voice")
                voicePreference?.value?.let { playVoiceSample(it) }
                true
            }

            findPreference<ListPreference>("tts_voice")?.summaryProvider = Preference.SummaryProvider<ListPreference> { preference ->
                val value = preference.value
                if (value.isNullOrEmpty()) {
                    getString(R.string.pref_not_set)
                } else {
                    value
                }
            }

            setupNumericInput()
            loadVoices()
        }

        private fun setupNumericInput() {
            val decimalKeys = listOf("tts_speed", "tts_pitch")
            val integerKeys = listOf("count_a", "count_b")

            decimalKeys.forEach { key ->
                findPreference<androidx.preference.EditTextPreference>(key)?.setOnBindEditTextListener { editText ->
                    editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    editText.keyListener = android.text.method.DigitsKeyListener.getInstance("0123456789.")
                }
            }

            integerKeys.forEach { key ->
                findPreference<androidx.preference.EditTextPreference>(key)?.setOnBindEditTextListener { editText ->
                    editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    editText.keyListener = android.text.method.DigitsKeyListener.getInstance("0123456789")
                }
            }
        }

        private fun loadVoices() {
            if (tts == null) {
                tts = TextToSpeech(requireContext()) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        updateVoicePreference()
                    }
                }
            } else {
                updateVoicePreference()
            }
        }

        private fun updateVoicePreference(langOverride: String? = null) {
            val langPref = findPreference<ListPreference>("tts_lang")
            
            // If no language is set, default to system language if supported
            if (langPref != null && langPref.value == null && langOverride == null) {
                val systemLang = java.util.Locale.getDefault().language
                val supportedLangs = listOf("ko", "en", "vi")
                langPref.value = if (systemLang in supportedLangs) systemLang else "all"
            }

            val selectedLang = langOverride ?: langPref?.value ?: "all"

            val voices = tts?.voices?.toList() ?: emptyList()
            val voicePreference = findPreference<ListPreference>("tts_voice")

            if (voicePreference != null && voices.isNotEmpty()) {
                val filteredVoices = when (selectedLang) {
                    "ko" -> voices.filter { it.locale.language == "ko" }
                    "en" -> voices.filter { it.locale.language == "en" }
                    "vi" -> voices.filter { it.locale.language == "vi" }
                    else -> voices
                }

                if (filteredVoices.isNotEmpty()) {
                    val voiceNames = filteredVoices.map { it.name }.toTypedArray()
                    voicePreference.entries = voiceNames
                    voicePreference.entryValues = voiceNames

                    // If current selection is not in filtered list and a specific language is selected, reset it
                    if (selectedLang != "all" && voicePreference.value != null && !voiceNames.contains(voicePreference.value)) {
                        voicePreference.value = null
                    }

                    // If no voice is selected, find the best default based on system locale and quality
                    if (voicePreference.value == null) {
                        val systemLocaleTag = java.util.Locale.getDefault().toLanguageTag().lowercase()
                        
                        // Priority 1: Locale match AND "-language" suffix (usually higher quality)
                        // Priority 2: Locale match
                        // Priority 3: "-language" suffix
                        // Priority 4: First available
                        val bestMatch = filteredVoices.find { 
                            val name = it.name.lowercase()
                            name.contains(systemLocaleTag) && name.endsWith("-language")
                        } ?: filteredVoices.find { 
                            it.name.lowercase().contains(systemLocaleTag)
                        } ?: filteredVoices.find { 
                            it.name.lowercase().endsWith("-language")
                        } ?: filteredVoices.firstOrNull()
                        
                        voicePreference.value = bestMatch?.name
                    }
                    
                    // The summary will be updated by the SummaryProvider set in onCreatePreferences
                }
            }
        }

        override fun playVoiceSample(voiceName: String) {
            val ttsEngine = tts ?: return
            val voice = ttsEngine.voices?.find { it.name == voiceName } ?: return
            
            ttsEngine.voice = voice
            
            // Apply current speed and pitch from SharedPreferences
            val sharedPref = preferenceManager.sharedPreferences
            val speedStr = sharedPref?.getString("tts_speed", "1.0") ?: "1.0"
            val pitchStr = sharedPref?.getString("tts_pitch", "1.0") ?: "1.0"
            
            ttsEngine.setSpeechRate(speedStr.toFloatOrNull() ?: 1.0f)
            ttsEngine.setPitch(pitchStr.toFloatOrNull() ?: 1.0f)
            
            val sampleText = getString(R.string.tts_sample_text)
            ttsEngine.speak(sampleText, TextToSpeech.QUEUE_FLUSH, null, "sample")
        }

        override fun onDestroy() {
            super.onDestroy()
            tts?.shutdown()
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (preference is ColorListPreference) {
                val dialogFragment = ColorPreferenceDialogFragmentCompat.newInstance(preference.key)
                @Suppress("DEPRECATION")
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
            } else if (preference is VoicePreference) {
                val dialogFragment = VoicePreferenceDialogFragmentCompat.newInstance(preference.key)
                @Suppress("DEPRECATION")
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
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
}
