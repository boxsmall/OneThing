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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.domain.GoalIconKey
import com.boxsmall.onething.ui.theme.OneThingTheme
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccessibilityLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun welcomeAndAboutRemainCenteredAt600Dp() {
        composeRule.setContent {
            OneThingTheme {
                WideAdaptiveFrame(width = 600.dp, height = 960.dp) {
                    var showAbout by remember { mutableStateOf(false) }
                    if (showAbout) {
                        AboutScreen(onBack = { showAbout = false })
                    } else {
                        WelcomeScreen(busy = false, onContinue = { showAbout = true })
                    }
                }
            }
        }

        val welcomeBounds = composeRule.onNodeWithTag("welcome-submit")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        assertTrue(welcomeBounds.right - welcomeBounds.left <= 473.dp)
        assertTrue(welcomeBounds.left >= 63.dp)
        assertTrue(welcomeBounds.right <= 537.dp)

        composeRule.onNodeWithTag("welcome-submit").performClick()
        val aboutBounds = composeRule.onNodeWithTag("about-list")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        assertTrue(aboutBounds.right - aboutBounds.left <= 521.dp)
        assertTrue(aboutBounds.left >= 39.dp)
        assertTrue(aboutBounds.right <= 561.dp)
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("返回设置"))
        composeRule.onNodeWithText("返回设置").assertIsDisplayed()
    }

    @Test
    fun settingsActionsRemainReachableAtTwoTimesFontScale() {
        composeRule.setContent {
            OneThingTheme {
                AdaptiveFrame(width = 320.dp, height = 480.dp, fontScale = 2f) {
                    SettingsScreen(
                        currentGoalName = "每天步行二十分钟",
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
                        showReminderReliabilityGuidance = true,
                        onOpenSystemSettings = {},
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription("返回").assertHasClickAction()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("系统提醒保障"))
        composeRule.onNodeWithText("系统提醒保障").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("结束这件事"))
        composeRule.onNodeWithText("结束这件事").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("返回首页"))
        composeRule.onNodeWithText("返回首页").assertIsDisplayed()
    }

    @Test
    fun allGoalIconsRemainVisibleAt600DpWithLargeText() {
        composeRule.setContent {
            OneThingTheme {
                WideAdaptiveFrame(width = 600.dp, height = 960.dp) {
                    CompositionLocalProvider(
                        LocalDensity provides Density(LocalDensity.current.density, 2f),
                    ) {
                        CreateGoalScreen(
                            busy = false,
                            hasHistory = false,
                            initialReminderEnabled = false,
                            initialReminderHour = 20,
                            initialReminderMinute = 0,
                            onCreate = { _, _, _, _, _ -> },
                            onHistory = {},
                        )
                    }
                }
            }
        }

        GoalIconKey.entries.forEach { icon ->
            composeRule.onNodeWithTag("goal-icon-${icon.storageValue}").assertIsDisplayed()
        }
    }

    @Test
    fun createControlsExposePurposeAndState() {
        composeRule.setContent {
            OneThingTheme {
                CreateGoalScreen(
                    busy = false,
                    hasHistory = false,
                    initialReminderEnabled = true,
                    initialReminderHour = 20,
                    initialReminderMinute = 0,
                    onCreate = { _, _, _, _, _ -> },
                    onHistory = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("目标名称，最多 20 个字符")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("提醒时间，20点00分")
            .assertHasClickAction()
    }

    @Test
    fun homeExposesSevenDayProgressAsOneDescription() {
        val today = LocalDate.of(2026, 9, 3)
        composeRule.setContent {
            OneThingTheme {
                HomeScreen(
                    goal = GoalSnapshot(
                        id = 1,
                        name = "走路 20 分钟",
                        startDate = today.minusDays(1),
                        endDate = null,
                        completionDates = setOf(today.minusDays(1)),
                    ),
                    today = today,
                    busy = false,
                    onComplete = {},
                    onSettings = {},
                    onRecord = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("最近七天：", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("9月2日，已完成", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("9月3日，今天，未完成", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun recordExposesNavigationCalendarAndMetrics() {
        val today = LocalDate.of(2026, 9, 3)
        val goal = GoalSnapshot(
            id = 1,
            name = "走路 20 分钟",
            startDate = today.minusDays(2),
            endDate = null,
            completionDates = setOf(today.minusDays(2), today),
        )
        composeRule.setContent {
            OneThingTheme {
                RecordScreen(goal = goal, today = today, onBack = {})
            }
        }

        composeRule.onNodeWithContentDescription("返回").assertHasClickAction()
        composeRule.onNodeWithContentDescription("上个月").assertHasClickAction()
        composeRule.onNodeWithContentDescription("下个月").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("9月1日，已完成").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("9月2日，未完成").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("9月3日，今天，已完成").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("当前连续，1 天").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("累计完成，2 天").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("最长连续，1 天").assertIsDisplayed()
    }

    @Test
    fun settingsSwitchAndAboutLinkHaveExplicitLabels() {
        composeRule.setContent {
            OneThingTheme {
                var showAbout by remember { mutableStateOf(false) }
                if (showAbout) {
                    AboutScreen(onBack = { showAbout = false })
                } else {
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
                        onAbout = { showAbout = true },
                        onEndGoal = {},
                        onBack = {},
                        showReminderReliabilityGuidance = true,
                        onOpenSystemSettings = {},
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription("提醒通知").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("系统提醒保障"))
        composeRule.onNodeWithText("系统提醒保障").performClick()
        composeRule.onNodeWithText("确保提醒准时").assertIsDisplayed()
        composeRule.onNodeWithText("打开系统设置").assertHasClickAction().performClick()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("关于一件"))
        composeRule.onNodeWithText("关于一件").performClick()
        composeRule.onNodeWithContentDescription("在浏览器中打开 GitHub 项目")
            .performScrollTo()
            .assertHasClickAction()
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

    @Composable
    private fun WideAdaptiveFrame(
        width: Dp,
        height: Dp,
        content: @Composable () -> Unit,
    ) {
        val windowInfo = LocalWindowInfo.current
        val currentDensity = LocalDensity.current
        val physicalWidthPx = windowInfo.containerSize.width.toFloat()
        val targetDensity = physicalWidthPx / width.value
        CompositionLocalProvider(
            LocalDensity provides Density(targetDensity, currentDensity.fontScale),
        ) {
            Box(Modifier.requiredSize(width, height)) { content() }
        }
    }
}
