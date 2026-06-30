package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.background
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
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE9EDFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size((20f * scale).dp)
                    )

                    Text(
                        text = "Rezumat AI",
                        color = Color(0xFF312E81),
                        fontSize = 18.sp * scale,
                        lineHeight = 22.sp * scale,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height((14f * scale).dp))

                Text(
                    text = summaryText,
                    color = Color(0xFF312E81),
                    fontSize = 15.3.sp * scale,
                    lineHeight = 26.sp * scale
                )

                Spacer(modifier = Modifier.height((16f * scale).dp))

                Text(
                    text = "Consultă doctorul pentru mai multe detalii.",
                    color = Color(0xFF4F46E5),
                    fontSize = 13.sp * scale,
                    lineHeight = 16.sp * scale,
                    fontWeight = FontWeight.Bold
                )

            }
        }
    }
}
