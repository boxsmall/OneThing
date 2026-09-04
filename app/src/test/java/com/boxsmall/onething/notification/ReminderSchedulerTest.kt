package com.boxsmall.onething.notification

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderSchedulerTest {
    private val zone = ZoneId.of("Asia/Shanghai")

    @Test
    fun schedulesTodayWhenTimeIsStillAhead() {
        val now = ZonedDateTime.of(2026, 9, 2, 19, 30, 15, 0, zone)

        val result = ReminderScheduler.nextReminderAt(now, hour = 20, minute = 0)

        assertEquals(ZonedDateTime.of(2026, 9, 2, 20, 0, 0, 0, zone), result)
    }

    @Test
    fun schedulesTomorrowWhenTimeHasPassed() {
        val now = ZonedDateTime.of(2026, 9, 2, 20, 0, 1, 0, zone)

        val result = ReminderScheduler.nextReminderAt(now, hour = 20, minute = 0)

        assertEquals(ZonedDateTime.of(2026, 9, 3, 20, 0, 0, 0, zone), result)
    }

    @Test
    fun completionBeforeReminderSkipsTodayAndSchedulesTomorrow() {
        val now = ZonedDateTime.of(2026, 9, 2, 9, 0, 0, 0, zone)

        val result = ReminderScheduler.nextReminderAt(
            now,
            hour = 20,
            minute = 0,
            skipToday = true,
        )

        assertEquals(ZonedDateTime.of(2026, 9, 3, 20, 0, 0, 0, zone), result)
    }

    @Test
    fun completionAfterReminderDoesNotSkipAnExtraDay() {
        val now = ZonedDateTime.of(2026, 9, 2, 21, 0, 0, 0, zone)

        val result = ReminderScheduler.nextReminderAt(
            now,
            hour = 20,
            minute = 0,
            skipToday = true,
        )

        assertEquals(ZonedDateTime.of(2026, 9, 3, 20, 0, 0, 0, zone), result)
    }

    @Test
    fun recalculatesAtTheConfiguredLocalTimeAfterTimezoneChanges() {
        val sameInstantInTokyo = ZonedDateTime
            .of(2026, 9, 2, 18, 30, 0, 0, zone)
            .withZoneSameInstant(ZoneId.of("Asia/Tokyo"))

        val result = ReminderScheduler.nextReminderAt(
            now = sameInstantInTokyo,
            hour = 20,
            minute = 0,
        )

        assertEquals(ZoneId.of("Asia/Tokyo"), result.zone)
        assertEquals(20, result.hour)
        assertEquals(0, result.minute)
        assertEquals(sameInstantInTokyo.toLocalDate(), result.toLocalDate())
    }
}
