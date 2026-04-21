package com.apartment.watertracker.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun QualityChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (dataPoints.size < 2) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxData = dataPoints.maxOrNull() ?: 1f
            val minData = dataPoints.minOrNull() ?: 0f
            val range = (maxData - minData).coerceAtLeast(1f)

            val points = dataPoints.mapIndexed { index, value ->
                val x = index * (width / (dataPoints.size - 1))
                val y = height - ((value - minData) / range) * height
                Offset(x, y)
            }

            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val p1 = points[i - 1]
                    val p2 = points[i]
                    val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2, p1.y)
                    val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2, p2.y)
                    cubicTo(
                        controlPoint1.x, controlPoint1.y,
                        controlPoint2.x, controlPoint2.y,
                        p2.x, p2.y
                    )
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Fill area under the curve
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )
        }
    }
}
