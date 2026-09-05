package com.boxsmall.onething.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boxsmall.onething.R
import com.boxsmall.onething.domain.GoalIconKey
import com.boxsmall.onething.ui.theme.BrandGreen
import com.boxsmall.onething.ui.theme.BrandLogoYellow
import com.boxsmall.onething.ui.theme.Hairline
import com.boxsmall.onething.ui.theme.Ink
import com.boxsmall.onething.ui.theme.Paper

internal data class GoalIconPresentation(
    val key: GoalIconKey,
    @DrawableRes val drawableRes: Int,
    val label: String,
    val accessibilityDescription: String,
)

internal val goalIconPresentations = listOf(
    GoalIconPresentation(GoalIconKey.WALK, R.drawable.ic_goal_walk, "走路", "走路目标图标"),
    GoalIconPresentation(GoalIconKey.READ, R.drawable.ic_goal_read, "阅读", "阅读目标图标"),
    GoalIconPresentation(GoalIconKey.SLEEP, R.drawable.ic_goal_sleep, "睡眠", "睡眠目标图标"),
    GoalIconPresentation(GoalIconKey.WATER, R.drawable.ic_goal_water, "喝水", "喝水目标图标"),
    GoalIconPresentation(GoalIconKey.STRETCH, R.drawable.ic_goal_stretch, "拉伸", "拉伸目标图标"),
    GoalIconPresentation(GoalIconKey.STUDY, R.drawable.ic_goal_study, "学习", "学习目标图标"),
    GoalIconPresentation(GoalIconKey.MEDICINE, R.drawable.ic_goal_medicine, "吃药", "吃药目标图标"),
    GoalIconPresentation(GoalIconKey.OTHER, R.drawable.ic_goal_other, "其他", "其他目标图标"),
)

internal fun GoalIconKey.presentation(): GoalIconPresentation =
    goalIconPresentations.firstOrNull { it.key == this }
        ?: goalIconPresentations.last()

@Composable
internal fun GoalIconBadge(
    iconKey: GoalIconKey,
    modifier: Modifier = Modifier,
    completed: Boolean = false,
    selected: Boolean = false,
    size: Dp = 56.dp,
) {
    val item = iconKey.presentation()
    val container = when {
        completed -> BrandGreen.copy(alpha = .18f)
        selected -> BrandLogoYellow.copy(alpha = .34f)
        else -> Color.White.copy(alpha = .8f)
    }
    val borderColor = when {
        completed -> BrandGreen
        selected -> BrandLogoYellow
        else -> Hairline
    }
    val iconColor = if (completed) BrandGreen else Ink

    Box(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = item.accessibilityDescription }
            .clip(RoundedCornerShape(size / 3))
            .background(container)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(size / 3)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(item.drawableRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(size * .48f),
        )
    }
}

@Composable
internal fun GoalIconPicker(
    selected: GoalIconKey,
    onSelect: (GoalIconKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 4,
    ) {
        goalIconPresentations.forEach { item ->
            val isSelected = item.key == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .testTag("goal-icon-${item.key.storageValue}")
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(item.key) },
                    )
                    .semantics {
                        contentDescription = "${item.accessibilityDescription}，${if (isSelected) "已选择" else "未选择"}"
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GoalIconBadge(
                    iconKey = item.key,
                    selected = isSelected,
                    size = 52.dp,
                    modifier = Modifier.clearAndSetSemantics {},
                )
                Text(
                    text = item.label,
                    modifier = Modifier.clearAndSetSemantics {},
                    color = Ink,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}
