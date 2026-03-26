package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.SouthEast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.IndicatorSegments
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.data.model.IndicatorTrendDirection
import kotlin.math.max
import kotlin.math.min

@Composable
fun BasicIndicatorsCard(
    title: String,
    value: String,
    unit: String,
    status: IndicatorStatus,
    trendDirection: IndicatorTrendDirection,
    trendDelta: String,
    trendDescription: String,
    markerPosition: Float,
    segments: IndicatorSegments,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.88f, min(maxWidth.value / 343.2f, 1.12f))
        val safeMarker = markerPosition.coerceIn(0f, 1f)

        val chipBg = when (status) {
            IndicatorStatus.NORMAL -> Color(0xFFF0FDF4)
            IndicatorStatus.BORDERLINE -> Color(0xFFFEF9C3)
            IndicatorStatus.ATTENTION -> Color(0xFFFEE2E2)
        }
        val chipText = when (status) {
            IndicatorStatus.NORMAL -> Color(0xFF16A34A)
            IndicatorStatus.BORDERLINE -> Color(0xFFA16207)
            IndicatorStatus.ATTENTION -> Color(0xFFB91C1C)
        }
        val chipLabel = when (status) {
            IndicatorStatus.NORMAL -> "Normal"
            IndicatorStatus.BORDERLINE -> "La limită"
            IndicatorStatus.ATTENTION -> "Atenție"
        }
        val markerColor = when (status) {
            IndicatorStatus.NORMAL -> Color(0xFF4ADE80)
            IndicatorStatus.BORDERLINE -> Color(0xFFFACC15)
            IndicatorStatus.ATTENTION -> Color(0xFFF87171)
        }

        val rawGood = segments.good.coerceAtLeast(0f)
        val rawBorderline = segments.borderline.coerceAtLeast(0f)
        val rawRisk = segments.risk.coerceAtLeast(0f)
        val segmentTotal = rawGood + rawBorderline + rawRisk

        // Compose weight() crashes for values <= 0f, so keep a safe minimum.
        val minWeight = 0.01f
        val goodWeight = if (segmentTotal > 0f) max(minWeight, rawGood / segmentTotal) else 0.6f
        val borderlineWeight = if (segmentTotal > 0f) max(minWeight, rawBorderline / segmentTotal) else 0.2f
        val riskWeight = if (segmentTotal > 0f) max(minWeight, rawRisk / segmentTotal) else 0.2f

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20.8f * scale).dp, vertical = (20.8f * scale).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF6B7280),
                        fontSize = 11.9.sp * scale,
                        lineHeight = 20.sp * scale
                    )
                    Box(
                        modifier = Modifier
                            .background(chipBg, RoundedCornerShape(999.dp))
                            .padding(horizontal = (8.8f * scale).dp, vertical = (2.8f * scale).dp)
                    ) {
                        Text(
                            text = chipLabel,
                            color = chipText,
                            fontSize = 10.2.sp * scale,
                            lineHeight = 16.sp * scale
                        )
                    }
                }

                Spacer(modifier = Modifier.height((10f * scale).dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color(0xFF111827),
                        fontSize = 25.5.sp * scale,
                        lineHeight = 36.sp * scale
                    )
                    Spacer(modifier = Modifier.size((6f * scale).dp))
                    Text(
                        text = unit,
                        color = Color(0xFF6B7280),
                        fontSize = 11.9.sp * scale,
                        lineHeight = 20.sp * scale,
                        modifier = Modifier.padding(bottom = (2f * scale).dp)
                    )
                }

                Spacer(modifier = Modifier.height((12f * scale).dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val barHeight = (8f * scale).dp
                    val markerSize = (12f * scale).dp
                    val markerX = (maxWidth - markerSize) * safeMarker

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .weight(goodWeight)
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .background(Color(0xFF4ADE80))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(borderlineWeight)
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .background(Color(0xFFFACC15))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(riskWeight)
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .background(Color(0xFFF87171))
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = markerX, y = (-2f * scale).dp)
                            .size(markerSize)
                            .background(markerColor, CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height((16f * scale).dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((2f * scale).dp)
                ) {
                    val trendIcon = when (trendDirection) {
                        IndicatorTrendDirection.STABLE -> Icons.Outlined.Remove
                        IndicatorTrendDirection.UP -> Icons.Outlined.NorthEast
                        IndicatorTrendDirection.DOWN -> Icons.Outlined.SouthEast
                    }

                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size((12f * scale).dp)
                    )

                    if (trendDirection == IndicatorTrendDirection.STABLE) {
                        Text(
                            text = trendDescription,
                            color = Color(0xFF6B7280),
                            fontSize = 10.2.sp * scale,
                            lineHeight = 16.sp * scale,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = if (trendDirection == IndicatorTrendDirection.UP) "+" else "-",
                            color = Color(0xFF6B7280),
                            fontSize = 10.2.sp * scale,
                            lineHeight = 16.sp * scale,
                            modifier = Modifier.padding(PaddingValues(top = (1.6f * scale).dp))
                        )
                        Text(
                            text = trendDelta,
                            color = Color(0xFF6B7280),
                            fontSize = 10.2.sp * scale,
                            lineHeight = 16.sp * scale
                        )
                        Text(
                            text = trendDescription,
                            color = Color(0xFF6B7280),
                            fontSize = 10.2.sp * scale,
                            lineHeight = 16.sp * scale,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
