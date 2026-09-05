package com.boxsmall.onething

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.boxsmall.onething.notification.ReminderReceiver
import com.boxsmall.onething.ui.MainViewModel
import com.boxsmall.onething.ui.OneThingApp
import com.boxsmall.onething.ui.theme.OneThingTheme

class MainActivity : ComponentActivity() {
    private var reminderOpenGeneration by mutableIntStateOf(0)

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as OneThingApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .scaleX(1.035f)
                .scaleY(1.035f)
                .setDuration(200L)
                .withEndAction(provider::remove)
                .start()
        }
        if (ReminderReceiver.isOpenReminderIntent(intent)) reminderOpenGeneration++
        enableEdgeToEdge()
        setContent {
            OneThingTheme {
                OneThingApp(
                    viewModel = viewModel,
                    reminderOpenGeneration = reminderOpenGeneration,
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (ReminderReceiver.isOpenReminderIntent(intent)) reminderOpenGeneration++
    }
}
