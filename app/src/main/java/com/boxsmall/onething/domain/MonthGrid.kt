package com.boxsmall.onething.domain

import java.time.LocalDate
import java.time.YearMonth

object MonthGrid {
    fun dates(month: YearMonth): List<LocalDate?> {
        val leadingEmptyDays = month.atDay(1).dayOfWeek.value - 1
        val usedCells = leadingEmptyDays + month.lengthOfMonth()
        val cellCount = ((usedCells + 6) / 7) * 7
        return List(cellCount) { index ->
            val dayOfMonth = index - leadingEmptyDays + 1
            if (dayOfMonth in 1..month.lengthOfMonth()) month.atDay(dayOfMonth) else null
        }
    }
}
