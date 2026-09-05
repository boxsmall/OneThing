package com.boxsmall.onething.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.domain.GoalIconKey
import com.boxsmall.onething.ui.theme.OneThingTheme
import java.time.LocalDate

class TestHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val screen = intent.getStringExtra(EXTRA_SCREEN)
        val today = LocalDate.now()
        if (screen != null) {
            setContent {
                OneThingTheme {
                    when (screen) {
                        SCREEN_INTERRUPTED_HOME -> HomeScreen(
                            goal = GoalSnapshot(
                                id = 1,
                                name = "每天走路",
                                iconKey = GoalIconKey.WALK,
                                startDate = today.minusDays(4),
                                endDate = null,
                                completionDates = setOf(today.minusDays(4)),
                            ),
                            today = today,
                            busy = false,
                            onComplete = {},
                            onSettings = {},
                            onRecord = {},
                        )
                        SCREEN_CREATE -> CreateGoalScreen(
                            busy = false,
                            hasHistory = true,
                            initialReminderEnabled = true,
                            initialReminderHour = 20,
                            initialReminderMinute = 0,
                            onCreate = { _, _, _, _, _ -> },
                            onHistory = {},
                        )
                        SCREEN_COMPLETION -> CompletionOverlay(
                            currentStreak = 7,
                            onDismiss = {},
                            animationsDisabledOverride = false,
                            previewProgress = .78f,
                        )
                        SCREEN_SETTINGS -> SettingsScreen(
                            currentGoalName = "走路 20 分钟",
                            currentGoalIcon = GoalIconKey.WALK,
                            reminderEnabled = true,
                            reminderHour = 20,
                            reminderMinute = 0,
                            busy = false,
                            onRenameGoal = {},
                            onGoalIconChange = {},
                            onReminderChange = { _, _, _ -> },
                            onHistory = {},
                            onAbout = {},
                            onEndGoal = {},
                            onBack = {},
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val SCREEN_INTERRUPTED_HOME = "interrupted"
        const val SCREEN_CREATE = "create"
        const val SCREEN_COMPLETION = "completion"
        const val SCREEN_SETTINGS = "settings"
    }
}
