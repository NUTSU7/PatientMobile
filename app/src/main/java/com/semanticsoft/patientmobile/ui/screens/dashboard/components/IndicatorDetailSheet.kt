package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.ui.theme.statusChipBg
import com.semanticsoft.patientmobile.ui.theme.statusChipText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndicatorDetailSheet(
    title: String,
    value: String,
    unit: String,
    status: IndicatorStatus,
    historyPoints: List<Float>,
    referenceLow: Float?,
    referenceHigh: Float?,
    color: Color,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val chipLabel = when (status) {
        IndicatorStatus.NORMAL -> "Optim"
        IndicatorStatus.BORDERLINE -> "La limit\u0103"
        IndicatorStatus.ATTENTION -> "Aten\u021Bie"
        IndicatorStatus.NO_REFERENCE -> "F\u0103r\u0103 interval"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF111827),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = chipLabel,
                        color = statusChipText(status),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(statusChipBg(status), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color(0xFF111827),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = unit,
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }
            }

            if (historyPoints.isNotEmpty()) {
                SparklineChart(
                    points = historyPoints,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    height = 160.dp,
                    referenceLow = referenceLow,
                    referenceHigh = referenceHigh,
                    showReferenceLines = true
                )
            }
        }
    }
}
