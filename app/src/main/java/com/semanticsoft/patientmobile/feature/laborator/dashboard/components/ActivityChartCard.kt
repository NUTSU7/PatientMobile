package com.semanticsoft.patientmobile.feature.laborator.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSubtleText
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorActivitySeries
import kotlin.math.max

@Composable
internal fun ActivityChartCard(
    activity: LaboratorActivitySeries,
    period: LaboratorPeriod,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Evoluția activității",
                        color = Gray900,
                        fontSize = 18.sp,
                        lineHeight = 21.6.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rezultate procesate vs. accesate vs. pacienți unici",
                        color = LaboratorSubtleText,
                        fontSize = 12.sp,
                        lineHeight = 16.2.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Text(
                    text = period.activityLabel,
                    color = Gray900,
                    fontSize = 9.sp,
                    lineHeight = 12.6.sp,
                    letterSpacing = 0.36.sp,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .background(LaboratorPanelBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActivityLineChart(
                series = activity,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LaboratorPanelBg)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    LegendItem(
                        color = LaboratorMutedText,
                        label = "Rezultate procesate"
                    )
                    LegendItem(
                        color = Indigo600,
                        label = "Rezultate accesate"
                    )
                }
                LegendItem(
                    color = Emerald600,
                    label = "Pacienți unici"
                )
            }

            Text(
                text = "Actualizare orară automată",
                color = LaboratorMutedText,
                fontSize = 11.sp,
                lineHeight = 16.5.sp,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = Gray900,
            fontSize = 11.sp,
            lineHeight = 16.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActivityLineChart(
    series: LaboratorActivitySeries,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription =
                "Grafic evoluție activitate: rezultate procesate, rezultate accesate și pacienți unici."
        }
    ) {
        val padding = 8.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

        val allValues = series.processed + series.accessed + series.uniquePatients
        if (allValues.isEmpty()) return@Canvas

        val minValue = allValues.minOrNull() ?: 0f
        val maxValue = allValues.maxOrNull() ?: 1f
        val valueRange = max(maxValue - minValue, 0.01f)

        fun toPoints(values: List<Float>): List<Offset> {
            if (values.isEmpty()) return emptyList()
            return values.mapIndexed { index, value ->
                val x = if (values.size == 1) {
                    padding + chartWidth / 2f
                } else {
                    padding + index * chartWidth / (values.size - 1)
                }
                val fraction = ((value - minValue) / valueRange).coerceIn(0f, 1f)
                val y = padding + chartHeight * (1f - fraction)
                Offset(x, y)
            }
        }

        val processedPoints = toPoints(series.processed)
        val accessedPoints = toPoints(series.accessed)
        val uniquePoints = toPoints(series.uniquePatients)

        val processedPath = smoothPath(processedPoints)
        val accessedPath = smoothPath(accessedPoints)
        val uniquePath = smoothPath(uniquePoints)

        if (accessedPoints.size >= 2) {
            val fillPath = Path().apply {
                addPath(accessedPath)
                lineTo(accessedPoints.last().x, size.height - padding)
                lineTo(accessedPoints.first().x, size.height - padding)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Indigo600.copy(alpha = 0.18f),
                        Indigo600.copy(alpha = 0.01f)
                    ),
                    startY = padding,
                    endY = size.height - padding
                )
            )
        }

        drawPath(
            path = processedPath,
            color = LaboratorMutedText,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(3.dp.toPx(), 4.dp.toPx())
                )
            )
        )
        drawPath(
            path = accessedPath,
            color = Indigo600,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = uniquePath,
            color = Emerald600,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        processedPoints.lastOrNull()?.let { point ->
            drawCircle(
                color = LaboratorMutedText,
                radius = 3.5.dp.toPx(),
                center = point
            )
        }
        accessedPoints.lastOrNull()?.let { point ->
            drawCircle(
                color = Indigo600,
                radius = 3.5.dp.toPx(),
                center = point
            )
        }
        uniquePoints.lastOrNull()?.let { point ->
            drawCircle(
                color = Emerald600,
                radius = 3.5.dp.toPx(),
                center = point
            )
        }
    }
}

private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    if (points.size == 1) return path

    for (index in 0 until points.lastIndex) {
        val previous = points[(index - 1).coerceAtLeast(0)]
        val current = points[index]
        val next = points[index + 1]
        val afterNext = points[(index + 2).coerceAtMost(points.lastIndex)]

        val control1 = Offset(
            x = current.x + (next.x - previous.x) / 6f,
            y = current.y + (next.y - previous.y) / 6f
        )
        val control2 = Offset(
            x = next.x - (afterNext.x - current.x) / 6f,
            y = next.y - (afterNext.y - current.y) / 6f
        )

        path.cubicTo(
            control1.x,
            control1.y,
            control2.x,
            control2.y,
            next.x,
            next.y
        )
    }
    return path
}
