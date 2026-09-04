package com.boxsmall.onething.domain

import java.time.LocalDate

data class GoalSnapshot(
    val id: Long,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val completionDates: Set<LocalDate>,
) {
    val isActive: Boolean get() = endDate == null
}
