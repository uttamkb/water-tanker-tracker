package com.apartment.watertracker.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = MidnightBlue,
    onPrimary = TextOnPrimary,
    primaryContainer = AppSurfaceVariant,
    onPrimaryContainer = MidnightBlue,
    secondary = PremiumGold,
    onSecondary = TextOnSecondary,
    tertiary = GoldMuted,
    onTertiary = TextOnSecondary,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AppError,
    outline = AppBorder
)

private val DarkColors = darkColorScheme(
    primary = PremiumGold,
    onPrimary = TextOnSecondary,
    secondary = MidnightBlue,
    onSecondary = TextOnPrimary,
    tertiary = GoldLight,
    background = DeepNavy,
    surface = DeepNavy,
    onBackground = TextInverted,
    onSurface = TextInverted,
    error = AppError
)

@Composable
fun WaterTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default for premium brand consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
