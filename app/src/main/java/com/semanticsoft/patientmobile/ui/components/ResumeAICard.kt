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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun ResumeAICard(
    summaryText: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.86f, min(maxWidth.value / 343.2f, 1.06f))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF).copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val radiusPx = (64f * scale).dp.toPx()
                        val insetPx = (24.8f * scale).dp.toPx()
                        drawCircle(
                            color = Color(0xFFE0E7FF).copy(alpha = 0.5f),
                            radius = radiusPx,
                            center = Offset(x = size.width - insetPx, y = insetPx)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (24.8f * scale).dp, vertical = (24.8f * scale).dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8f * scale).dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size((29.6f * scale).dp)
                                .border(1.dp, Color(0xFFEEF2FF), RoundedCornerShape(6.dp))
                                .background(Color.White, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF5A52E5),
                                modifier = Modifier.size((16f * scale).dp)
                            )
                        }

                        Text(
                            text = "AI · Rezumat pe înțelesul tău",
                            color = Color(0xFF5A52E5),
                            fontSize = 11.9.sp * scale,
                            lineHeight = 20.sp * scale,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height((22f * scale).dp))

                    Text(
                        text = summaryText,
                        color = Color(0xFF1F2937),
                        fontSize = 15.3.sp * scale,
                        lineHeight = 28.8.sp * scale
                    )

                    Spacer(modifier = Modifier.height((18f * scale).dp))

                    Text(
                        text = "Consultați medicul pentru interpretare medicală.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 10.2.sp * scale,
                        lineHeight = 16.sp * scale
                    )
                }
            }
        }
    }
}
