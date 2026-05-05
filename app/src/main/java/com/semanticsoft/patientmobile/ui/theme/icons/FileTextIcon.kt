package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FileTextIcon(modifier: Modifier = Modifier, color: Color = Color(0xFF4F46E5)) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.08f
        val foldSize = size.width * 0.35f

        val documentPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width - foldSize, 0f)
            lineTo(size.width, foldSize)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(documentPath, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

        val foldCreasePath = Path().apply {
            moveTo(size.width - foldSize, 0f)
            lineTo(size.width - foldSize, foldSize)
            lineTo(size.width, foldSize)
        }

        drawPath(foldCreasePath, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

        val lineStartX = size.width * 0.2f
        val startY = size.height * 0.45f
        val lineSpacing = size.height * 0.18f

        drawLine(color, Offset(lineStartX, startY), Offset(size.width * 0.5f, startY), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(lineStartX, startY + lineSpacing), Offset(size.width * 0.8f, startY + lineSpacing), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(lineStartX, startY + lineSpacing * 2), Offset(size.width * 0.8f, startY + lineSpacing * 2), strokeWidth, StrokeCap.Round)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFileTextIcon() {
    FileTextIcon(modifier = Modifier.size(48.dp))
}
