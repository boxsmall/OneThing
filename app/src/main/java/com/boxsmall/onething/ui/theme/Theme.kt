package com.boxsmall.onething.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val OneThingColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Paper,
    primaryContainer = BrandGreenSoft,
    onPrimaryContainer = Ink,
    secondary = BrandYellow,
    onSecondary = Ink,
    error = Danger,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    outline = Hairline,
)

@Composable
fun OneThingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OneThingColors,
        typography = OneThingTypography,
        content = content,
    )
}
