package com.boxsmall.onething.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.ui.theme.OneThingTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class AdaptiveLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun welcomeAndCreateActionsAreReachableAt320DpWithLargeText() {
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 320.dp, height = 480.dp, fontScale = 1.5f) {
                    var showWelcome by remember { mutableStateOf(true) }
                    if (showWelcome) {
                        WelcomeScreen(busy = false, onContinue = { showWelcome = false })
                    } else {
                        CreateGoalScreen(
                            busy = false,
                            hasHistory = true,
                            initialReminderEnabled = true,
                            initialReminderHour = 20,
                            initialReminderMinute = 0,
                            onCreate = { _, _, _, _, _ -> },
                            onHistory = {},
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("开始使用").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithText("你想坚持什么？").assertIsDisplayed()
        composeRule.onNodeWithText("开始坚持").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("查看我的历史 ›").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun homeActionsAreReachableInShortLandscapeContainer() {
        val today = LocalDate.now()
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 360.dp, height = 320.dp, fontScale = 1.3f) {
                    HomeScreen(
                        goal = activeGoal(today),
                        today = today,
                        busy = false,
                        onComplete = {},
                        onSettings = {},
                        onRecord = {},
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription("设置").assertIsDisplayed()
        composeRule.onNodeWithText("我完成了").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("查看记录 ›").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun recordActionsRemainReachableAt320DpWithLargeText() {
        val today = LocalDate.now()
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 320.dp, height = 480.dp, fontScale = 1.5f) {
                    RecordScreen(goal = activeGoal(today), today = today, onBack = {})
                }
            }
        }

        composeRule.onNodeWithText("我的坚持").assertIsDisplayed()
        composeRule.onNodeWithText("返回首页").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsActionsRemainReachableInShortLandscapeContainer() {
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 360.dp, height = 320.dp, fontScale = 1.3f) {
                    SettingsScreen(
                        currentGoalName = "走路 20 分钟",
                        currentGoalIcon = com.boxsmall.onething.domain.GoalIconKey.WALK,
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

        composeRule.onNodeWithText("设置").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("结束这件事"))
        composeRule.onNodeWithText("结束这件事").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("返回首页"))
        composeRule.onNodeWithText("返回首页").assertIsDisplayed()
    }

    @Test
    fun historyReturnActionRemainsReachableWithLargeCards() {
        val today = LocalDate.now()
        val history = (1L..3L).map { id ->
            GoalSnapshot(
                id = id,
                name = "已结束的目标 $id",
                startDate = today.minusDays(40),
                endDate = today.minusDays(id),
                completionDates = setOf(today.minusDays(id + 1), today.minusDays(id)),
            )
        }
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 320.dp, height = 480.dp, fontScale = 1.5f) {
                    HistoryScreen(
                        history = history,
                        today = today,
                        returnLabel = "返回设置",
                        onBack = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("我的历史").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("返回设置"))
        composeRule.onNodeWithText("返回设置").assertIsDisplayed()
    }

    @Test
    fun endResultPrimaryActionRemainsReachableInShortContainer() {
        val today = LocalDate.now()
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 320.dp, height = 480.dp, fontScale = 1.5f) {
                    EndResultScreen(
                        goal = activeGoal(today).copy(endDate = today),
                        today = today,
                        onStartNext = {},
                        onHistory = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("你完成了一段坚持").assertIsDisplayed()
        composeRule.onNodeWithText("开始下一件事").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("查看我的历史 ›").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun endGoalConfirmationActionsRemainReachableWithLargeText() {
        val today = LocalDate.now()
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 320.dp, height = 480.dp, fontScale = 1.5f) {
                    EndGoalConfirmationScreen(
                        goal = activeGoal(today),
                        today = today,
                        busy = false,
                        onConfirm = {},
                        onDismiss = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("这件事坚持了 1 天").assertIsDisplayed()
        composeRule.onNodeWithText("继续坚持").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun completionCardFitsCompactLargeTextContainer() {
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 320.dp, height = 480.dp, fontScale = 1.5f) {
                    CompletionOverlay(currentStreak = 8, forceFallback = true, onDismiss = {})
                }
            }
        }

        composeRule.onNodeWithText("太棒了！").assertIsDisplayed()
        composeRule.onNodeWithText("连续第 8 天").assertIsDisplayed()
        composeRule.onNodeWithText("明天继续。").assertIsDisplayed()
    }

    @Composable
    private fun AdaptiveFrame(
        width: Dp,
        height: Dp,
        fontScale: Float,
        content: @Composable () -> Unit,
    ) {
        val currentDensity = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(currentDensity.density, fontScale),
        ) {
            Box(Modifier.requiredSize(width, height)) { content() }
        }
    }

    private fun activeGoal(today: LocalDate) = GoalSnapshot(
        id = 1,
        name = "走路 20 分钟",
        startDate = today,
        endDate = null,
        completionDates = emptySet(),
    )
}
