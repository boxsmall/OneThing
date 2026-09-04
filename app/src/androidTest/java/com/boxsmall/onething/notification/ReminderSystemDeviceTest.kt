package com.boxsmall.onething.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boxsmall.onething.MainActivity
import com.boxsmall.onething.OneThingApplication
import java.io.FileInputStream
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderSystemDeviceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val app = context.applicationContext as OneThingApplication
    private val notifications = context.getSystemService(NotificationManager::class.java)

    @Before
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shell("pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}")
        }
        runBlocking {
            notifications.cancelAll()
            endActiveGoalIfPresent()
            app.container.settingsStore.updateReminder(enabled = false, hour = 20, minute = 0)
            app.container.refreshReminder()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            notifications.cancelAll()
            endActiveGoalIfPresent()
            app.container.settingsStore.updateReminder(enabled = false, hour = 20, minute = 0)
            app.container.refreshReminder()
        }
    }

    @Test
    fun activeIncompleteGoalPostsNotificationAndRegistersNextAlarm() = runBlocking {
        val reminderTime = ZonedDateTime.now().plusMinutes(2)
        app.container.goalRepository.createGoal("设备提醒测试")
        app.container.settingsStore.updateReminder(
            enabled = true,
            hour = reminderTime.hour,
            minute = reminderTime.minute,
        )

        val result = ReminderReceiver.executeReminder(
            context = context,
            app = app,
            notificationPermissionGranted = true,
        )

        assertTrue(result.notificationPosted)
        assertTrue(result.schedule.scheduled)
        assertEquals(reminderTime.toLocalDate(), result.schedule.triggerAt?.toLocalDate())
        assertEquals(reminderTime.hour, result.schedule.triggerAt?.hour)
        assertEquals(reminderTime.minute, result.schedule.triggerAt?.minute)

        val posted = awaitActiveNotification(ReminderReceiver.NOTIFICATION_ID)
        assertNotNull(posted)
        assertEquals(
            "今天的一件事",
            posted?.notification?.extras?.getCharSequence(Notification.EXTRA_TITLE),
        )
        assertEquals(
            "留一点时间，完成此刻最重要的目标。",
            posted?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT),
        )
        assertNotNull(posted?.notification?.contentIntent)
        assertEquals(context.packageName, posted?.notification?.contentIntent?.creatorPackage)

        val openIntent = ReminderReceiver.openAppIntent(context)
        assertEquals(MainActivity::class.java.name, openIntent.component?.className)
        assertTrue(openIntent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
        assertAlarmRegistered()
    }

    private fun awaitActiveNotification(id: Int): StatusBarNotification? {
        repeat(20) {
            notifications.activeNotifications.singleOrNull { it.id == id }?.let { return it }
            SystemClock.sleep(100)
        }
        return notifications.activeNotifications.singleOrNull { it.id == id }
    }

    @Test
    fun completedGoalSkipsNotificationAndSchedulesTomorrow() = runBlocking {
        val reminderTime = ZonedDateTime.now().plusMinutes(2)
        app.container.goalRepository.createGoal("完成后跳过提醒")
        assertTrue(app.container.goalRepository.completeToday())
        app.container.settingsStore.updateReminder(
            enabled = true,
            hour = reminderTime.hour,
            minute = reminderTime.minute,
        )

        val result = ReminderReceiver.executeReminder(
            context = context,
            app = app,
            notificationPermissionGranted = true,
        )

        assertFalse(result.notificationPosted)
        assertTrue(result.schedule.scheduled)
        assertEquals(
            app.container.dateProvider.today().plusDays(1),
            result.schedule.triggerAt?.toLocalDate(),
        )
        assertNull(
            notifications.activeNotifications
                .singleOrNull { it.id == ReminderReceiver.NOTIFICATION_ID },
        )
        assertAlarmRegistered()
    }

    @Test
    fun disabledReminderCancelsAlarmAndNotificationIsNotPosted() = runBlocking {
        app.container.goalRepository.createGoal("关闭提醒测试")
        app.container.settingsStore.updateReminder(enabled = false, hour = 20, minute = 0)

        val result = ReminderReceiver.executeReminder(
            context = context,
            app = app,
            notificationPermissionGranted = true,
        )

        assertFalse(result.notificationPosted)
        assertFalse(result.schedule.scheduled)
        assertNull(result.schedule.triggerAt)
        assertNull(
            notifications.activeNotifications
                .singleOrNull { it.id == ReminderReceiver.NOTIFICATION_ID },
        )
        assertAlarmNotRegistered()
    }

    @Test
    fun supportedSystemBroadcastsReapplyReminderState() = runBlocking {
        val reminderTime = ZonedDateTime.now().plusMinutes(2)
        app.container.goalRepository.createGoal("系统广播重排")
        app.container.settingsStore.updateReminder(
            enabled = true,
            hour = reminderTime.hour,
            minute = reminderTime.minute,
        )

        listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        ).forEach { action -> assertTrue(BootReceiver.supportsAction(action)) }
        assertFalse(BootReceiver.supportsAction(Intent.ACTION_AIRPLANE_MODE_CHANGED))

        val result = app.container.refreshReminder()

        assertTrue(result.scheduled)
        assertEquals(reminderTime.hour, result.triggerAt?.hour)
        assertEquals(reminderTime.minute, result.triggerAt?.minute)
        assertAlarmRegistered()
    }

    private suspend fun endActiveGoalIfPresent() {
        if (app.container.goalRepository.observeActiveGoal().first() != null) {
            app.container.goalRepository.endActiveGoal()
        }
    }

    private fun assertAlarmRegistered() {
        val alarms = currentPendingAlarms()
        assertTrue(alarms.contains(reminderAlarmIdentity()))
    }

    private fun assertAlarmNotRegistered() {
        assertFalse(currentPendingAlarms().contains(reminderAlarmIdentity()))
    }

    private fun reminderAlarmIdentity(): String =
        "${context.packageName}/${ReminderReceiver::class.java.name}"

    private fun currentPendingAlarms(): String {
        val dump = shell("dumpsys alarm")
        val modernHeader = Regex("(?m)^[ \\t]*\\d+ pending alarms:[ \\t]*$").find(dump)
        if (modernHeader != null) {
            return dump
                .substring(modernHeader.range.last + 1)
                .substringBefore("LazyAlarmStore stats:")
        }

        val legacyHeader = Regex("(?m)^[ \\t]*Pending alarm batches:[ \\t]*\\d+[ \\t]*$")
            .find(dump)
            ?: throw AssertionError("AlarmManager pending section was not found")
        return dump
            .substring(legacyHeader.range.last + 1)
            .substringBefore("Past-due non-wakeup alarms:")
    }

    private fun shell(command: String): String {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
    }
}
