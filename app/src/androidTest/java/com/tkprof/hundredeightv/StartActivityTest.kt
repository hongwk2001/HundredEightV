package com.tkprof.HundredEightV

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
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import org.junit.After

@RunWith(AndroidJUnit4::class)
class StartActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(StartActivity::class.java)

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
    fun testStartActivityUI_ElementsAreDisplayed() {
        onView(withId(R.id.btn_begin)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_settings_start_btn)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_exit)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_interval_value)).check(matches(isDisplayed()))
        onView(withId(R.id.spinner_vow_file)).check(matches(isDisplayed()))
    }

    @Test
    fun testIntervalButtons() {
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
        // Click the +1s button and ensure no crash, and that the text view is still displayed
        onView(withId(R.id.btn_plus_1)).perform(click())
        onView(withId(R.id.tv_interval_value)).check(matches(isDisplayed()))
        
        // Click the -1s button
        onView(withId(R.id.btn_minus_1)).perform(click())
        onView(withId(R.id.tv_interval_value)).check(matches(isDisplayed()))
    }

    @Test
    fun testBeginLaunchesMainActivity() {
        onView(withId(R.id.btn_begin)).perform(click())
        intended(allOf(
            hasComponent(MainActivity::class.java.name),
            hasExtra("AUTO_START", true)
        ))
    }

    @Test
    fun testSelectDhammapadaAndStart() {
        // Open the spinner
        onView(withId(R.id.spinner_vow_file)).perform(click())

        // Select "법구경_p1_108" from the dropdown
        // The adapter uses displayValues (filenames without .json)
        androidx.test.espresso.Espresso.onData(org.hamcrest.Matchers.`is`("법구경_p1_108"))
            .perform(click())

        // Click the Begin button
        onView(withId(R.id.btn_begin)).perform(click())

        // Verify MainActivity is launched with AUTO_START
        intended(allOf(
            hasComponent(MainActivity::class.java.name),
            hasExtra("AUTO_START", true)
        ))

        // Give it a moment to load and start in MainActivity
        Thread.sleep(2000)

        // Verify that some text from the Dhammapada is displayed in MainActivity
        // The first verse is: "모든 것은 우리의 마음으로부터 나왔고..."
        onView(withId(R.id.text)).check(matches(isDisplayed()))
        onView(withId(R.id.count)).check(matches(withText("1")))
    }
}
