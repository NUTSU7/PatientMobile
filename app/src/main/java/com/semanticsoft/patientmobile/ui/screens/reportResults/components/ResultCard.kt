package com.semanticsoft.patientmobile.ui.screens.reportResults.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.semanticsoft.patientmobile.ui.screens.reportResults.ResultItem
import com.semanticsoft.patientmobile.ui.theme.ErrorLightBg
import com.semanticsoft.patientmobile.ui.theme.PastelGreenBg
import com.semanticsoft.patientmobile.ui.theme.PastelOrangeBg
import com.semanticsoft.patientmobile.ui.theme.ResultHigh
import com.semanticsoft.patientmobile.ui.theme.ResultLow
import com.semanticsoft.patientmobile.ui.theme.ResultNormal
import com.semanticsoft.patientmobile.ui.theme.SoftIndigoGrey

@Composable
internal fun ResultCard(
    item: ResultItem,
    modifier: Modifier = Modifier
) {
    val valueColor = when (item.abnormalFlag) {
        "HIGH" -> ResultHigh
        "LOW" -> ResultLow
        else -> ResultNormal
    }

    val statusText = when (item.abnormalFlag) {
        "HIGH" -> "Ridicat"
        "LOW" -> "Sc\u0103zut"
        else -> "Normal"
    }

    val statusColor = when (item.abnormalFlag) {
        "HIGH" -> ResultHigh
        "LOW" -> ResultLow
        else -> ResultNormal
    }

    val badgeBg = when (item.abnormalFlag) {
        "HIGH" -> ErrorLightBg
        "LOW" -> PastelOrangeBg
        else -> PastelGreenBg
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.testName,
                color = SoftIndigoGrey,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.value,
                    color = valueColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (item.unit.isNotBlank()) {
                    Text(
                        text = item.unit,
                        color = SoftIndigoGrey,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Text(
                text = "Interval: ${item.reference}",
                color = SoftIndigoGrey,
                fontSize = 12.sp
            )

            Box(
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
