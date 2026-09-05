package com.boxsmall.onething.ui

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.boxsmall.onething.R
import com.boxsmall.onething.ui.theme.BrandGreen
import com.boxsmall.onething.ui.theme.Ink
import com.boxsmall.onething.ui.theme.MutedInk
import com.boxsmall.onething.ui.theme.Paper

@Composable
internal fun CompletionOverlay(
    currentStreak: Int,
    onDismiss: () -> Unit,
    forceFallback: Boolean = false,
    animationsDisabledOverride: Boolean? = null,
    previewProgress: Float? = null,
) {
    val context = LocalContext.current
    val systemAnimationsDisabled = remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
    val animationsDisabled = animationsDisabledOverride ?: systemAnimationsDisabled
    if (forceFallback || animationsDisabled) {
        CompletionFallbackOverlay(
            currentStreak = currentStreak,
            reducedMotion = animationsDisabled,
            onDismiss = onDismiss,
        )
        return
    }

    val requestedResource = completionAnimationResource(currentStreak)
    val requestedResult = rememberLottieComposition(
        LottieCompositionSpec.RawRes(requestedResource),
    )
    val baseResult = rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.complete_success),
    )
    val requestedComposition by requestedResult
    val baseComposition by baseResult
    val composition = requestedComposition ?: if (requestedResult.isFailure) baseComposition else null

    if (requestedResult.isFailure && baseResult.isFailure) {
        CompletionFallbackOverlay(
            currentStreak = currentStreak,
            onDismiss = onDismiss,
        )
        return
    }

    val animatedProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = composition != null && previewProgress == null,
        speed = 1f,
    )
    val progress = previewProgress ?: animatedProgress
    var dismissalSent by remember { mutableStateOf(false) }
    val dismissOnce = remember(onDismiss) {
        {
            if (!dismissalSent) {
                dismissalSent = true
                onDismiss()
            }
        }
    }
    val haptics = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(composition, previewProgress) {
        if (composition != null && previewProgress == null) {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            kotlinx.coroutines.delay(1_800)
            dismissOnce()
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) dismissOnce()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    BackHandler(onBack = dismissOnce)

    val textProgress = ((progress - 59f / 108f) / (16f / 108f)).coerceIn(0f, 1f)
    val exitProgress = ((progress - 99f / 108f) / (9f / 108f)).coerceIn(0f, 1f)
    val message = completionMessage(currentStreak)
    val fullDescription = "完成反馈。今天完成，${message.streakLine}，${message.hint}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("completion-overlay")
            .clearAndSetSemantics { contentDescription = fullDescription }
            .background(Paper)
            .clickable(
                role = Role.Button,
                onClickLabel = "跳过动画",
                onClick = dismissOnce,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer { alpha = 1f - exitProgress },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                if (composition == null) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier
                            .size(116.dp)
                            .alpha(.88f),
                    )
                } else {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 2.4f
                                scaleY = 2.4f
                            },
                    )
                }
            }
            if (progress < 59f / 108f) {
                Text(
                    text = "正在记录…",
                    modifier = Modifier.clearAndSetSemantics {},
                    color = MutedInk,
                    fontSize = 14.sp,
                )
            } else {
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = textProgress
                            translationY = (1f - textProgress) * 12.dp.toPx()
                        }
                        .clearAndSetSemantics {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "今天完成",
                        color = Ink,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    CompletionStreakLine(currentStreak)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = message.hint,
                        color = MutedInk,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionStreakLine(streak: Int) {
    if (streak <= 1) {
        Text(
            text = "你已经开始了",
            color = Ink,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (streak == 3) "已经连续" else "连续第",
            color = Ink,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(BrandGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = streak.toString(),
                color = Paper,
                fontSize = if (streak >= 10) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = "天",
            color = Ink,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

internal fun completionAnimationResource(streak: Int): Int = when {
    streak >= 30 -> R.raw.complete_streak_30
    streak == 7 -> R.raw.complete_streak_7
    streak == 3 -> R.raw.complete_streak_3
    else -> R.raw.complete_success
}

private data class CompletionMessage(
    val streakLine: String,
    val hint: String,
)

private fun completionMessage(streak: Int): CompletionMessage = when {
    streak <= 1 -> CompletionMessage("你已经开始了", "第一天，也很重要")
    streak >= 30 -> CompletionMessage("连续第 $streak 天", "这件事，正在成为生活的一部分")
    streak == 7 -> CompletionMessage("连续第 7 天", "你已经坚持一周了")
    streak == 3 -> CompletionMessage("已经连续 3 天", "节奏开始形成了")
    else -> CompletionMessage("连续第 $streak 天", "明天继续")
}
