package com.boxsmall.onething.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeekWindowTest {
    @Test
    fun windowContainsPastSixDaysAndToday() {
        val today = LocalDate.of(2026, 9, 2)

        val result = WeekWindow.endingToday(today)

        assertEquals(7, result.size)
        assertEquals(LocalDate.of(2026, 8, 27), result.first())
        assertEquals(today, result.last())
    }
}
