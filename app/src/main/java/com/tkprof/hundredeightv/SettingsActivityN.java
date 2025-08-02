package com.tkprof.hundredeightv;

// Remove: import android.preference.PreferenceActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity; // Import AppCompatActivity
import androidx.core.app.NavUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsActivityN extends AppCompatActivity { // Extend AppCompatActivity

    // Keep sBindPreferenceSummaryToValueListener and bindPreferenceSummaryToValue here
    // or move them into GeneralPreferenceFragment if only used there.
    // Ensure they use androidx.preference types as discussed.

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object value) {
                    String stringValue = value.toString();
                    if (preference instanceof androidx.preference.ListPreference) {
                        androidx.preference.ListPreference listPreference = (androidx.preference.ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);
                        preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                    } else {
                        preference.setSummary(stringValue);
                    }
                    return true;
                }
            };

    private static void bindPreferenceSummaryToValue(androidx.preference.Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Create this layout file

        setupActionBar();

        // Display the fragment as the main content.
        if (savedInstanceState == null) { // Only add the fragment if it's not being restored
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, new GeneralPreferenceFragment()) // Use your container ID
                    .commit();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Optionally set a title if not set by the theme or manifest
            // actionBar.setTitle(R.string.settings_title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // Changed from onMenuItemSelected
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This is the standard way to handle Up navigation in AppCompatActivity
            // NavUtils.navigateUpFromSameTask(this) is also an option if you have
            // specific parent activity logic in your manifest.
            if (!super.onOptionsItemSelected(item)) { // Allows fragment to handle first
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // You likely won't need onIsMultiPane() or isValidFragment()
    // with this AppCompatActivity + PreferenceFragmentCompat setup,
    // as multi-pane is usually handled differently (e.g., with multiple fragments)
    // and isValidFragment was specific to PreferenceActivity's header system.

    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("count_a")));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("count_b")));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("interval")));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("file_name")));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("file_line_cnt")));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("bellsound")));
            bindPreferenceSummaryToValue(Objects.requireNonNull(findPreference("bgcolor")));
        }

//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            int id = item.getItemId();
//            if (id == android.R.id.home) {
//                return false; // This allows the Activity's onOptionsItemSelected to handle it.
//            }
//            return super.onOptionsItemSelected(item);
//        }
    }

    // Helper method (if you still need to check for tablets, though
    // multi-pane with PreferenceFragmentCompat is done differently)
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
}
