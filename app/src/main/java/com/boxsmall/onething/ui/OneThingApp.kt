package com.boxsmall.onething.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.boxsmall.onething.data.local.GoalEntity
import com.boxsmall.onething.BuildConfig
import com.boxsmall.onething.domain.GoalProgressCalculator
import com.boxsmall.onething.domain.GoalProgressKind
import com.boxsmall.onething.domain.GoalIconKey
import com.boxsmall.onething.domain.GoalSnapshot
import com.boxsmall.onething.domain.GoalStatsCalculator
import com.boxsmall.onething.domain.GoalNamePolicy
import com.boxsmall.onething.domain.MonthGrid
import com.boxsmall.onething.domain.WeekWindow
import com.boxsmall.onething.domain.millisecondsUntilNextLocalDay
import com.boxsmall.onething.ui.theme.BrandGreen
import com.boxsmall.onething.ui.theme.BrandLogoYellow
import com.boxsmall.onething.ui.theme.BrandYellow
import com.boxsmall.onething.ui.theme.Danger
import com.boxsmall.onething.ui.theme.Hairline
import com.boxsmall.onething.ui.theme.Ink
import com.boxsmall.onething.ui.theme.MutedInk
import com.boxsmall.onething.ui.theme.Paper
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

@Composable
fun OneThingApp(
    viewModel: MainViewModel,
    reminderOpenGeneration: Int = 0,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var page by rememberSaveable { mutableStateOf(AppPage.HOME) }
    var historyReturnPage by rememberSaveable { mutableStateOf(AppPage.HOME) }
    var endedGoalId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showEndConfirmation by rememberSaveable { mutableStateOf(false) }
    var showNotificationSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val backTarget = backDestination(page, historyReturnPage)

    SystemDateRefreshEffect(onRefresh = viewModel::refreshSystemState)
    LaunchedEffect(reminderOpenGeneration) {
        if (reminderOpenGeneration > 0) {
            page = AppPage.HOME
            endedGoalId = null
            showEndConfirmation = false
        }
    }
    BackHandler(enabled = showEndConfirmation || state.showCelebration || backTarget != null) {
        if (showEndConfirmation) {
            showEndConfirmation = false
        } else if (state.showCelebration) {
            viewModel.dismissCelebration()
        } else {
            page = requireNotNull(backTarget)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                viewModel.updateReminder(
                    enabled = false,
                    hour = state.settings?.reminderHour ?: 20,
                    minute = state.settings?.reminderMinute ?: 0,
                )
                showNotificationSettingsDialog = true
            }
        },
    )

    LaunchedEffect(
        state.activeGoal?.id,
        state.settings?.reminderEnabled,
        state.settings?.onboardingCompleted,
    ) {
        val shouldAsk = Build.VERSION.SDK_INT >= 33 &&
            state.activeGoal != null &&
            state.settings?.onboardingCompleted == true &&
            state.settings?.reminderEnabled == true &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (shouldAsk) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            when {
                state.settings == null -> LoadingScreen()
                state.settings?.onboardingCompleted == false -> WelcomeScreen(
                    busy = state.operationInProgress,
                    onContinue = viewModel::completeOnboarding,
                )
                page == AppPage.END_RESULT -> {
                    val endedGoal = state.history.firstOrNull { it.id == endedGoalId }
                    if (endedGoal == null) {
                        LoadingScreen()
                    } else {
                        EndResultScreen(
                            goal = endedGoal,
                            today = state.currentDate,
                            onStartNext = {
                                endedGoalId = null
                                page = AppPage.HOME
                            },
                            onHistory = {
                                historyReturnPage = AppPage.END_RESULT
                                page = AppPage.HISTORY
                            },
                        )
                    }
                }
                state.activeGoal == null && page == AppPage.HOME -> CreateGoalScreen(
                    busy = state.operationInProgress,
                    hasHistory = state.history.isNotEmpty(),
                    initialReminderEnabled = state.settings?.reminderEnabled ?: true,
                    initialReminderHour = state.settings?.reminderHour ?: 20,
                    initialReminderMinute = state.settings?.reminderMinute ?: 0,
                    onCreate = viewModel::createGoal,
                    onHistory = {
                        historyReturnPage = AppPage.HOME
                        page = AppPage.HISTORY
                    },
                )
                page == AppPage.HISTORY -> HistoryScreen(
                    history = state.history,
                    today = state.currentDate,
                    returnLabel = when (historyReturnPage) {
                        AppPage.SETTINGS -> "返回设置"
                        AppPage.END_RESULT -> "返回结束结果"
                        else -> "返回创建目标"
                    },
                    onBack = { page = historyReturnPage },
                )
                page == AppPage.RECORD -> RecordScreen(
                    goal = requireNotNull(state.activeGoal),
                    today = state.currentDate,
                    onBack = { page = AppPage.HOME },
                )
                page == AppPage.SETTINGS -> SettingsScreen(
                    currentGoalName = state.activeGoal?.name.orEmpty(),
                    currentGoalIcon = state.activeGoal?.iconKey ?: GoalIconKey.OTHER,
                    reminderEnabled = state.settings?.reminderEnabled ?: true,
                    reminderHour = state.settings?.reminderHour ?: 20,
                    reminderMinute = state.settings?.reminderMinute ?: 0,
                    busy = state.operationInProgress,
                    onRenameGoal = viewModel::renameActiveGoal,
                    onGoalIconChange = viewModel::updateActiveGoalIcon,
                    onReminderChange = viewModel::updateReminder,
                    onHistory = {
                        historyReturnPage = AppPage.SETTINGS
                        page = AppPage.HISTORY
                    },
                    onAbout = { page = AppPage.ABOUT },
                    onEndGoal = { showEndConfirmation = true },
                    onBack = { page = AppPage.HOME },
                    showReminderReliabilityGuidance = isXiaomiFamilyDevice(),
                    onOpenSystemSettings = { openApplicationSettings(context) },
                )
                page == AppPage.ABOUT -> AboutScreen(
                    onBack = { page = AppPage.SETTINGS },
                )
                else -> HomeScreen(
                    goal = requireNotNull(state.activeGoal),
                    today = state.currentDate,
                    busy = state.operationInProgress,
                    onComplete = viewModel::completeToday,
                    onSettings = { page = AppPage.SETTINGS },
                    onRecord = { page = AppPage.RECORD },
                )
            }

            if (state.showCelebration) {
                val streak = state.celebrationStreak ?: state.activeGoal?.let { goal ->
                    GoalStatsCalculator.calculate(goal.completionDates, state.currentDate).currentStreak
                } ?: 1
                CompletionOverlay(
                    currentStreak = streak,
                    onDismiss = viewModel::dismissCelebration,
                )
            }

            if (showEndConfirmation) {
                state.activeGoal?.let { goal ->
                    EndGoalConfirmationScreen(
                        goal = goal,
                        today = state.currentDate,
                        busy = state.operationInProgress,
                        onConfirm = {
                            val goalId = state.activeGoal?.id
                            showEndConfirmation = false
                            viewModel.endGoal {
                                endedGoalId = goalId
                                page = AppPage.END_RESULT
                            }
                        },
                        onDismiss = { showEndConfirmation = false },
                    )
                }
            }
        }
    }

    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            title = { Text("通知权限未开启") },
            text = { Text("提醒已关闭。你可以在系统设置中允许通知，再回到“一件”重新开启提醒。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationSettingsDialog = false
                        openApplicationSettings(context)
                    },
                ) { Text("打开系统设置") }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationSettingsDialog = false }) {
                    Text("稍后")
                }
            },
            containerColor = Paper,
        )
    }
}

