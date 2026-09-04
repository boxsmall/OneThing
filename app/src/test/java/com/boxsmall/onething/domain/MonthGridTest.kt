package com.boxsmall.onething.domain

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MonthGridTest {
    @Test
    fun startsOnMondayAndPadsToWholeWeeks() {
        val dates = MonthGrid.dates(YearMonth.of(2026, 9))

        assertNull(dates[0])
        assertEquals(LocalDate.of(2026, 9, 1), dates[1])
        assertEquals(LocalDate.of(2026, 9, 30), dates[30])
        assertEquals(35, dates.size)
    }

    @Test
    fun monthStartingOnMondayHasNoLeadingPadding() {
        val dates = MonthGrid.dates(YearMonth.of(2026, 6))

        assertEquals(LocalDate.of(2026, 6, 1), dates.first())
        assertEquals(35, dates.size)
    }
}
