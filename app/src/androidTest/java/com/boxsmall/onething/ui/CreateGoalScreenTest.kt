package com.boxsmall.onething.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.boxsmall.onething.ui.theme.OneThingTheme
import com.boxsmall.onething.domain.GoalIconKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class CreateGoalScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun reminderCanBeDisabledBeforeCreatingGoal() {
        var submitted: SubmittedGoal? = null
        composeRule.setContent {
            OneThingTheme {
                CreateGoalScreen(
                    busy = false,
                    hasHistory = false,
                    initialReminderEnabled = true,
                    initialReminderHour = 20,
                    initialReminderMinute = 0,
                    onCreate = { name, iconKey, enabled, hour, minute ->
                        submitted = SubmittedGoal(name, iconKey, enabled, hour, minute)
                    },
                    onHistory = {},
                )
            }
        }

        composeRule.onNodeWithText("20:00").assertIsDisplayed()
        composeRule.onNodeWithTag("create-reminder-toggle").performClick()
        composeRule.onNodeWithText("20:00").assertDoesNotExist()
        composeRule.onNodeWithTag("create-name").performTextInput("Walk 20 minutes")
        composeRule.onNodeWithTag("create-submit").performClick()

        composeRule.runOnIdle {
            val result = requireNotNull(submitted)
            assertEquals("Walk 20 minutes", result.name)
            assertEquals(GoalIconKey.OTHER, result.iconKey)
            assertFalse(result.reminderEnabled)
            assertEquals(20, result.hour)
            assertEquals(0, result.minute)
        }
    }

    private data class SubmittedGoal(
        val name: String,
        val iconKey: GoalIconKey,
        val reminderEnabled: Boolean,
        val hour: Int,
        val minute: Int,
    )

    @Test
    fun selectedIconIsSubmittedWithGoal() {
        var submittedIcon: GoalIconKey? = null
        composeRule.setContent {
            OneThingTheme {
                CreateGoalScreen(
                    busy = false,
                    hasHistory = false,
                    initialReminderEnabled = false,
                    initialReminderHour = 20,
                    initialReminderMinute = 0,
                    onCreate = { _, iconKey, _, _, _ -> submittedIcon = iconKey },
                    onHistory = {},
                )
            }
        }

        composeRule.onNodeWithTag("goal-icon-walk").performClick()
        composeRule.onNodeWithTag("goal-icon-walk").assertIsSelected()
        composeRule.onNodeWithTag("create-name").performTextInput("每天走路")
        composeRule.onNodeWithTag("create-submit").performClick()

        composeRule.runOnIdle { assertEquals(GoalIconKey.WALK, submittedIcon) }
    }
}
