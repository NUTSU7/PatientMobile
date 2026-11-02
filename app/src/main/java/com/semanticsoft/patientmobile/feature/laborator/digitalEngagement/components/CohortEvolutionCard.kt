package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorAnalysisSubtitle
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDivider
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorEngagementBlue
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorEngagementPurple
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorInsufficientText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorInputBorder
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSubtleText
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.DigitalEngagementUiState
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.EngagementCohortLine
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.EngagementCohortSnapshot
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.EngagementCohortStat
import kotlin.math.max

private val cohortSeriesColors = listOf(
    LaboratorEngagementBlue,
    Indigo600,
    LaboratorEngagementPurple
)

private val cohortSeriesLabels = listOf(
    "<30 zile",
    "30–90 zile",
    "91–180 zile"
)

private val yPercentLabels = listOf("50%", "40%", "30%", "20%", "10%", "0%")

@Composable
internal fun CohortEvolutionCard(
    state: DigitalEngagementUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.25.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "Evoluția implicării",
            color = LaboratorSubtleText,
            fontSize = 18.sp,
            lineHeight = 22.5.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = state.chartSubtitle,
            color = LaboratorAnalysisSubtitle,
            fontSize = 12.sp,
            lineHeight = 16.8.sp,
            modifier = Modifier.padding(top = 5.dp)
        )

        CohortLegend(modifier = Modifier.padding(top = 16.dp))

        CohortLineChartArea(
            xLabels = state.xLabels,
            cohortLines = state.cohortLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        state.cohortSnapshot?.let { snapshot ->
            CohortTooltip(
                snapshot = snapshot,
                compareEnabled = state.compareEnabled,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        HorizontalDivider(
            color = LaboratorDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            text = state.cohortFootnote,
            color = LaboratorInsufficientText,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun CohortLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cohortSeriesLabels.forEachIndexed { index, label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(cohortSeriesColors[index], CircleShape)
                )
                Text(
                    text = label,
                    color = LaboratorSubtleText,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun CohortLineChartArea(
    xLabels: List<String>,
    cohortLines: List<EngagementCohortLine>,
    modifier: Modifier = Modifier
) {
    val chartHeight = 170.dp

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier.height(chartHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yPercentLabels.forEach { label ->
                    Text(
                        text = label,
                        color = LaboratorInsufficientText,
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            CohortLineChart(
                cohortLines = cohortLines,
                modifier = Modifier
                    .weight(1f)
                    .height(chartHeight)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { label ->
                Text(
                    text = label,
                    color = LaboratorInsufficientText,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun CohortLineChart(
    cohortLines: List<EngagementCohortLine>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription =
                "Grafic evoluție implicare pe cohorte: sub 30 de zile, 30–90 de zile și 91–180 de zile."
        }
    ) {
        val maxValue = 0.5f

        drawGridlines(maxValue)

        cohortLines.forEachIndexed { index, line ->
            val color = cohortSeriesColors[index % cohortSeriesColors.size]
            val observedValues = line.values.take(max(line.observed, 1))
            val observedPoints = toPoints(observedValues, maxValue, size.width, size.height)
            val observedPath = smoothPath(observedPoints)

            if (index == 0 && observedPoints.isNotEmpty()) {
                drawAreaFill(observedPoints, observedPath, size.height)
            }

            drawPath(
                path = observedPath,
                color = color,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            observedPoints.forEach { point ->
                drawCircle(
                    color = color,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }

            if (line.observed < line.values.size) {
                val tailValues = line.values.drop((line.observed - 1).coerceAtLeast(0))
                val tailPoints = toPoints(tailValues, maxValue, size.width, size.height)
                drawPath(
                    path = smoothPath(tailPoints),
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(3.dp.toPx(), 4.dp.toPx())
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun CohortTooltip(
    snapshot: EngagementCohortSnapshot,
    compareEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LaboratorDashboardBackground, RoundedCornerShape(12.dp))
            .border(1.25.dp, Gray300, RoundedCornerShape(12.dp))
    ) {
        Text(
            text = snapshot.cohortLabel,
            color = Gray900,
            fontSize = 11.sp,
            lineHeight = 16.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
        HorizontalDivider(color = LaboratorInputBorder, thickness = 1.dp)

        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            TooltipCell(
                stat = snapshot.stats.firstOrNull(),
                compareEnabled = compareEnabled,
                modifier = Modifier.weight(1f)
            )
            snapshot.stats.drop(1).forEach { stat ->
                VerticalDivider(
                    color = LaboratorInputBorder,
                    modifier = Modifier.fillMaxHeight()
                )
                TooltipCell(
                    stat = stat,
                    compareEnabled = compareEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TooltipCell(
    stat: EngagementCohortStat?,
    compareEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val index = cohortSeriesLabels.indexOfFirst { it == stat?.label }
    val dotColor = index.takeIf { it >= 0 }?.let { cohortSeriesColors[it] } ?: Color.Transparent

    Column(
        modifier = modifier.padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = stat?.label.orEmpty(),
                color = LaboratorSubtleText,
                fontSize = 9.sp,
                lineHeight = 12.2.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = stat?.valueText.orEmpty(),
                color = Gray900,
                fontSize = 12.sp,
                lineHeight = 16.2.sp,
                fontWeight = FontWeight.SemiBold
            )
            val change = stat?.changeText.orEmpty()
            if (compareEnabled && change.isNotEmpty()) {
                Text(
                    text = change,
                    color = Emerald600,
                    fontSize = 9.sp,
                    lineHeight = 12.2.sp
                )
            }
        }
    }
}

private fun toPoints(
    values: List<Float>,
    maxValue: Float,
    width: Float,
    height: Float
): List<Offset> {
    if (values.isEmpty()) return emptyList()
    return values.mapIndexed { index, value ->
        val x = if (values.size == 1) {
            width / 2f
        } else {
            index * width / (values.size - 1)
        }
        val fraction = (value / maxValue).coerceIn(0f, 1f)
        Offset(x, height * (1f - fraction))
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

private fun DrawScope.drawGridlines(maxValue: Float) {
    val steps = 5
    for (step in 0..steps) {
        val y = size.height * (1f - step.toFloat() / steps)
        drawLine(
            color = LaboratorInputBorder.copy(alpha = 0.6f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(2.dp.toPx(), 5.dp.toPx())
            )
        )
    }
}

private fun DrawScope.drawAreaFill(
    points: List<Offset>,
    linePath: Path,
    height: Float
) {
    val fillPath = Path().apply {
        addPath(linePath)
        lineTo(points.last().x, height)
        lineTo(points.first().x, height)
        close()
    }
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                LaboratorEngagementBlue.copy(alpha = 0.14f),
                LaboratorEngagementBlue.copy(alpha = 0.01f)
            ),
            startY = 0f,
            endY = height
        )
    )
}
