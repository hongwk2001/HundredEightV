package com.tkprof.HundredEightV

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
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
import org.hamcrest.Matchers.allOf
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
        } catch (e: Exception) {
            // Ignore
        }

        activityRule.scenario.onActivity { activity ->
            activity.setTurnScreenOn(true)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(activity, null)
        }
        Intents.init()
        // Wait for the activity to settle and insets to apply
        Thread.sleep(2000)
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
        // Using isDisplayingAtLeast(90) to be robust against edge-to-edge insets
        // Perform click with a small wait to ensure UI is ready
        onView(allOf(withId(R.id.tgb_begin_pause), isDisplayingAtLeast(90)))
            .perform(click())
        
        // Check state change (Start -> Pause or vice versa)
        onView(withId(R.id.tgb_begin_pause)).check(matches(isDisplayed()))
    }

    @Test
    fun testDhammapadaExecutionFlow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit()
            .putString("interval", "1.5")
            .putString("file_name", "법구경_p1_108.json")
            .commit()

        // Toggle play to start
        onView(withId(R.id.tgb_begin_pause)).perform(click())

        // Check if the first verse is displayed (ID 1)
        // Part 1, ID 1 text: "모든 것은 우리의 마음으로부터 나왔고, 마음은 모든 것에 앞선다. 그리고 마음으로부터 모든 것은 이루어진다."
        Thread.sleep(1000)
        onView(withId(R.id.text)).check(matches(isDisplayed()))
        
        // Wait for the next verse (ID 2)
        // Part 1, ID 2 text: "나쁜 마음을 가지고 말하거나 행동하면 그 뒤에는 슬픔이 따라오기 마련. 수레바퀴가 마부의 뒤를 따르듯이."
        Thread.sleep(2500)
        onView(withId(R.id.text)).check(matches(isDisplayed()))
        
        // Verify count updated to 2
        onView(withId(R.id.count)).check(matches(withText("2")))
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
