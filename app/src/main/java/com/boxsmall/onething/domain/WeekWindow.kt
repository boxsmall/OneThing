package com.boxsmall.onething.domain

import java.time.LocalDate

object WeekWindow {
    fun endingToday(today: LocalDate): List<LocalDate> =
        (6L downTo 0L).map(today::minusDays)
}
