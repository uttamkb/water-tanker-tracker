package com.apartment.watertracker.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            val icon = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline
            val tint = if (isFilled) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

            Icon(
                imageVector = icon,
                contentDescription = "Star $i",
                tint = tint,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChange(i) }
                    .padding(2.dp)
            )
        }
    }
}
