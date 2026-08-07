package com.tkprof.hundredeightv

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Matchers.allOf
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
import org.junit.After

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SettingsActivity::class.java)

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
    fun testSettingsActivityUI_ElementsAreDisplayed() {
        onView(withId(R.id.settings_toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_close_settings)).check(matches(isDisplayed()))
    }

    @Test
    fun testAboutPageNavigation() {
        // Ensure the fragment is loaded by checking a top-level preference
        onView(withText(R.string.interval)).check(matches(isDisplayed()))

        // Ensure the RecyclerView is displayed
        onView(allOf(withId(androidx.preference.R.id.recycler_view), isDisplayed()))
            .check(matches(isDisplayed()))

        // Click on the About preference using a robust RecyclerView matcher
        onView(allOf(withId(androidx.preference.R.id.recycler_view), isDisplayed()))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(R.string.about_title)),
                click()
            ))

        intended(hasComponent(AboutActivity::class.java.name))
    }
}
