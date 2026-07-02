package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.ui.theme.statusChipBg
import com.semanticsoft.patientmobile.ui.theme.statusChipText
import com.semanticsoft.patientmobile.ui.theme.statusFillColor
import kotlin.math.max
import kotlin.math.min

@Composable
@Suppress("UNUSED_PARAMETER")
fun GeneralMarkersCard(
    title: String,
    category: String,
    value: String,
    unit: String,
    status: IndicatorStatus,
    normalRange: String,
    borderlineRange: String,
    attentionRange: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.88f, min(maxWidth.value / 343.2f, 1.12f))
        val initials = title
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "In" }

        val chipLabel = when (status) {
            IndicatorStatus.NORMAL -> "Optim"
            IndicatorStatus.BORDERLINE -> "La limit\u0103"
            IndicatorStatus.ATTENTION -> "Aten\u021Bie"
            IndicatorStatus.NO_REFERENCE -> "F\u0103r\u0103 interval"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = (16f * scale).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((12f * scale).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((46f * scale).dp)
                        .background(Color.White, CircleShape)
                        .border((2f * scale).dp, statusFillColor(status), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = statusFillColor(status),
                        fontSize = 13.sp * scale,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy((2f * scale).dp)) {
                    Text(
                        text = title,
                        color = Color(0xFF111827),
                        fontSize = 15.sp * scale,
                        lineHeight = 20.sp * scale,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category,
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp * scale,
                        lineHeight = 17.sp * scale,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy((4f * scale).dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color(0xFF111827),
                        fontSize = 20.sp * scale,
                        lineHeight = 24.sp * scale,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.size((4f * scale).dp))
                    Text(
                        text = unit,
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp * scale,
                        lineHeight = 16.sp * scale
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusChipBg(status), RoundedCornerShape(999.dp))
                        .padding(horizontal = (10f * scale).dp, vertical = (4f * scale).dp)
                ) {
                    Text(
                        text = chipLabel,
                        color = statusChipText(status),
                        fontSize = 10.6f.sp * scale,
                        lineHeight = 15.sp * scale,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
