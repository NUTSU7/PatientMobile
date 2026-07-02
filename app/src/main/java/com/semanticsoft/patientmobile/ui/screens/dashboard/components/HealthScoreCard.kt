package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.theme.StatusAttentionFill
import com.semanticsoft.patientmobile.ui.theme.StatusBorderlineFill
import com.semanticsoft.patientmobile.ui.theme.StatusNormalFill
import kotlin.math.max
import kotlin.math.min

@Composable
@Suppress("UNUSED_PARAMETER")
fun HealthScoreCard(
    score: Int,
    normalCount: Int,
    borderlineCount: Int,
    attentionCount: Int,
    noReferenceCount: Int = 0,
    documentCount: Int = 0,
    statusText: String = "",
    onStatusFilterClick: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.9f, min(maxWidth.value / 343.2f, 1.15f))
        val interpretableTotal = normalCount + borderlineCount + attentionCount

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20f * scale).dp, vertical = (20f * scale).dp),
                verticalArrangement = Arrangement.spacedBy((14f * scale).dp)
            ) {
                Text(
                    text = "Scor analize",
                    color = Color(0xFF111827),
                    fontSize = 18.sp * scale,
                    lineHeight = 24.sp * scale,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when {
                        documentCount == 1 -> "Bazat pe ultimul set de analize"
                        documentCount > 1 -> "Bazat pe ultimele $documentCount seturi de analize"
                        else -> "Bazat pe ultimele analize"
                    },
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp * scale,
                    lineHeight = 16.sp * scale
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((16f * scale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SegmentedDonut(
                        normalCount = normalCount,
                        borderlineCount = borderlineCount,
                        attentionCount = attentionCount,
                        total = interpretableTotal,
                        score = score,
                        scale = scale
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy((8f * scale).dp)
                    ) {
                        StatusPill(
                            label = "Normal",
                            count = normalCount,
                            fillColor = StatusNormalFill,
                            scale = scale,
                            onClick = { onStatusFilterClick("NORMAL") }
                        )
                        StatusPill(
                            label = "La limit\u0103",
                            count = borderlineCount,
                            fillColor = StatusBorderlineFill,
                            scale = scale,
                            onClick = { onStatusFilterClick("BORDERLINE") }
                        )
                        StatusPill(
                            label = "Aten\u021Bie",
                            count = attentionCount,
                            fillColor = StatusAttentionFill,
                            scale = scale,
                            onClick = { onStatusFilterClick("ATTENTION") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedDonut(
    normalCount: Int,
    borderlineCount: Int,
    attentionCount: Int,
    total: Int,
    score: Int,
    scale: Float
) {
    val ringSize = (120f * scale).dp
    val strokeWidth = 34f * scale

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = (size.width - strokeWidth) / 2f
            val gap = if (listOf(normalCount, borderlineCount, attentionCount).count { it > 0 } > 1) 2.2f else 0f

            fun drawSegment(count: Int, color: Color, startAngle: Float): Float {
                if (count <= 0 || total <= 0) return startAngle
                val sweepAngle = (count.toFloat() / total) * 360f - gap
                if (sweepAngle <= 0f) return startAngle
                drawArc(
                    color = color,
                    startAngle = startAngle + gap / 2f - 90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
                return startAngle + sweepAngle + gap
            }

            var angle = 0f
            angle = drawSegment(normalCount, StatusNormalFill, angle)
            angle = drawSegment(borderlineCount, StatusBorderlineFill, angle)
            drawSegment(attentionCount, StatusAttentionFill, angle)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = total.toString(),
                color = Color(0xFF111827),
                fontSize = 28.sp * scale,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Indicatori",
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp * scale,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${score}%",
                color = Color(0xFF111827),
                fontSize = 13.sp * scale,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = (2f * scale).dp)
            )
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    count: Int,
    fillColor: Color,
    scale: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = (16f * scale).dp, vertical = (12f * scale).dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(fillColor, CircleShape)
            )
            Text(
                text = label,
                color = Color(0xFF374151),
                fontSize = 14.sp * scale,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = count.toString(),
            color = Color(0xFF111827),
            fontSize = 16.sp * scale,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
