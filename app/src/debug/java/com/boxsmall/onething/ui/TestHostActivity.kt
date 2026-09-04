package com.boxsmall.onething.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.ui.theme.OneThingTheme
import java.time.LocalDate

class TestHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent.getStringExtra(EXTRA_SCREEN) == SCREEN_INTERRUPTED_HOME) {
            val today = LocalDate.now()
            setContent {
                OneThingTheme {
                    HomeScreen(
                        goal = GoalSnapshot(
                            id = 1,
                            name = "每天走路",
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
                }
            }
        }
    }

    private companion object {
        const val EXTRA_SCREEN = "screen"
        const val SCREEN_INTERRUPTED_HOME = "interrupted"
    }
}
