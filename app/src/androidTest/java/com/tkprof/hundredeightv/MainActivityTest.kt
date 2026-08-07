package com.tkprof.hundredeightv

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.view.WindowManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.not
import org.junit.After
import android.content.Context
import androidx.test.core.app.ApplicationProvider

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            device.wakeUp()
            device.pressMenu()
        } catch (e: Exception) {
            // Ignore
        }

        activityRule.scenario.onActivity { activity ->
            activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testMainActivityUI_ElementsAreDisplayed() {
        onView(withId(R.id.cbx_tts_number)).check(matches(isDisplayed()))
        onView(withId(R.id.cbx_tts_text)).check(matches(isDisplayed()))
        onView(withId(R.id.tgb_begin_pause)).check(matches(isDisplayed()))
        onView(withId(R.id.count)).check(matches(isDisplayed()))
    }

    @Test
    fun testPlayPauseToggle() {
        // Toggle the button and check it's still displayed (no crash)
        onView(withId(R.id.tgb_begin_pause)).perform(click())
        onView(withId(R.id.tgb_begin_pause)).check(matches(isDisplayed()))
    }

    @Test
    fun testInDepthVowFlow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit()
            .putString("interval", "1")
            .putString("file_name", "touching_the_earth_6vows.json")
            .commit()

        // Toggle play to start the 6 vows
        onView(withId(R.id.tgb_begin_pause)).perform(click())
        
        // Wait 2.5s for count to naturally reach 2 or 3
        Thread.sleep(2500)
        
        // Toggle pause
        onView(withId(R.id.tgb_begin_pause)).perform(click())
        
        // Wait 2s to ensure the timer is actually stopped and count doesn't increment wildly
        Thread.sleep(2000)
        
        // Dismiss the dialog to return to the activity so we can swipe on rootLayout
        onView(withId(R.id.btn_continue)).perform(click())
        
        // Swipe left to manually navigate to the next vow
        onView(withId(R.id.rootLayout)).perform(androidx.test.espresso.action.ViewActions.swipeLeft())
        
        // Wait for the remaining vows to finish (at 1 second interval)
        // Adding buffer time for the 4s transition delay in MainActivity.f_GoToEndActivity
        Thread.sleep(10000)
        
        // Verify that completing the JSON file automatically launches EndActivity
        intended(hasComponent(EndActivity::class.java.name))
    }
}
