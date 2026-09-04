package com.boxsmall.onething.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

class SettingsStore(
    private val context: Context,
) {
    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
            reminderEnabled = preferences[Keys.REMINDER_ENABLED] ?: true,
            reminderHour = preferences[Keys.REMINDER_HOUR] ?: 20,
            reminderMinute = preferences[Keys.REMINDER_MINUTE] ?: 0,
        )
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        require(hour in 0..23)
        require(minute in 0..59)
        context.dataStore.edit {
            it[Keys.REMINDER_ENABLED] = enabled
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }
}
