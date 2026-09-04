package com.boxsmall.onething.notification

import com.boxsmall.onething.data.settings.AppSettings
import com.boxsmall.onething.domain.GoalSnapshot
import java.time.LocalDate

internal object ReminderPolicy {
    fun shouldPost(
        settings: AppSettings,
        activeGoal: GoalSnapshot?,
        today: LocalDate,
        notificationPermissionGranted: Boolean,
    ): Boolean = settings.reminderEnabled &&
        notificationPermissionGranted &&
        activeGoal?.isActive == true &&
        today !in activeGoal.completionDates
}
