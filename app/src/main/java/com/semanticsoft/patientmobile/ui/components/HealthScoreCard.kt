package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
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

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (24 * scale).dp, vertical = (24 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scorul tău de sănătate",
                    color = Color(0xFF111827),
                    fontSize = 15.3.sp * scale,
                    lineHeight = 28.sp * scale,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height((24 * scale).dp))

                Box(
                    modifier = Modifier
                        .size((160 * scale).dp)
                        .border(width = 1.dp, color = Color(0xFF111827), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = score.toString(),
                            color = Color(0xFFEAB308),
                            fontSize = 30.6.sp * scale,
                            lineHeight = 40.sp * scale,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/100",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.9.sp * scale,
                            lineHeight = 20.sp * scale,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height((16 * scale).dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF9C3), RoundedCornerShape(999.dp))
                        .padding(horizontal = (12 * scale).dp, vertical = (4 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        color = Color(0xFF854D0E),
                        fontSize = 11.9.sp * scale,
                        lineHeight = 20.sp * scale,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height((18 * scale).dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    ScoreLegendItem(
                        topText = normalCount.toString(),
                        bottomText = "normali",
                        dotColor = Color(0xFF22C55E),
                        scale = scale,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "·",
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.9.sp * scale,
                        modifier = Modifier.padding(top = (10 * scale).dp)
                    )
                    ScoreLegendItem(
                        topText = "$borderlineCount la",
                        bottomText = "limită",
                        dotColor = Color(0xFFEAB308),
                        scale = scale,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "·",
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.9.sp * scale,
                        modifier = Modifier.padding(top = (10 * scale).dp)
                    )
                    ScoreLegendItem(
                        topText = attentionCount.toString(),
                        bottomText = "atenție!",
                        dotColor = Color(0xFFEF4444),
                        scale = scale,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height((18 * scale).dp))

                Text(
                    text = "Nu constituie aviz medical.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.2.sp * scale,
                    lineHeight = 16.sp * scale,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ScoreLegendItem(
    topText: String,
    bottomText: String,
    dotColor: Color,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(width = (9.375f * scale).dp, height = (10f * scale).dp)
                .background(dotColor, CircleShape)
        )
        Column {
            Text(
                text = topText,
                color = Color(0xFF4B5563),
                fontSize = 11.9.sp * scale,
                lineHeight = 20.sp * scale,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = bottomText,
                color = Color(0xFF4B5563),
                fontSize = 11.9.sp * scale,
                lineHeight = 20.sp * scale,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
