package com.boxsmall.onething.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.ui.theme.OneThingTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CompletionFeedbackTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun completionButtonInvokesCallbackOnce() {
        val today = LocalDate.now()
        var completionCount = 0
        composeRule.setContent {
            OneThingTheme {
                HomeScreen(
                    goal = GoalSnapshot(
                        id = 1,
                        name = "走路 20 分钟",
                        startDate = today,
                        endDate = null,
                        completionDates = emptySet(),
                    ),
                    today = today,
                    busy = false,
                    onComplete = { completionCount += 1 },
                    onSettings = {},
                    onRecord = {},
                )
            }
        }

        composeRule.onNodeWithText("我完成了")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle { assertEquals(1, completionCount) }
    }

    @Test
    fun completionOverlayTapWaitsForExitThenDismissesOnce() {
        var dismissCount = 0
        composeRule.setContent {
            OneThingTheme {
                CompletionOverlay(
                    currentStreak = 3,
                    onDismiss = { dismissCount += 1 },
                )
            }
        }

        composeRule.onNodeWithTag("completion-overlay")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 2_000) { dismissCount == 1 }
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
    }

    @Test
    fun completionOverlayAutomaticallyDismissesOnce() {
        var dismissCount = 0
        composeRule.setContent {
            OneThingTheme {
                CompletionOverlay(
                    currentStreak = 4,
                    onDismiss = { dismissCount += 1 },
                )
            }
        }

        composeRule.onNodeWithText("连续第 4 天").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 2_500) { dismissCount == 1 }
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
    }
}
