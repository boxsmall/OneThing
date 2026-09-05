package com.boxsmall.onething.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boxsmall.onething.OneThingApplication
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompletionStateDeviceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val app = context.applicationContext as OneThingApplication

    @Before
    fun setUp() {
        runBlocking {
            endActiveGoalIfPresent()
            app.container.goalRepository.createGoal("完成状态顺序测试")
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            endActiveGoalIfPresent()
        }
    }

    @Test
    fun completionIsPersistedBeforeAnimationAndIsNotReplayedAfterRecreation() = runBlocking {
        val viewModel = MainViewModel(app.container)
        viewModel.completeToday()

        val celebratingState = withTimeout(5_000) {
            viewModel.uiState.filter { it.showCelebration }.first()
        }
        val today = celebratingState.currentDate
        val persistedAtAnimationStart = withTimeout(5_000) {
            app.container.goalRepository.observeActiveGoal()
                .filter { goal -> goal != null && today in goal.completionDates }
                .first()
        }
        assertTrue(today in requireNotNull(persistedAtAnimationStart).completionDates)
        assertTrue(celebratingState.celebrationStreak == 1)

        viewModel.dismissCelebration()
        viewModel.completeToday()
        val stored = withTimeout(5_000) {
            app.container.goalRepository.observeActiveGoal().filter { it != null }.first()
        }
        assertTrue(today in requireNotNull(stored).completionDates)

        val recreated = MainViewModel(app.container)
        val recreatedState = withTimeout(5_000) {
            recreated.uiState.filter { it.activeGoal != null }.first()
        }
        assertFalse(recreatedState.showCelebration)
        assertTrue(today in requireNotNull(recreatedState.activeGoal).completionDates)
    }

    private suspend fun endActiveGoalIfPresent() {
        if (app.container.goalRepository.observeActiveGoal().first() != null) {
            app.container.goalRepository.endActiveGoal()
        }
    }
}
