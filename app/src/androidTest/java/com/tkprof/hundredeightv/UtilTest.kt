package com.tkprof.HundredEightV

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilTest {

    @Test
    fun testMigratePreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        
        // Setup initial old state (Ints/Floats)
        sharedPref.edit()
            .clear()
            .putInt("interval", 15)
            .putInt("tts_speed", 10) // 1.0
            .commit()

        // Run migration
        Util.migratePreferences(context)

        // Validate
        assertEquals("15", sharedPref.getString("interval", null))
        assertEquals("1.0", sharedPref.getString("tts_speed", null))
    }
}
