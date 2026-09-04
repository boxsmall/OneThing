package com.boxsmall.onething.domain

import java.time.Duration
import java.time.ZonedDateTime

fun millisecondsUntilNextLocalDay(now: ZonedDateTime): Long {
    val nextDayStart = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
    return Duration.between(now.toInstant(), nextDayStart.toInstant())
        .toMillis()
        .coerceAtLeast(1L)
}
