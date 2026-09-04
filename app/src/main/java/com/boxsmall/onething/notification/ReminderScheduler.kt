package com.boxsmall.onething.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.boxsmall.onething.data.settings.AppSettings
import java.time.ZonedDateTime

class ReminderScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    internal fun applySettings(
        settings: AppSettings,
        skipToday: Boolean = false,
    ): ReminderScheduleResult {
        val operation = reminderPendingIntent()
        alarmManager.cancel(operation)
        if (!settings.reminderEnabled) {
            operation.cancel()
            return ReminderScheduleResult(triggerAt = null)
        }

        val next = nextReminderAt(
            now = ZonedDateTime.now(),
            hour = settings.reminderHour,
            minute = settings.reminderMinute,
            skipToday = skipToday,
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next.toInstant().toEpochMilli(),
            operation,
        )
        return ReminderScheduleResult(triggerAt = next)
    }

    private fun reminderPendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        private const val REQUEST_CODE = 1001

        fun nextReminderAt(
            now: ZonedDateTime,
            hour: Int,
            minute: Int,
            skipToday: Boolean = false,
        ): ZonedDateTime {
            require(hour in 0..23)
            require(minute in 0..59)
            val candidate = now
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)
            val next = if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
            return if (skipToday && next.toLocalDate() == now.toLocalDate()) {
                next.plusDays(1)
            } else {
                next
            }
        }
    }
}

internal data class ReminderScheduleResult(
    val triggerAt: ZonedDateTime?,
) {
    val scheduled: Boolean get() = triggerAt != null
}
