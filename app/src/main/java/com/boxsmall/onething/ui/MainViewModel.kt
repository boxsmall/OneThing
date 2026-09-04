package com.boxsmall.onething.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.boxsmall.onething.AppContainer
import com.boxsmall.onething.data.settings.AppSettings
import com.boxsmall.onething.domain.GoalSnapshot
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val currentDate: LocalDate,
    val settings: AppSettings? = null,
    val activeGoal: GoalSnapshot? = null,
    val history: List<GoalSnapshot> = emptyList(),
    val operationInProgress: Boolean = false,
    val showCelebration: Boolean = false,
    val message: String? = null,
)

class MainViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val transientState = MutableStateFlow(TransientState())
    private val currentDate = MutableStateFlow(container.dateProvider.today())

    val uiState: StateFlow<MainUiState> = combine(
        container.settingsStore.settings,
        container.goalRepository.observeActiveGoal(),
        container.goalRepository.observeHistory(),
        transientState,
        currentDate,
    ) { settings, activeGoal, history, transient, today ->
        MainUiState(
            currentDate = today,
            settings = settings,
            activeGoal = activeGoal,
            history = history,
            operationInProgress = transient.operationInProgress,
            showCelebration = transient.showCelebration,
            message = transient.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(currentDate = currentDate.value),
    )

    fun refreshSystemState() {
        currentDate.value = container.dateProvider.today()
        viewModelScope.launch { container.refreshReminder() }
    }

    fun completeOnboarding() = launchOperation {
        container.settingsStore.completeOnboarding()
    }

    fun createGoal(
        name: String,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int,
    ) = launchOperation {
        container.settingsStore.updateReminder(
            enabled = reminderEnabled,
            hour = reminderHour,
            minute = reminderMinute,
        )
        container.goalRepository.createGoal(name)
    }

    fun completeToday() = launchOperation {
        if (container.goalRepository.completeToday()) {
            transientState.value = TransientState(showCelebration = true)
        }
    }

    fun dismissCelebration() {
        transientState.value = TransientState()
    }

    fun endGoal(onEnded: () -> Unit = {}) = launchOperation {
        check(container.goalRepository.endActiveGoal()) { "当前目标不存在" }
        onEnded()
    }

    fun renameActiveGoal(name: String) = launchOperation {
        check(container.goalRepository.renameActiveGoal(name)) { "当前目标不存在" }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) = launchOperation {
        container.settingsStore.updateReminder(enabled, hour, minute)
    }

    fun clearMessage() {
        transientState.value = transientState.value.copy(message = null)
    }

    private fun launchOperation(block: suspend () -> Unit) {
        if (transientState.value.operationInProgress) return
        viewModelScope.launch {
            transientState.value = TransientState(operationInProgress = true)
            runCatching { block() }
                .onSuccess {
                    if (!transientState.value.showCelebration) {
                        transientState.value = TransientState()
                    }
                }
                .onFailure { error ->
                    transientState.value = TransientState(
                        message = error.message ?: "操作失败，请重试",
                    )
                }
        }
    }

    private data class TransientState(
        val operationInProgress: Boolean = false,
        val showCelebration: Boolean = false,
        val message: String? = null,
    )

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(container) as T
        }
    }
}
