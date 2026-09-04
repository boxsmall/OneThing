package com.boxsmall.onething.domain

import java.time.LocalDate

object GoalStatsCalculator {
    fun calculate(completionDates: Set<LocalDate>, today: LocalDate): GoalStats {
        if (completionDates.isEmpty()) {
            return GoalStats(totalCompletedDays = 0, currentStreak = 0, longestStreak = 0)
        }

        val sortedDates = completionDates.sorted()
        var longest = 1
        var running = 1
        for (index in 1 until sortedDates.size) {
            if (sortedDates[index] == sortedDates[index - 1].plusDays(1)) {
                running += 1
                longest = maxOf(longest, running)
            } else {
                running = 1
            }
        }

        val currentAnchor = when {
            today in completionDates -> today
            today.minusDays(1) in completionDates -> today.minusDays(1)
            else -> null
        }
        var current = 0
        var cursor = currentAnchor
        while (cursor != null && cursor in completionDates) {
            current += 1
            cursor = cursor.minusDays(1)
        }

        return GoalStats(
            totalCompletedDays = completionDates.size,
            currentStreak = current,
            longestStreak = longest,
        )
    }
}
