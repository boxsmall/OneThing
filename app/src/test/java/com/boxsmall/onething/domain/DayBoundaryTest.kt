package com.boxsmall.onething.domain

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DayBoundaryTest {
    @Test
    fun normalDayWaitsUntilNextMidnight() {
        val now = ZonedDateTime.of(2026, 9, 3, 21, 30, 0, 0, ZoneId.of("Asia/Shanghai"))

        assertEquals(Duration.ofHours(2).plusMinutes(30).toMillis(), millisecondsUntilNextLocalDay(now))
    }

    @Test
    fun springForwardDayUsesTheActualShorterDuration() {
        val now = ZonedDateTime.of(2026, 3, 8, 0, 0, 0, 0, ZoneId.of("America/New_York"))

        assertEquals(Duration.ofHours(23).toMillis(), millisecondsUntilNextLocalDay(now))
    }

    @Test
    fun fallBackDayUsesTheActualLongerDuration() {
        val now = ZonedDateTime.of(2026, 11, 1, 0, 0, 0, 0, ZoneId.of("America/New_York"))

        assertEquals(Duration.ofHours(25).toMillis(), millisecondsUntilNextLocalDay(now))
    }
}
