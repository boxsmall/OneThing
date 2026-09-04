package com.boxsmall.onething.domain

import java.time.LocalDate

enum class GoalProgressKind {
    ACTIVE_UNDONE,
    ACTIVE_DONE,
    ACTIVE_INTERRUPTED,
}

data class GoalProgress(
    val kind: GoalProgressKind,
    val gapDays: Int = 0,
)

object GoalProgressCalculator {
    fun calculate(goal: GoalSnapshot, today: LocalDate): GoalProgress {
        if (today in goal.completionDates) {
            return GoalProgress(GoalProgressKind.ACTIVE_DONE)
        }
        if (!goal.startDate.isBefore(today) || today.minusDays(1) in goal.completionDates) {
            return GoalProgress(GoalProgressKind.ACTIVE_UNDONE)
        }

        var gapDays = 0
        var cursor = today.minusDays(1)
        while (!cursor.isBefore(goal.startDate) && cursor !in goal.completionDates) {
            gapDays += 1
            cursor = cursor.minusDays(1)
        }
        return GoalProgress(
            kind = GoalProgressKind.ACTIVE_INTERRUPTED,
            gapDays = gapDays,
        )
    }
}
