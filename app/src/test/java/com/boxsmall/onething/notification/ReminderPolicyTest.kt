package com.boxsmall.onething.notification

import com.boxsmall.onething.data.settings.AppSettings
import com.boxsmall.onething.domain.GoalSnapshot
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPolicyTest {
    private val today = LocalDate.of(2026, 9, 3)
    private val activeGoal = GoalSnapshot(
        id = 1,
        name = "每天走路",
        startDate = today.minusDays(2),
        endDate = null,
        completionDates = emptySet(),
    )

    @Test
    fun postsOnlyForEnabledIncompleteActiveGoalWithPermission() {
        assertTrue(
            ReminderPolicy.shouldPost(
                settings = AppSettings(reminderEnabled = true),
                activeGoal = activeGoal,
                today = today,
                notificationPermissionGranted = true,
            ),
        )
    }

    @Test
    fun skipsWhenReminderIsDisabledOrPermissionIsMissing() {
        assertFalse(
            ReminderPolicy.shouldPost(
                settings = AppSettings(reminderEnabled = false),
                activeGoal = activeGoal,
                today = today,
                notificationPermissionGranted = true,
            ),
        )
        assertFalse(
            ReminderPolicy.shouldPost(
                settings = AppSettings(reminderEnabled = true),
                activeGoal = activeGoal,
                today = today,
                notificationPermissionGranted = false,
            ),
        )
    }

    @Test
    fun skipsWithoutAnActiveGoalOrAfterCompletion() {
        assertFalse(
            ReminderPolicy.shouldPost(
                settings = AppSettings(),
                activeGoal = null,
                today = today,
                notificationPermissionGranted = true,
            ),
        )
        assertFalse(
            ReminderPolicy.shouldPost(
                settings = AppSettings(),
                activeGoal = activeGoal.copy(completionDates = setOf(today)),
                today = today,
                notificationPermissionGranted = true,
            ),
        )
        assertFalse(
            ReminderPolicy.shouldPost(
                settings = AppSettings(),
                activeGoal = activeGoal.copy(endDate = today),
                today = today,
                notificationPermissionGranted = true,
            ),
        )
    }
}
