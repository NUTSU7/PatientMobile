package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
@Suppress("UNUSED_PARAMETER")
fun HealthScoreCard(
    score: Int,
    normalCount: Int,
    borderlineCount: Int,
    attentionCount: Int,
    statusText: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.9f, min(maxWidth.value / 343.2f, 1.15f))
        val isCompact = maxWidth < 360.dp

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
                    text = "Bazat pe ultimele 1 seturi de analize",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp * scale,
                    lineHeight = 16.sp * scale
                )

                if (isCompact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy((16f * scale).dp)
                    ) {
                        ScoreRing(score = score, scale = scale)
                        ScoreBars(normalCount, borderlineCount, attentionCount, score, scale)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy((16f * scale).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScoreRing(score = score, scale = scale)
                        ScoreBars(
                            normalCount = normalCount,
                            borderlineCount = borderlineCount,
                            attentionCount = attentionCount,
                            score = score,
                            scale = scale,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRing(score: Int, scale: Float) {
    val progress = score.coerceIn(0, 100) / 100f

    Box(
        modifier = Modifier.size((120f * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 52f * scale
            val center = Offset(size.width / 2f, size.height / 2f)
            val sweepPurple = 360f * progress
            val sweepGray = 360f - sweepPurple

            rotate(-90f, center) {
                drawArc(
                    color = Color(0xFF6366F1),
                    startAngle = 0f,
                    sweepAngle = sweepPurple,
                    useCenter = false,
                    style = Stroke(width = stroke)
                )

                if (sweepGray > 0f) {
                    drawArc(
                        color = Color(0xFFF3F4F6),
                        startAngle = sweepPurple,
                        sweepAngle = sweepGray,
                        useCenter = false,
                        style = Stroke(width = stroke)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                color = Color(0xFF111827),
                fontSize = 28.sp * scale,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "PUNCTE",
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp * scale,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ScoreBars(
    normalCount: Int,
    borderlineCount: Int,
    attentionCount: Int,
    score: Int,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val total = (normalCount + borderlineCount + attentionCount).coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy((10f * scale).dp)
    ) {
        ScoreBarLine("Normal", normalCount.toString(), score.coerceIn(0, 100) / 100f, Color(0xFF6366F1), scale)
        ScoreBarLine("La limită", borderlineCount.toString(), borderlineCount.toFloat() / total.toFloat(), Color(0xFFFACC15), scale)
        ScoreBarLine("Atenție", attentionCount.toString(), attentionCount.toFloat() / total.toFloat(), Color(0xFFEF4444), scale)
    }
}

@Composable
private fun ScoreBarLine(label: String, value: String, progress: Float, color: Color, scale: Float) {
    Column(verticalArrangement = Arrangement.spacedBy((4f * scale).dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color(0xFF6B7280), fontSize = 12.sp * scale)
            Text(text = value, color = Color(0xFF111827), fontSize = 13.sp * scale, fontWeight = FontWeight.SemiBold)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((8f * scale).dp)
                .background(Color(0xFFF3F4F6), RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(color, RoundedCornerShape(999.dp))
            )
        }
    }
}
