package com.boxsmall.onething.notification

import com.boxsmall.onething.data.settings.AppSettings
import com.boxsmall.onething.domain.GoalSnapshot
import java.time.LocalDate

internal data class ReminderScheduleDecision(
    val enabled: Boolean,
    val skipToday: Boolean,
)

internal fun reminderScheduleDecision(
    settings: AppSettings,
    activeGoal: GoalSnapshot?,
    today: LocalDate,
): ReminderScheduleDecision = ReminderScheduleDecision(
    enabled = settings.reminderEnabled && activeGoal?.isActive == true,
    skipToday = activeGoal?.isActive == true && today in activeGoal.completionDates,
)
