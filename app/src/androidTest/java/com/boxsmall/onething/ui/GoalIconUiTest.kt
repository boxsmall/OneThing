package com.boxsmall.onething.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.boxsmall.onething.domain.GoalIconKey
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.ui.theme.OneThingTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GoalIconUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun settingsCanChangeCurrentGoalIcon() {
        var currentIcon by mutableStateOf(GoalIconKey.OTHER)
        composeRule.setContent {
            OneThingTheme {
                SettingsScreen(
                    currentGoalName = "每天走路",
                    currentGoalIcon = currentIcon,
                    reminderEnabled = false,
                    reminderHour = 20,
                    reminderMinute = 0,
                    busy = false,
                    onRenameGoal = {},
                    onGoalIconChange = { currentIcon = it },
                    onReminderChange = { _, _, _ -> },
                    onHistory = {},
                    onAbout = {},
                    onEndGoal = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("目标图标").performClick()
        composeRule.onNodeWithTag("goal-icon-walk").performClick()
        composeRule.onNodeWithText("保存").performClick()

        composeRule.runOnIdle { assertEquals(GoalIconKey.WALK, currentIcon) }
        composeRule.onNodeWithText("走路").assertIsDisplayed()
    }

    @Test
    fun historyShowsPersistedGoalIcon() {
        val today = LocalDate.of(2026, 9, 5)
        composeRule.setContent {
            OneThingTheme {
                HistoryScreen(
                    history = listOf(
                        GoalSnapshot(
                            id = 1,
                            name = "每天阅读",
                            iconKey = GoalIconKey.READ,
                            startDate = today.minusDays(7),
                            endDate = today,
                            completionDates = setOf(today),
                        ),
                    ),
                    today = today,
                    returnLabel = "返回设置",
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("阅读目标图标").assertIsDisplayed()
        composeRule.onNodeWithText("每天阅读").assertIsDisplayed()
    }
}
