package com.boxsmall.onething.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalProgressCalculatorTest {
    private val today = LocalDate.of(2026, 9, 3)

    @Test
    fun newGoalTodayIsUndoneNotInterrupted() {
        val result = GoalProgressCalculator.calculate(goal(startDate = today), today)

        assertEquals(GoalProgress(GoalProgressKind.ACTIVE_UNDONE), result)
    }

    @Test
    fun completedTodayIsDone() {
        val result = GoalProgressCalculator.calculate(
            goal(completions = setOf(today)),
            today,
        )

        assertEquals(GoalProgress(GoalProgressKind.ACTIVE_DONE), result)
    }

    @Test
    fun completedYesterdayKeepsTodayUndone() {
        val result = GoalProgressCalculator.calculate(
            goal(completions = setOf(today.minusDays(1))),
            today,
        )

        assertEquals(GoalProgress(GoalProgressKind.ACTIVE_UNDONE), result)
    }

    @Test
    fun missingYesterdayCreatesOneDayInterruption() {
        val result = GoalProgressCalculator.calculate(
            goal(completions = setOf(today.minusDays(2))),
            today,
        )

        assertEquals(GoalProgress(GoalProgressKind.ACTIVE_INTERRUPTED, gapDays = 1), result)
    }

    @Test
    fun consecutiveMissingDaysAreCountedBackToLastCompletion() {
        val result = GoalProgressCalculator.calculate(
            goal(
                startDate = today.minusDays(8),
                completions = setOf(today.minusDays(5), today.minusDays(4)),
            ),
            today,
        )

        assertEquals(GoalProgress(GoalProgressKind.ACTIVE_INTERRUPTED, gapDays = 3), result)
    }

    @Test
    fun neverCompletedCountsOnlyFromStartThroughYesterday() {
        val result = GoalProgressCalculator.calculate(
            goal(startDate = today.minusDays(3)),
            today,
        )

        assertEquals(GoalProgress(GoalProgressKind.ACTIVE_INTERRUPTED, gapDays = 3), result)
    }

    private fun goal(
        startDate: LocalDate = today.minusDays(10),
        completions: Set<LocalDate> = emptySet(),
    ) = GoalSnapshot(
        id = 1,
        name = "测试目标",
        startDate = startDate,
        endDate = null,
        completionDates = completions,
    )
}
