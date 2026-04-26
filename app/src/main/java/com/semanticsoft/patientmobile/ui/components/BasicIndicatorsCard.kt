package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.IndicatorSegments
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.data.model.IndicatorTrendDirection
import kotlin.math.max
import kotlin.math.min

@Composable
@Suppress("UNUSED_PARAMETER")
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

        val chipLabel = when (status) {
            IndicatorStatus.NORMAL -> "Normal"
            IndicatorStatus.BORDERLINE -> "La limită"
            IndicatorStatus.ATTENTION -> "Atenție"
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (16f * scale).dp, vertical = (16f * scale).dp),
                verticalArrangement = Arrangement.spacedBy((10f * scale).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF6B7280),
                        fontSize = 13.5f.sp * scale,
                        lineHeight = 19.sp * scale,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size((8f * scale).dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(999.dp))
                            .padding(horizontal = (8f * scale).dp, vertical = (3f * scale).dp)
                    ) {
                        Text(
                            text = chipLabel,
                            color = Color(0xFF6366F1),
                            fontSize = 9.8f.sp * scale,
                            lineHeight = 14.sp * scale,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color(0xFF111827),
                        fontSize = 26.sp * scale,
                        lineHeight = 30.sp * scale,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size((6f * scale).dp))
                    Text(
                        text = unit,
                        color = Color(0xFF6B7280),
                        fontSize = 11.sp * scale,
                        lineHeight = 20.sp * scale,
                        modifier = Modifier.padding(bottom = (2f * scale).dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((32f * scale).dp)
                        .background(Color(0xFFEEF2FF), RoundedCornerShape((10f * scale).dp)),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(safeMarker.coerceAtLeast(0.04f))
                            .fillMaxHeight()
                            .background(Color(0xFF6366F1), RoundedCornerShape((10f * scale).dp))
                    )
                }
            }
        }
    }
}
