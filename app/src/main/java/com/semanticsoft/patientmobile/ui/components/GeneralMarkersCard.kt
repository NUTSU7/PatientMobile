package com.semanticsoft.patientmobile.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import kotlin.math.max
import kotlin.math.min

@Composable
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

        val chipBg = when (status) {
            IndicatorStatus.NORMAL -> Color(0xFFDCFCE7)
            IndicatorStatus.BORDERLINE -> Color(0xFFFEF9C3)
            IndicatorStatus.ATTENTION -> Color(0xFFFEE2E2)
        }
        val chipText = when (status) {
            IndicatorStatus.NORMAL -> Color(0xFF166534)
            IndicatorStatus.BORDERLINE -> Color(0xFF854D0E)
            IndicatorStatus.ATTENTION -> Color(0xFF991B1B)
        }
        val chipLabel = when (status) {
            IndicatorStatus.NORMAL -> "Normal"
            IndicatorStatus.BORDERLINE -> "La limită"
            IndicatorStatus.ATTENTION -> "Atenție"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20.8f * scale).dp, vertical = (20.8f * scale).dp),
                verticalArrangement = Arrangement.spacedBy((16f * scale).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text(
                            text = title,
                            color = Color(0xFF111827),
                            fontSize = 13.6.sp * scale,
                            lineHeight = 24.sp * scale,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = category,
                            color = Color(0xFF6B7280),
                            fontSize = 10.2.sp * scale,
                            lineHeight = 16.sp * scale
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(chipBg, RoundedCornerShape(999.dp))
                            .height((24f * scale).dp)
                            .padding(horizontal = (10f * scale).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chipLabel,
                            color = chipText,
                            fontSize = 10.2.sp * scale,
                            lineHeight = 16.sp * scale,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color(0xFF111827),
                        fontSize = 20.4.sp * scale,
                        lineHeight = 32.sp * scale,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.padding(start = (4f * scale).dp))
                    Text(
                        text = unit,
                        color = Color(0xFF6B7280),
                        fontSize = 11.9.sp * scale,
                        lineHeight = 20.sp * scale,
                        modifier = Modifier.padding(bottom = (2f * scale).dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                        .padding(horizontal = (10.8f * scale).dp, vertical = (10.8f * scale).dp),
                    verticalArrangement = Arrangement.spacedBy((4f * scale).dp)
                ) {
                    RangeLine(label = "Normal:", value = normalRange, labelColor = Color(0xFF6B7280), valueColor = Color(0xFF6B7280), scale = scale)
                    RangeLine(label = "La limită:", value = borderlineRange, labelColor = Color(0xFF6B7280), valueColor = Color(0xFF6B7280), scale = scale)
                    RangeLine(label = "Atenție:", value = attentionRange, labelColor = Color(0xFFDC2626).copy(alpha = 0.8f), valueColor = Color(0xFFDC2626).copy(alpha = 0.8f), scale = scale)
                }
            }
        }
    }
}

@Composable
private fun RangeLine(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    scale: Float
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label ",
            color = labelColor,
            fontSize = 10.2.sp * scale,
            lineHeight = 16.sp * scale
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 10.2.sp * scale,
            lineHeight = 16.sp * scale
        )
    }
}
