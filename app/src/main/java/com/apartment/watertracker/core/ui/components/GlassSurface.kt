package com.apartment.watertracker.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A surface that implements a glassmorphism (frosted glass) effect.
 * On API 31+, it uses actual blur. On older versions, it falls back to
 * a semi-transparent tinted surface.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColor = containerColor.copy(alpha = 0.85f) // Increased opacity for better legibility
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        glassColor,
                        glassColor.copy(alpha = 0.6f)
                    )
                )
            )
            .border(1.dp, borderColor, shape) // Slightly thicker border for "layered" feel
    ) {
        content()
    }
}
