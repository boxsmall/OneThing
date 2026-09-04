package com.boxsmall.onething

import android.content.Context
import com.boxsmall.onething.core.AnalyticsTracker
import com.boxsmall.onething.core.DateProvider
import com.boxsmall.onething.core.NoOpAnalyticsTracker
import com.boxsmall.onething.core.SystemDateProvider
import com.boxsmall.onething.data.GoalRepository
import com.boxsmall.onething.data.local.OneThingDatabase
import com.boxsmall.onething.data.settings.SettingsStore
import com.boxsmall.onething.notification.ReminderScheduler
import com.boxsmall.onething.notification.ReminderScheduleResult
import com.boxsmall.onething.notification.reminderScheduleDecision
import com.boxsmall.onething.data.settings.AppSettings
import com.boxsmall.onething.domain.GoalSnapshot
import kotlinx.coroutines.flow.first

class AppContainer(context: Context) {
    private val database = OneThingDatabase.create(context)
    val dateProvider: DateProvider = SystemDateProvider()
    val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker
    val settingsStore = SettingsStore(context.applicationContext)
    val reminderScheduler = ReminderScheduler(context.applicationContext)
    val goalRepository = GoalRepository(
        database = database,
        goalDao = database.goalDao(),
        completionDao = database.completionDao(),
        dateProvider = dateProvider,
    )

    internal fun applyReminderState(
        settings: AppSettings,
        activeGoal: GoalSnapshot?,
    ): ReminderScheduleResult {
        val today = dateProvider.today()
        val decision = reminderScheduleDecision(settings, activeGoal, today)
        return reminderScheduler.applySettings(
            settings = settings.copy(reminderEnabled = decision.enabled),
            skipToday = decision.skipToday,
        )
    }

    internal suspend fun refreshReminder(): ReminderScheduleResult =
        applyReminderState(
            settings = settingsStore.settings.first(),
            activeGoal = goalRepository.observeActiveGoal().first(),
        )
}
