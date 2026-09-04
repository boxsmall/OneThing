package com.boxsmall.onething

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OneThingApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            combine(
                container.settingsStore.settings,
                container.goalRepository.observeActiveGoal(),
            ) { settings, activeGoal -> settings to activeGoal }
                .collectLatest { (settings, activeGoal) ->
                    container.applyReminderState(settings, activeGoal)
                }
        }
    }
}
