package com.boxsmall.onething.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
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
                    forceFallback = true,
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
                    animationsDisabledOverride = false,
                    onDismiss = { dismissCount += 1 },
                )
            }
        }

        val description = "完成反馈。今天完成，连续第 4 天，明天继续"
        composeRule.waitUntil(timeoutMillis = 2_500) {
            composeRule.onAllNodesWithContentDescription(description)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(description).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 2_500) { dismissCount == 1 }
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
    }

    @Test
    fun completionOverlayBackInterruptDismissesOnce() {
        var dismissCount = 0
        composeRule.setContent {
            OneThingTheme {
                CompletionOverlay(
                    currentStreak = 7,
                    animationsDisabledOverride = false,
                    previewProgress = .78f,
                    onDismiss = { dismissCount += 1 },
                )
            }
        }

        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.waitUntil(timeoutMillis = 1_000) { dismissCount == 1 }
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
    }

    @Test
    fun reducedMotionUsesFastAccessibleFallback() {
        var dismissCount = 0
        composeRule.setContent {
            OneThingTheme {
                CompletionOverlay(
                    currentStreak = 30,
                    animationsDisabledOverride = true,
                    onDismiss = { dismissCount += 1 },
                )
            }
        }

        composeRule.onNodeWithContentDescription(
            "完成反馈。今天完成，连续第 30 天，明天继续。",
        ).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 1_000) { dismissCount == 1 }
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
    }

    @Test
    fun milestoneUsesDedicatedAnimationResource() {
        assertEquals(com.boxsmall.onething.R.raw.complete_streak_3, completionAnimationResource(3))
        assertEquals(com.boxsmall.onething.R.raw.complete_streak_7, completionAnimationResource(7))
        assertEquals(com.boxsmall.onething.R.raw.complete_streak_30, completionAnimationResource(30))
        assertEquals(com.boxsmall.onething.R.raw.complete_streak_30, completionAnimationResource(45))
        assertEquals(com.boxsmall.onething.R.raw.complete_success, completionAnimationResource(8))
    }
}
