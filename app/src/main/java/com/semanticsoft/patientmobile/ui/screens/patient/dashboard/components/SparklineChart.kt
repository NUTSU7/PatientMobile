package com.semanticsoft.patientmobile.ui.screens.patient.dashboard.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.StatusAttentionFill
import com.semanticsoft.patientmobile.ui.theme.StatusBorderlineFill
import com.semanticsoft.patientmobile.ui.theme.StatusNoReferenceFill
import com.semanticsoft.patientmobile.ui.theme.StatusNormalFill

@Composable
fun SparklineChart(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    referenceLow: Float? = null,
    referenceHigh: Float? = null,
    showReferenceLines: Boolean = false
) {
    val effectivePoints = if (points.size <= 1) points else points
    val isEmpty = effectivePoints.isEmpty()

    Canvas(
        modifier = modifier.fillMaxWidth().height(height)
    ) {
        if (isEmpty) return@Canvas

        val dataMin = effectivePoints.min()
        val dataMax = effectivePoints.max()
        val dataRange = if (dataMax - dataMin > 0f) dataMax - dataMin else 1f

        val expandRange = showReferenceLines && referenceLow != null && referenceHigh != null
        val yMin = if (expandRange) {
            val refMin = referenceLow!!
            val refMax = referenceHigh!!
            (refMin - (refMax - refMin) * 0.15f).coerceAtMost(dataMin)
        } else dataMin
        val yMax = if (expandRange) {
            val refMin = referenceLow!!
            val refMax = referenceHigh!!
            (refMax + (refMax - refMin) * 0.15f).coerceAtLeast(dataMax)
        } else dataMax
        val yRange = if (yMax - yMin > 0f) yMax - yMin else 1f

        val padding = 4.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        val coords = effectivePoints.mapIndexed { index, value ->
            val x = padding + if (effectivePoints.size == 1) chartWidth / 2f else index * chartWidth / (effectivePoints.size - 1)
            val y = padding + chartHeight * (1f - (value - yMin) / yRange)
            val pointColor = getPointColor(value, referenceLow, referenceHigh)
            Coord(x, y, value, pointColor)
        }

        if (coords.size == 1) {
            val c = coords.first()
            val baseY = c.y
            drawLine(
                color = StatusNoReferenceFill,
                start = Offset(padding, baseY),
                end = Offset(padding + chartWidth, baseY),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = c.color,
                radius = 4f,
                center = Offset(c.x, baseY)
            )
            drawCircle(
                color = Color.Transparent,
                radius = 16f,
                center = Offset(c.x, baseY)
            )
        } else if (coords.size >= 2) {
            val fillPath = Path().apply {
                moveTo(coords.first().x, size.height - padding)
                coords.forEach { lineTo(it.x, it.y) }
                lineTo(coords.last().x, size.height - padding)
                close()
            }

            val hasMultipleColors = coords.any { it.color != coords.first().color }

            if (hasMultipleColors) {
                val colorStops = coords.map { coord ->
                    val fraction = (coord.x - padding) / chartWidth
                    fraction.coerceIn(0f, 1f) to coord.color
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.horizontalGradient(
                        *colorStops.toTypedArray(),
                        startX = padding,
                        endX = padding + chartWidth
                    ),
                    alpha = 0.12f
                )

                for (i in 0 until coords.size - 1) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            0f to coords[i].color,
                            1f to coords[i + 1].color,
                            startX = coords[i].x,
                            endX = coords[i + 1].x
                        ),
                        start = Offset(coords[i].x, coords[i].y),
                        end = Offset(coords[i + 1].x, coords[i + 1].y),
                        strokeWidth = 2f
                    )
                }
            } else {
                drawPath(
                    path = fillPath,
                    color = color,
                    alpha = 0.12f
                )

                for (i in 0 until coords.size - 1) {
                    drawLine(
                        color = color,
                        start = Offset(coords[i].x, coords[i].y),
                        end = Offset(coords[i + 1].x, coords[i + 1].y),
                        strokeWidth = 2f
                    )
                }
            }

            coords.forEach { coord ->
                drawCircle(
                    color = coord.color,
                    radius = 3.5f,
                    center = Offset(coord.x, coord.y)
                )
                drawCircle(
                    color = Color.Transparent,
                    radius = 16f,
                    center = Offset(coord.x, coord.y)
                )
            }
        }

        if (showReferenceLines && referenceLow != null && referenceHigh != null) {
            val refLowY = padding + chartHeight * (1f - (referenceLow - yMin) / yRange)
            val refHighY = padding + chartHeight * (1f - (referenceHigh - yMin) / yRange)
            val labelEndX = padding + chartWidth - 8f
            drawLine(
                color = StatusNoReferenceFill,
                start = Offset(padding, refLowY),
                end = Offset(labelEndX, refLowY),
                strokeWidth = 1f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
            )
            drawLine(
                color = StatusNoReferenceFill,
                start = Offset(padding, refHighY),
                end = Offset(labelEndX, refHighY),
                strokeWidth = 1f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
            )

            val labelPaint = Paint()
            labelPaint.color = android.graphics.Color.parseColor("#94A3B8")
            labelPaint.textSize = 10.dp.toPx()
            labelPaint.isAntiAlias = true
            labelPaint.textAlign = Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText(
                "min $referenceLow",
                labelEndX + 8f,
                refLowY + 4f,
                labelPaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                "max $referenceHigh",
                labelEndX + 8f,
                refHighY + 4f,
                labelPaint
            )
        }
    }
}

private data class Coord(val x: Float, val y: Float, val value: Float, val color: Color)

private fun getPointColor(value: Float, refLow: Float?, refHigh: Float?): Color {
    if (refLow == null || refHigh == null || refHigh <= refLow) return StatusNoReferenceFill
    val range = refHigh - refLow
    return when {
        value >= refLow && value <= refHigh -> StatusNormalFill
        value < refLow -> {
            val deviation = (refLow - value) / range
            if (deviation > 0.5f) StatusAttentionFill else StatusBorderlineFill
        }
        value > refHigh -> {
            val deviation = (value - refHigh) / range
            if (deviation > 0.5f) StatusAttentionFill else StatusBorderlineFill
        }
        else -> StatusNormalFill
    }
}
