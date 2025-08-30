package com.tkprof.hundredeightv

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.Objects

// Remove: import android.preference.PreferenceActivity;

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings) // Create this layout file

        // 1. Find the toolbar
        val toolbar: Toolbar = findViewById(R.id.settings_toolbar)

        // 2. Set it as the support action bar
        setSupportActionBar(toolbar)

        // 3. Enable the Up button (back arrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        // 4. Load your PreferenceFragment
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, GeneralPreferenceFragment())
                .commit()
        }
    }

    // Handle the Up button press to navigate back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Or finish() if it's a simpler case
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Changed from onMenuItemSelected
        val id = item.getItemId()
        if (id == android.R.id.home) {
            // This is the standard way to handle Up navigation in AppCompatActivity
            // NavUtils.navigateUpFromSameTask(this) is also an option if you have
            // specific parent activity logic in your manifest.
            if (!super.onOptionsItemSelected(item)) { // Allows fragment to handle first
                NavUtils.navigateUpFromSameTask(this)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // You likely won't need onIsMultiPane() or isValidFragment()
    // with this AppCompatActivity + PreferenceFragmentCompat setup,
    // as multi-pane is usually handled differently (e.g., with multiple fragments)
    // and isValidFragment was specific to PreferenceActivity's header system.
    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_general, rootKey)
            bindPreferenceSummaryToValue(
                Objects.requireNonNull<Preference?>(
                    findPreference<Preference?>(
                        "count_a"
                    )
                )
            )
            bindPreferenceSummaryToValue(
                Objects.requireNonNull<Preference?>(
                    findPreference<Preference?>(
                        "count_b"
                    )
                )
            )
            bindPreferenceSummaryToValue(
                Objects.requireNonNull<Preference?>(
                    findPreference<Preference?>(
                        "interval"
                    )
                )
            )
            bindPreferenceSummaryToValue(
                Objects.requireNonNull<Preference?>(
                    findPreference<Preference?>(
                        "file_name"
                    )
                )
            )
            bindPreferenceSummaryToValue(
                Objects.requireNonNull<Preference?>(
                    findPreference<Preference?>(
                        "file_line_cnt"
                    )
                )
            )
            bindPreferenceSummaryToValue(
                Objects.requireNonNull<Preference?>(
                    findPreference<Preference?>(
                        "bellsound"
                    )
                )
            )
            bindPreferenceSummaryToValue(
                Objects.requireNonNull<Preference?>(
                    findPreference<Preference?>(
                        "bgcolor"
                    )
                )
            )
        } //        @Override
        //        public boolean onOptionsItemSelected(MenuItem item) {
        //            int id = item.getItemId();
        //            if (id == android.R.id.home) {
        //                return false; // This allows the Activity's onOptionsItemSelected to handle it.
        //            }
        //            return super.onOptionsItemSelected(item);
        //        }
    }

    companion object {
        // Keep sBindPreferenceSummaryToValueListener and bindPreferenceSummaryToValue here
        // or move them into GeneralPreferenceFragment if only used there.
        // Ensure they use androidx.preference types as discussed.
        private val sBindPreferenceSummaryToValueListener: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference, value: Any): Boolean {
                    val stringValue = value.toString()
                    if (preference is ListPreference) {
                        val listPreference = preference
                        val index = listPreference.findIndexOfValue(stringValue)
                        preference.setSummary(if (index >= 0) listPreference.getEntries()[index] else null)
                    } else {
                        preference.setSummary(stringValue)
                    }
                    return true
                }
            }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener)
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), "")
            )
        }


        // Helper method (if you still need to check for tablets, though
        // multi-pane with PreferenceFragmentCompat is done differently)
        private fun isXLargeTablet(context: Context): Boolean {
            return (context.getResources().getConfiguration().screenLayout
                    and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }
    }
}
