package com.tkprof.HundredEightV

import android.content.Context
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
            activity.setTurnScreenOn(true)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(activity, null)
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
        // Wait for preference fragment to load and settle
        Thread.sleep(2000)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Matcher for finding the item in the list
        // We use withEffectiveVisibility(Visibility.VISIBLE) to distinguish it from 
        // stale or reused views in other positions (like version_info) where the title is GONE.
        val itemMatcher = hasDescendant(allOf(
            withId(android.R.id.title),
            withText(R.string.about_title),
            withEffectiveVisibility(Visibility.VISIBLE)
        ))

        // Find the RecyclerView and scroll to the About preference item
        onView(allOf(withId(androidx.preference.R.id.recycler_view), isDisplayed()))
            .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(itemMatcher))

        // Then perform the click action on the item
        onView(allOf(withId(androidx.preference.R.id.recycler_view), isDisplayed()))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                itemMatcher,
                click()
            ))

        intended(hasComponent(AboutActivity::class.java.name))
    }
}
