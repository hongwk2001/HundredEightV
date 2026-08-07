package com.tkprof.hundredeightv

import android.content.Context
import android.content.Intent
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndActivityTest {

    @Test
    fun testEndActivityDataPassing() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            device.wakeUp()
            device.pressMenu()
        } catch (e: Exception) {
            // Ignore
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Let's set a fake count_b in shared preferences first
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().putString("count_b", "500").commit()

        val intent = Intent(context, EndActivity::class.java).apply {
            putExtra("DONE_TODAY", 108)
        }

        ActivityScenario.launch<EndActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                activity.window.addFlags(
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                )
            }

            // Verify UI components are displayed
            onView(withId(R.id.btn_restart)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_exit)).check(matches(isDisplayed()))

            // Verify text contains the correct data
            onView(withId(R.id.tv_done_today)).check(matches(withText(org.hamcrest.Matchers.containsString("108"))))
            onView(withId(R.id.tv_overall)).check(matches(withText(org.hamcrest.Matchers.containsString("500"))))
        }
    }
}
