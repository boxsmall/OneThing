package com.boxsmall.onething.notification

import com.boxsmall.onething.data.settings.AppSettings
import com.boxsmall.onething.domain.GoalSnapshot
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderSchedulePolicyTest {
    private val today = LocalDate.of(2026, 9, 3)
    private val activeGoal = GoalSnapshot(
        id = 1,
        name = "每天走路",
        startDate = today,
        endDate = null,
        completionDates = emptySet(),
    )

    @Test
    fun activeIncompleteGoalSchedulesToday() {
        assertEquals(
            ReminderScheduleDecision(enabled = true, skipToday = false),
            reminderScheduleDecision(AppSettings(reminderEnabled = true), activeGoal, today),
        )
    }

    @Test
    fun completedGoalKeepsSchedulingButSkipsToday() {
        assertEquals(
            ReminderScheduleDecision(enabled = true, skipToday = true),
            reminderScheduleDecision(
                AppSettings(reminderEnabled = true),
                activeGoal.copy(completionDates = setOf(today)),
                today,
            ),
        )
    }

    @Test
    fun disabledOrMissingGoalCancelsScheduling() {
        assertEquals(
            ReminderScheduleDecision(enabled = false, skipToday = false),
            reminderScheduleDecision(AppSettings(reminderEnabled = false), activeGoal, today),
        )
        assertEquals(
            ReminderScheduleDecision(enabled = false, skipToday = false),
            reminderScheduleDecision(AppSettings(reminderEnabled = true), null, today),
        )
    }
}