private fun isXiaomiFamilyDevice(): Boolean =
    listOf(Build.MANUFACTURER, Build.BRAND).any { value ->
        value.lowercase(Locale.ROOT) in setOf("xiaomi", "redmi", "poco")
    }

private fun openApplicationSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ),
    )
}

@Composable
private fun SystemDateRefreshEffect(onRefresh: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var clockChangeGeneration by remember { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                clockChangeGeneration += 1
                onRefresh()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(lifecycleOwner, clockChangeGeneration) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            onRefresh()
            while (true) {
                delay(millisecondsUntilNextLocalDay(ZonedDateTime.now()) + 250L)
                onRefresh()
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandGreen)
    }
}

@Composable
internal fun WelcomeScreen(busy: Boolean, onContinue: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(18.dp))
            Text(
                "1.",
                color = BrandGreen,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 72.sp,
            )
            Spacer(Modifier.height(54.dp))
            Text(
                "一件",
                color = Ink,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 48.sp,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "一次，只坚持一件事。",
                color = MutedInk,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(22.dp))
            PrimaryButton(
                text = "开始使用",
                enabled = !busy,
                modifier = Modifier.testTag("welcome-submit"),
                onClick = onContinue,
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
internal fun CreateGoalScreen(
    busy: Boolean,
    hasHistory: Boolean,
    initialReminderEnabled: Boolean,
    initialReminderHour: Int,
    initialReminderMinute: Int,
    onCreate: (String, GoalIconKey, Boolean, Int, Int) -> Unit,
    onHistory: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var selectedIconValue by rememberSaveable { mutableStateOf(GoalIconKey.OTHER.storageValue) }
    var reminderEnabled by rememberSaveable { mutableStateOf(initialReminderEnabled) }
    var reminderHour by rememberSaveable { mutableIntStateOf(initialReminderHour) }
    var reminderMinute by rememberSaveable { mutableIntStateOf(initialReminderMinute) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val normalized = name.trim()
    val focusManager = LocalFocusManager.current
    val count = GoalNamePolicy.visibleLength(normalized)
    val canSubmit = normalized.isNotBlank() && count <= GoalEntity.MAX_NAME_LENGTH && !busy
    val selectedIcon = GoalIconKey.fromStorage(selectedIconValue)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("create-screen"),
        contentAlignment = Alignment.TopCenter,
    ) {
        val bottomGap = (maxHeight - 600.dp).coerceAtLeast(28.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "你想坚持什么？",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(38.dp))
            Text("我想每天", color = MutedInk, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { candidate ->
                    val oneLine = candidate.replace("\n", "").replace("\r", "")
                    if (GoalNamePolicy.visibleLength(oneLine) <= GoalEntity.MAX_NAME_LENGTH) {
                        name = oneLine
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create-name")
                    .semantics { contentDescription = "目标名称，最多 20 个字符" },
                singleLine = true,
                placeholder = { Text("走路 20 分钟") },
                supportingText = { Text("$count / ${GoalEntity.MAX_NAME_LENGTH}") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "选择一个图标",
                modifier = Modifier.fillMaxWidth(),
                color = Ink,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            GoalIconPicker(
                selected = selectedIcon,
                onSelect = { selectedIconValue = it.storageValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal-icon-picker"),
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = .72f))
                    .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                    .toggleable(
                        value = reminderEnabled,
                        role = Role.Switch,
                        onValueChange = { reminderEnabled = it },
                    )
                    .testTag("create-reminder-toggle")
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("提醒我", color = Ink, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (reminderEnabled) "每天提醒一次" else "不发送通知",
                        color = MutedInk,
                        fontSize = 13.sp,
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = null,
                )
            }
            if (reminderEnabled) {
                Spacer(Modifier.height(18.dp))
                Text("什么时候提醒你？", color = MutedInk)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics {
                            contentDescription = "提醒时间，%02d点%02d分".format(
                                reminderHour,
                                reminderMinute,
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                ) {
                    Text(
                        String.format(Locale.ROOT, "%02d:%02d", reminderHour, reminderMinute),
                        color = Ink,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "V1 只记录是否完成，不记录文字内容。",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MutedInk,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(bottomGap))
            PrimaryButton(
                text = "开始坚持",
                enabled = canSubmit,
                modifier = Modifier.testTag("create-submit"),
                onClick = {
                    focusManager.clearFocus()
                    onCreate(normalized, selectedIcon, reminderEnabled, reminderHour, reminderMinute)
                },
            )
            if (hasHistory) {
                TextButton(
                    onClick = onHistory,
                    modifier = Modifier.height(48.dp),
                ) { Text("查看我的历史 ›", color = MutedInk) }
            } else {
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showTimePicker) {
        ReminderTimeDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            busy = busy,
            onConfirm = { hour, minute ->
                reminderHour = hour
                reminderMinute = minute
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
internal fun HomeScreen(
    goal: GoalSnapshot,
    today: LocalDate,
    busy: Boolean,
    onComplete: () -> Unit,
    onSettings: () -> Unit,
    onRecord: () -> Unit,
) {
    val progress = GoalProgressCalculator.calculate(goal, today)
    val completedToday = progress.kind == GoalProgressKind.ACTIVE_DONE
    val interrupted = progress.kind == GoalProgressKind.ACTIVE_INTERRUPTED
    val stats = GoalStatsCalculator.calculate(goal.completionDates, today)
    val dateText = DateTimeFormatter
        .ofPattern("M月d日 EEEE", Locale.SIMPLIFIED_CHINESE)
        .format(today)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home-screen"),
        contentAlignment = Alignment.TopCenter,
    ) {
        val availableHeight = maxHeight
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    dateText,
                    modifier = Modifier.weight(1f),
                    color = Ink,
                    fontSize = 14.sp,
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onSettings)
                        .semantics { contentDescription = "设置" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⚙", color = Ink, fontSize = 27.sp)
                }
            }

            when {
                interrupted -> {
                    val durationDays = ChronoUnit.DAYS.between(goal.startDate, today).toInt() + 1
                    Spacer(Modifier.height(8.dp))
                    GoalIconBadge(goal.iconKey, size = 58.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (progress.gapDays == 1) "昨天没有完成" else "有几天没有继续了",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "过去 $durationDays 天，\n你完成了 ${stats.totalCompletedDays} 天。",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (progress.gapDays == 1) "今天继续。" else "今天回来就好。",
                        modifier = Modifier.fillMaxWidth(),
                        color = BrandGreen,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(28.dp))
                    SevenDayStrip(goal, today, showLabels = false, interrupted = true)
                    Spacer(Modifier.height((availableHeight - 440.dp).coerceIn(170.dp, 300.dp)))
                    PrimaryButton(
                        text = "我完成了",
                        enabled = !busy,
                        animatePress = true,
                        onClick = onComplete,
                    )
                }

                completedToday -> {
                    Text("今天", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(18.dp))
                    GoalIconBadge(goal.iconKey, completed = true, size = 58.dp)
                    Spacer(Modifier.height(26.dp))
                    Text(
                        goal.name,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(Modifier.height(30.dp))
                    Text(
                        "✓",
                        modifier = Modifier.semantics { contentDescription = "完成" },
                        color = BrandGreen,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 64.sp,
                    )
                    Spacer(Modifier.height(30.dp))
                    Text(
                        "今天已完成",
                        color = BrandGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Text(
                        "连续第 ${stats.currentStreak} 天",
                        color = MutedInk,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(24.dp))
                    SevenDayStrip(goal, today, showLabels = false)
                    Spacer(Modifier.height(30.dp))
                    EncouragementCard()
                    Spacer(Modifier.height((availableHeight - 650.dp).coerceIn(58.dp, 170.dp)))
                    RecordAction(onRecord)
                }

                else -> {
                    Spacer(Modifier.height(8.dp))
                    Text("今天", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(18.dp))
                    GoalIconBadge(goal.iconKey, size = 58.dp)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        goal.name,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(Modifier.height(26.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BrandLogoYellow.copy(alpha = .24f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("1.", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stats.currentStreak.toString(),
                        color = Ink,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 66.sp,
                    )
                    Spacer(Modifier.height(22.dp))
                    Text("连续坚持天数", color = MutedInk, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(28.dp))
                    SevenDayStrip(goal, today, showLabels = true)
                    Spacer(Modifier.height((availableHeight - 670.dp).coerceIn(58.dp, 150.dp)))
                    PrimaryButton(
                        text = "我完成了",
                        enabled = !busy,
                        containerColor = BrandLogoYellow,
                        contentColor = Ink,
                        animatePress = true,
                        onClick = onComplete,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("今天只做好这一件事", color = MutedInk, fontSize = 14.sp)
                    RecordAction(onRecord)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SevenDayStrip(
    goal: GoalSnapshot,
    today: LocalDate,
    showLabels: Boolean,
    interrupted: Boolean = false,
) {
    val days = WeekWindow.endingToday(today)
    val weekDay = DateTimeFormatter.ofPattern("E", Locale.SIMPLIFIED_CHINESE)
    val dateLabel = DateTimeFormatter.ofPattern("M月d日", Locale.SIMPLIFIED_CHINESE)
    val progressDescription = days.joinToString(separator = "；", prefix = "最近七天：") { date ->
        val dayState = when {
            date.isBefore(goal.startDate) -> "目标开始前"
            goal.endDate != null && date.isAfter(goal.endDate) -> "目标结束后"
            date in goal.completionDates -> "已完成"
            else -> "未完成"
        }
        val todayLabel = if (date == today) "，今天" else ""
        "${dateLabel.format(date)}$todayLabel，$dayState"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = progressDescription },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { date ->
            val completed = date in goal.completionDates
            val activeDay = !date.isBefore(goal.startDate) && (goal.endDate == null || !date.isAfter(goal.endDate))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (completed) BrandGreen else Color.Transparent)
                        .border(
                            width = if (date == today && completed) 2.dp else 1.dp,
                            color = when {
                                date == today && completed -> Paper
                                date == today && !completed && !interrupted -> BrandYellow
                                activeDay -> BrandGreen
                                else -> Hairline
                            },
                            shape = CircleShape,
                        ),
                )
                if (showLabels) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        weekDay.format(date).removePrefix("周").removePrefix("星期"),
                        color = MutedInk,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun EncouragementCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = .82f))
            .border(1.dp, Hairline, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("太棒了！明天继续。", color = Ink, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecordAction(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
    ) {
        Text("查看记录 ›", color = Ink, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun CompletionFallbackOverlay(
    currentStreak: Int,
    reducedMotion: Boolean = false,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    var dismissalRequested by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.9f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "completionScrimAlpha",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "completionCardAlpha",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.94f,
        animationSpec = tween(durationMillis = 240),
        label = "completionCardScale",
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(durationMillis = 240),
        label = "completionCardOffsetY",
    )

    LaunchedEffect(Unit) {
        visible = true
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        delay(if (reducedMotion) 360 else 960)
        dismissalRequested = true
    }

    LaunchedEffect(dismissalRequested) {
        if (dismissalRequested) {
            visible = false
            delay(if (reducedMotion) 120 else 240)
            onDismiss()
        }
    }

    val requestDismiss = { dismissalRequested = true }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("completion-overlay")
            .semantics {
                contentDescription = "完成反馈。今天完成，连续第 $currentStreak 天，明天继续。"
            }
            .background(Ink.copy(alpha = scrimAlpha))
            .clickable(
                enabled = visible && !dismissalRequested,
                role = Role.Button,
                onClick = requestDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = cardAlpha
                    scaleX = cardScale
                    scaleY = cardScale
                    translationY = cardOffsetY
                }
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "✓",
                modifier = Modifier.semantics { contentDescription = "完成" },
                color = BrandGreen,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 64.sp,
            )
            Spacer(Modifier.height(22.dp))
            Text("太棒了！", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(24.dp))
            Text("今天完成", color = Ink, fontWeight = FontWeight.Bold)
            Text(
                "连续第 $currentStreak 天",
                color = Ink,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text("明天继续。", color = MutedInk, fontSize = 13.sp)
        }
    }
}

@Composable
internal fun SettingsScreen(
    currentGoalName: String,
    currentGoalIcon: GoalIconKey,
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    busy: Boolean,
    onRenameGoal: (String) -> Unit,
    onGoalIconChange: (GoalIconKey) -> Unit,
    onReminderChange: (Boolean, Int, Int) -> Unit,
    onHistory: () -> Unit,
    onAbout: () -> Unit,
    onEndGoal: () -> Unit,
    onBack: () -> Unit,
    showReminderReliabilityGuidance: Boolean = false,
    onOpenSystemSettings: () -> Unit = {},
) {
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showIconDialog by rememberSaveable { mutableStateOf(false) }
    var showTimeDialog by rememberSaveable { mutableStateOf(false) }
    var showReminderReliabilityDialog by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .testTag("settings-list"),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { ScreenHeader("设置", onBack) }
            item { Spacer(Modifier.height(2.dp)) }
            item {
                SettingsCard(
                title = "当前坚持",
                value = currentGoalName,
                enabled = !busy,
                onClick = { showRenameDialog = true },
            )
            }
            item {
                SettingsCard(
                    title = "目标图标",
                    value = currentGoalIcon.presentation().label,
                    enabled = !busy,
                    onClick = { showIconDialog = true },
                    leading = {
                        GoalIconBadge(
                            iconKey = currentGoalIcon,
                            size = 38.dp,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    },
                )
            }
            item {
                SettingsCard(
                title = "提醒时间",
                value = "每天 %02d:%02d".format(reminderHour, reminderMinute),
                enabled = reminderEnabled && !busy,
                onClick = { showTimeDialog = true },
            )
            }
            item {
                SettingsCard(
                    title = "提醒通知",
                    value = if (reminderEnabled) "已开启" else "已关闭",
                    trailing = {
                        Switch(
                            checked = reminderEnabled,
                            enabled = !busy,
                            modifier = Modifier.semantics {
                                contentDescription = "提醒通知"
                            },
                            onCheckedChange = { enabled ->
                                onReminderChange(enabled, reminderHour, reminderMinute)
                            },
                        )
                    },
                )
            }
            if (showReminderReliabilityGuidance && reminderEnabled) {
                item {
                    SettingsCard(
                        title = "系统提醒保障",
                        value = "检查自启动与省电策略",
                        enabled = !busy,
                        onClick = { showReminderReliabilityDialog = true },
                    )
                }
            }
            item { SettingsCard(title = "我的历史", onClick = onHistory) }
            item {
                SettingsCard(
                    title = "结束这件事",
                    titleColor = Danger,
                    enabled = !busy,
                    onClick = onEndGoal,
                )
            }
            item {
                SettingsCard(
                    title = "关于一件",
                    value = "版本 ${BuildConfig.VERSION_NAME}",
                    onClick = onAbout,
                )
            }
            item {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text("返回首页", color = Ink, fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    if (showRenameDialog) {
        RenameGoalDialog(
            currentName = currentGoalName,
            busy = busy,
            onConfirm = { name ->
                onRenameGoal(name)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showIconDialog) {
        GoalIconDialog(
            currentIcon = currentGoalIcon,
            busy = busy,
            onConfirm = { iconKey ->
                onGoalIconChange(iconKey)
                showIconDialog = false
            },
            onDismiss = { showIconDialog = false },
        )
    }

    if (showTimeDialog) {
        ReminderTimeDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            busy = busy,
            onConfirm = { hour, minute ->
                onReminderChange(true, hour, minute)
                showTimeDialog = false
            },
            onDismiss = { showTimeDialog = false },
        )
    }

    if (showReminderReliabilityDialog) {
        AlertDialog(
            onDismissRequest = { showReminderReliabilityDialog = false },
            title = { Text("确保提醒准时") },
            text = {
                Text(
                    "在小米、Redmi 或 POCO 设备上，请在系统应用信息中开启“自启动”，并将省电策略设为“无限制”。否则 HyperOS 可能把提醒延后数天。",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReminderReliabilityDialog = false
                        onOpenSystemSettings()
                    },
                ) { Text("打开系统设置") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderReliabilityDialog = false }) {
                    Text("稍后")
                }
            },
            containerColor = Paper,
        )
    }
}

@Composable
internal fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .testTag("about-list"),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { ScreenHeader("关于一件", onBack) }
            item {
                Spacer(Modifier.height(12.dp))
                AboutCard(title = "一件 ${BuildConfig.VERSION_NAME}") {
                    Text(
                        "一次只做好一件事。V1 完全离线，不提供账号、云同步或文字记录。",
                        color = MutedInk,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item {
                AboutCard(title = "隐私说明") {
                    Text(
                        "应用不收集、不上传目标、完成记录或设备信息，也不包含网络权限。目标、完成记录和提醒设置仅保存在设备本地。",
                        color = MutedInk,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Android 设备到设备迁移可能复制本地目标、完成记录和非敏感设置；通知权限和系统提醒任务不会迁移，应用会按新设备状态重新调度。卸载应用会删除本机数据。",
                        color = MutedInk,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item {
                AboutCard(title = "开源许可") {
                    Text(
                        "本项目采用 Apache License 2.0。完整许可证随应用源代码保存在仓库根目录。",
                        color = MutedInk,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Hairline, RoundedCornerShape(18.dp))
                        .clickable(role = Role.Button) {
                            runCatching { uriHandler.openUri("https://github.com/boxsmall/OneThing") }
                        }
                        .semantics { contentDescription = "在浏览器中打开 GitHub 项目" }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("GitHub 项目", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(3.dp))
                        Text("github.com/boxsmall/OneThing", color = MutedInk, fontSize = 13.sp)
                    }
                    Text(
                        "›",
                        modifier = Modifier.clearAndSetSemantics {},
                        color = MutedInk,
                        fontSize = 28.sp,
                    )
                }
            }
            item {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text("返回设置", color = Ink, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun AboutCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Hairline, RoundedCornerShape(18.dp))
            .padding(18.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(9.dp))
        content()
    }
}

@Composable
private fun RenameGoalDialog(
    currentName: String,
    busy: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable(currentName) { mutableStateOf(currentName) }
    val normalized = name.trim()
    val count = GoalNamePolicy.visibleLength(normalized)
    val canSave = normalized.isNotBlank() &&
        count <= GoalEntity.MAX_NAME_LENGTH &&
        normalized != currentName &&
        !busy

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改当前目标") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { candidate ->
                    val oneLine = candidate.replace("\n", "").replace("\r", "")
                    if (GoalNamePolicy.visibleLength(oneLine) <= GoalEntity.MAX_NAME_LENGTH) {
                        name = oneLine
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("目标名称") },
                supportingText = { Text("$count / ${GoalEntity.MAX_NAME_LENGTH}") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(16.dp),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(normalized) }, enabled = canSave) {
                Text("保存")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        containerColor = Paper,
    )
}

@Composable
private fun GoalIconDialog(
    currentIcon: GoalIconKey,
    busy: Boolean,
    onConfirm: (GoalIconKey) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedValue by rememberSaveable(currentIcon) { mutableStateOf(currentIcon.storageValue) }
    val selected = GoalIconKey.fromStorage(selectedValue)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改目标图标") },
        text = {
            GoalIconPicker(
                selected = selected,
                onSelect = { selectedValue = it.storageValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings-goal-icon-picker"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selected) },
                enabled = !busy && selected != currentIcon,
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        containerColor = Paper,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    busy: Boolean,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置提醒时间") },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pickerState.hour, pickerState.minute) },
                enabled = !busy,
            ) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        containerColor = Paper,
    )
}

@Composable
internal fun RecordScreen(goal: GoalSnapshot, today: LocalDate, onBack: () -> Unit) {
    val currentMonth = YearMonth.from(today)
    var monthText by rememberSaveable(goal.id) { mutableStateOf(currentMonth.toString()) }
    LaunchedEffect(currentMonth) { monthText = currentMonth.toString() }
    val month = runCatching { YearMonth.parse(monthText) }.getOrDefault(currentMonth)
    val stats = GoalStatsCalculator.calculate(goal.completionDates, today)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
        ) {
        item { ScreenHeader("我的坚持", onBack) }
        item {
            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GoalIconBadge(goal.iconKey, size = 48.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    goal.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonthArrow("‹", "上个月", enabled = true) {
                    monthText = month.minusMonths(1).toString()
                }
                Text(
                    "${month.year}年${month.monthValue}月",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
                MonthArrow("›", "下个月", enabled = month < currentMonth) {
                    monthText = month.plusMonths(1).toString()
                }
            }
            Spacer(Modifier.height(14.dp))
            MonthCalendar(goal = goal, month = month, today = today)
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Hairline, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Metric("当前连续", "${stats.currentStreak} 天")
                Metric("累计完成", "${stats.totalCompletedDays} 天")
                Metric("最长连续", "${stats.longestStreak} 天")
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "开始于 ${DateTimeFormatter.ofPattern("yyyy年M月d日").format(goal.startDate)}",
                modifier = Modifier.fillMaxWidth(),
                color = MutedInk,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                goal.name,
                modifier = Modifier.fillMaxWidth(),
                color = MutedInk,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("返回首页", color = Ink, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun MonthArrow(
    label: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            modifier = Modifier.clearAndSetSemantics {},
            color = MutedInk.copy(alpha = if (enabled) 1f else .3f),
            fontSize = 30.sp,
        )
    }
}

@Composable
private fun MonthCalendar(goal: GoalSnapshot, month: YearMonth, today: LocalDate) {
    val dates = remember(month) { MonthGrid.dates(month) }
    val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            weekLabels.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    color = MutedInk,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        dates.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    CalendarDay(
                        date = date,
                        goal = goal,
                        today = today,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    goal: GoalSnapshot,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        if (date == null) return@Box
        val completed = date in goal.completionDates
        val withinGoal = !date.isBefore(goal.startDate) &&
            (goal.endDate == null || !date.isAfter(goal.endDate)) &&
            !date.isAfter(today)
        val missed = withinGoal && date.isBefore(today) && !completed
        val shape = CircleShape
        val dateDescription = DateTimeFormatter
            .ofPattern("M月d日", Locale.SIMPLIFIED_CHINESE)
            .format(date)
        val dayState = when {
            date == today && completed -> "今天，已完成"
            date == today -> "今天，未完成"
            completed -> "已完成"
            date.isBefore(goal.startDate) -> "目标开始前"
            goal.endDate != null && date.isAfter(goal.endDate) -> "目标结束后"
            date.isAfter(today) -> "未来日期"
            missed -> "未完成"
            else -> "未完成"
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clearAndSetSemantics {
                    contentDescription = "$dateDescription，$dayState"
                }
                .clip(shape)
                .background(
                    when {
                        completed -> BrandGreen
                        else -> Color.Transparent
                    },
                )
                .border(
                    width = when {
                        date == today && !completed -> 2.dp
                        missed -> 1.dp
                        else -> 0.dp
                    },
                    color = when {
                        date == today && !completed -> BrandGreen
                        missed -> Hairline
                        else -> Color.Transparent
                    },
                    shape = shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                date.dayOfMonth.toString(),
                color = when {
                    completed -> Paper
                    missed -> MutedInk
                    withinGoal || date == today -> Ink
                    else -> MutedInk.copy(alpha = .38f)
                },
                fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
internal fun EndResultScreen(
    goal: GoalSnapshot,
    today: LocalDate,
    onStartNext: () -> Unit,
    onHistory: () -> Unit,
) {
    val endDate = goal.endDate ?: today
    val durationDays = ChronoUnit.DAYS.between(goal.startDate, endDate).toInt() + 1
    val completedDays = goal.completionDates.size
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val availableHeight = maxHeight
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(42.dp))
            Text("✓", color = BrandGreen, fontSize = 76.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(28.dp))
            Text("你完成了一段坚持", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Text(
                goal.name,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                if (completedDays == 0) "这段记录已保存" else "$durationDays 天里，完成了 $completedDays 天",
                color = Ink,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height((availableHeight - 540.dp).coerceIn(80.dp, 300.dp)))
            PrimaryButton(text = "开始下一件事", enabled = true, onClick = onStartNext)
            TextButton(
                onClick = onHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("查看我的历史 ›", color = Ink, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
internal fun HistoryScreen(
    history: List<GoalSnapshot>,
    today: LocalDate,
    returnLabel: String,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { ScreenHeader("我的历史", onBack) }
            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight(.65f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("还没有结束过的目标", color = MutedInk)
                    }
                }
            } else {
                items(history, key = { it.id }) { goal -> HistoryCard(goal, today) }
            }
            item {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text(returnLabel, color = Ink, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun HistoryCard(goal: GoalSnapshot, today: LocalDate) {
    val stats = GoalStatsCalculator.calculate(goal.completionDates, goal.endDate ?: today)
    val format = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val endDate = goal.endDate ?: today
    val durationDays = ChronoUnit.DAYS.between(goal.startDate, endDate).toInt() + 1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Hairline, RoundedCornerShape(18.dp))
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GoalIconBadge(goal.iconKey, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                goal.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "${format.format(goal.startDate)} — ${goal.endDate?.let(format::format).orEmpty()}",
            color = MutedInk,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text("$durationDays 天 · 完成 ${stats.totalCompletedDays} 天", color = MutedInk)
        Spacer(Modifier.height(4.dp))
        Text("最长连续 ${stats.longestStreak} 天", color = BrandGreen, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun EndGoalConfirmationScreen(
    goal: GoalSnapshot,
    today: LocalDate,
    busy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val durationDays = ChronoUnit.DAYS.between(goal.startDate, today).toInt() + 1
    val stats = GoalStatsCalculator.calculate(goal.completionDates, today)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper),
        contentAlignment = Alignment.TopCenter,
    ) {
        val availableHeight = maxHeight
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
                ScreenHeader("结束这件事", onDismiss)
                Spacer(Modifier.height(26.dp))
                Text(
                    goal.name,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(28.dp))
                Text("这件事坚持了 $durationDays 天", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.height(26.dp))
                Text("累计完成 ${stats.totalCompletedDays} 天", color = Ink)
                Text("最长连续 ${stats.longestStreak} 天", color = Ink)
                Spacer(Modifier.height(8.dp))
                Text("结束后，记录会保存在“我的历史”。", color = MutedInk, fontSize = 13.sp)
                Spacer(Modifier.height((availableHeight - 540.dp).coerceIn(80.dp, 280.dp)))
                PrimaryButton(
                    text = "继续坚持",
                    enabled = !busy,
                    containerColor = Color(0xFFC2C2BC),
                    contentColor = Paper,
                    onClick = onDismiss,
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = "结束这件事",
                    enabled = !busy,
                    containerColor = Danger,
                    contentColor = Paper,
                    onClick = onConfirm,
                )
                Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = "返回" },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "‹",
                modifier = Modifier.clearAndSetSemantics {},
                fontSize = 36.sp,
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun SettingsCard(
    title: String,
    value: String? = null,
    titleColor: Color = Ink,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = .82f))
            .border(1.dp, Hairline.copy(alpha = .7f), RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = if (enabled) titleColor else MutedInk.copy(alpha = .55f),
            fontWeight = FontWeight.Medium,
        )
        if (value != null) {
            Text(
                value,
                modifier = Modifier.padding(start = 12.dp),
                color = MutedInk.copy(alpha = if (enabled) 1f else .55f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Text(
                "›",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clearAndSetSemantics {},
                color = MutedInk.copy(alpha = if (enabled) 1f else .4f),
                fontSize = 25.sp,
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = "$label，$value"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = MutedInk, fontSize = 12.sp)
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = BrandGreen,
    contentColor: Color = Paper,
    animatePress: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (animatePress && pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "primaryButtonScale",
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(buttonScale)
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Hairline,
            disabledContentColor = MutedInk,
        ),
        interactionSource = interactionSource,
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}
