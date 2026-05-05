package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PillIcon(modifier: Modifier = Modifier, color: Color = Color(0xFF4F46E5)) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.08f
        rotate(degrees = -45f) {
            val pillWidth = size.width
            val pillHeight = size.height * 0.45f
            val topLeftY = (size.height - pillHeight) / 2f

            drawRoundRect(
                color = color,
                topLeft = Offset(x = 0f, y = topLeftY),
                size = Size(width = pillWidth, height = pillHeight),
                cornerRadius = CornerRadius(x = pillHeight / 2f, y = pillHeight / 2f),
                style = Stroke(width = strokeWidth)
            )

            drawLine(
                color = color,
                start = Offset(x = pillWidth / 2f, y = topLeftY),
                end = Offset(x = pillWidth / 2f, y = topLeftY + pillHeight),
                strokeWidth = strokeWidth
            )
        }
    }
}

@Preview
@Composable
fun PreviewPillIcon() {
    PillIcon(modifier = Modifier.size(48.dp))
}
