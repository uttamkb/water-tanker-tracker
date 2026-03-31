package com.apartment.watertracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AquaBlue,
    onPrimary = Surface,
    primaryContainer = DeepAqua,
    onPrimaryContainer = Surface,
    secondary = Teal,
    onSecondary = Surface,
    tertiary = Coral,
    onTertiary = Surface,
    background = Surface,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = DeepAqua,
    error = Coral,
)

private val DarkColors = darkColorScheme(
    primary = Sand,
    secondary = Teal,
    background = DeepAqua,
    surface = DeepAqua,
)

@Composable
fun WaterTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}
