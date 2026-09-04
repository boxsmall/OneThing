package com.boxsmall.onething.data.settings

data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
)
