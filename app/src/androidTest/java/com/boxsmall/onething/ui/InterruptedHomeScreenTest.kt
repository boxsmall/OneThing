package com.boxsmall.onething.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.ui.theme.OneThingTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class InterruptedHomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun oneMissingDayUsesYesterdayCopyAndHidesZeroStreak() {
        val today = LocalDate.now()
        showGoal(
            today = today,
            startDate = today.minusDays(2),
            completions = setOf(today.minusDays(2)),
        )

        composeRule.onNodeWithText("昨天没有完成").assertIsDisplayed()
        composeRule.onNodeWithText("今天继续。").assertIsDisplayed()
        composeRule.onNodeWithText("当前连续").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("设置").assertIsDisplayed()
        composeRule.onNodeWithText("设置 ›").assertDoesNotExist()
    }

    @Test
    fun multipleMissingDaysUseGentleReturnCopy() {
        val today = LocalDate.now()
        showGoal(
            today = today,
            startDate = today.minusDays(4),
            completions = setOf(today.minusDays(4)),
        )

        composeRule.onNodeWithText("有几天没有继续了").assertIsDisplayed()
        composeRule.onNodeWithText("今天回来就好。").assertIsDisplayed()
        composeRule.onNodeWithText("当前连续").assertDoesNotExist()
    }

    private fun showGoal(
        today: LocalDate,
        startDate: LocalDate,
        completions: Set<LocalDate>,
    ) {
        composeRule.setContent {
            OneThingTheme {
                HomeScreen(
                    goal = GoalSnapshot(
                        id = 1,
                        name = "每天走路",
                        startDate = startDate,
                        endDate = null,
                        completionDates = completions,
                    ),
                    today = today,
                    busy = false,
                    onComplete = {},
                    onSettings = {},
                    onRecord = {},
                )
            }
        }
    }
}
