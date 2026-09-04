package com.boxsmall.onething.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalStatsCalculatorTest {
    private val today = LocalDate.of(2026, 9, 2)

    @Test
    fun emptyDatesReturnZeroStats() {
        assertEquals(
            GoalStats(0, 0, 0),
            GoalStatsCalculator.calculate(emptySet(), today),
        )
    }

    @Test
    fun completedTodayCountsCurrentStreak() {
        val dates = setOf(today.minusDays(2), today.minusDays(1), today)

        assertEquals(
            GoalStats(totalCompletedDays = 3, currentStreak = 3, longestStreak = 3),
            GoalStatsCalculator.calculate(dates, today),
        )
    }

    @Test
    fun yesterdayKeepsStreakAliveBeforeTodayIsCompleted() {
        val dates = setOf(today.minusDays(3), today.minusDays(2), today.minusDays(1))

        assertEquals(3, GoalStatsCalculator.calculate(dates, today).currentStreak)
    }

    @Test
    fun missingYesterdayBreaksCurrentStreak() {
        val dates = setOf(today.minusDays(4), today.minusDays(3), today.minusDays(2))

        assertEquals(0, GoalStatsCalculator.calculate(dates, today).currentStreak)
    }

    @Test
    fun longestStreakIsComputedAcrossGaps() {
        val dates = setOf(
            today.minusDays(10),
            today.minusDays(9),
            today.minusDays(5),
            today.minusDays(4),
            today.minusDays(3),
            today,
        )

        val stats = GoalStatsCalculator.calculate(dates, today)

        assertEquals(6, stats.totalCompletedDays)
        assertEquals(1, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }
}
