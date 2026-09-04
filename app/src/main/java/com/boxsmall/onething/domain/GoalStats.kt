package com.boxsmall.onething.domain

data class GoalStats(
    val totalCompletedDays: Int,
    val currentStreak: Int,
    val longestStreak: Int,
)
