package com.boxsmall.onething.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.boxsmall.onething.MainActivity
import com.boxsmall.onething.OneThingApplication
import com.boxsmall.onething.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val app = context.applicationContext as OneThingApplication
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                executeReminder(context, app)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        internal const val CHANNEL_ID = "daily_reminder"
        internal const val NOTIFICATION_ID = 1001

        internal suspend fun executeReminder(
            context: Context,
            app: OneThingApplication,
            notificationPermissionGranted: Boolean = hasNotificationPermission(context),
        ): ReminderExecutionResult {
            val settings = app.container.settingsStore.settings.first()
            val activeGoal = app.container.goalRepository.observeActiveGoal().first()
            val today = app.container.dateProvider.today()
            val shouldPost = ReminderPolicy.shouldPost(
                settings = settings,
                activeGoal = activeGoal,
                today = today,
                notificationPermissionGranted = notificationPermissionGranted,
            )
            if (shouldPost) postNotification(context)
            return ReminderExecutionResult(
                notificationPosted = shouldPost,
                schedule = app.container.applyReminderState(settings, activeGoal),
            )
        }

        internal fun openAppIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_REMINDER
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        internal fun isOpenReminderIntent(intent: Intent?): Boolean =
            intent?.action == ACTION_OPEN_REMINDER

        private fun hasNotificationPermission(context: Context): Boolean =
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

        private fun postNotification(context: Context) {
            createChannel(context)
            val openApp = PendingIntent.getActivity(
                context,
                0,
                openAppIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("今天的一件事")
                .setContentText("留一点时间，完成此刻最重要的目标。")
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .build()

            context.getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, notification)
        }

        private fun createChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "每日提醒",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "提醒你完成当前唯一目标"
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                },
            )
        }

        private const val ACTION_OPEN_REMINDER = "com.boxsmall.onething.action.OPEN_REMINDER"
    }
}

internal data class ReminderExecutionResult(
    val notificationPosted: Boolean,
    val schedule: ReminderScheduleResult,
)
