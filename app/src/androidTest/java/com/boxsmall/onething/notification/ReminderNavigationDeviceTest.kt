package com.boxsmall.onething.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boxsmall.onething.OneThingApplication
import com.boxsmall.onething.ui.theme.OneThingTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZReminderNavigationDeviceTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestHostActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val app = context.applicationContext as OneThingApplication

    @Before
    fun setUp() {
        runBlocking {
            endActiveGoalIfPresent()
            app.container.settingsStore.completeOnboarding()
            app.container.settingsStore.updateReminder(enabled = false, hour = 20, minute = 0)
            app.container.goalRepository.createGoal("通知入口首页")
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            endActiveGoalIfPresent()
            app.container.settingsStore.updateReminder(enabled = false, hour = 20, minute = 0)
            app.container.refreshReminder()
        }
    }

    @Test
    fun reminderEntryReturnsHomeAndFallsBackToCreateWithoutAnActiveGoal() {
        lateinit var openReminder: () -> Unit
        val viewModel = MainViewModel(app.container)
        composeRule.setContent {
            var reminderOpenGeneration by remember { mutableIntStateOf(1) }
            openReminder = { reminderOpenGeneration++ }
            OneThingTheme {
                OneThingApp(
                    viewModel = viewModel,
                    reminderOpenGeneration = reminderOpenGeneration,
                )
            }
        }

        waitForTag("home-screen")
        composeRule.onNodeWithTag("home-screen").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.onNodeWithContentDescription("返回").assertIsDisplayed()

        composeRule.runOnUiThread(openReminder)
        waitForTag("home-screen")
        composeRule.onNodeWithTag("home-screen").assertIsDisplayed()

        runBlocking {
            assertTrue(app.container.goalRepository.endActiveGoal())
        }
        composeRule.runOnUiThread(openReminder)
        waitForTag("create-screen")
        composeRule.onNodeWithTag("create-screen").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private suspend fun endActiveGoalIfPresent() {
        if (app.container.goalRepository.observeActiveGoal().first() != null) {
            app.container.goalRepository.endActiveGoal()
        }
    }
}
