package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorAnalysisSubtitle
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorAnalysisTitle
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorInsufficientText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSurfaceSubtle
import com.semanticsoft.patientmobile.core.ui.theme.TextTertiary
import com.semanticsoft.patientmobile.core.ui.theme.icons.InfoCircleIcon
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FrequentAnalysis
import java.text.NumberFormat
import java.util.Locale

@Composable
internal fun FrequentAnalysisRow(
    analysis: FrequentAnalysis,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = analysis.name,
            color = LaboratorAnalysisTitle,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = analysis.subtitle,
            color = LaboratorAnalysisSubtitle,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalysisMetricCell(
                label = "REZULTATE ACCESATE",
                value = formatCount(analysis.accessedResults),
                modifier = Modifier.weight(1f)
            )
            AnalysisMetricCell(
                label = "PACIENȚI UNICI",
                value = analysis.uniquePatients?.let(::formatCount),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnalysisMetricCell(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(LaboratorSurfaceSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            letterSpacing = 0.055.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (value != null) {
            Text(
                text = value,
                color = LaboratorAnalysisTitle,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "—",
                color = LaboratorInsufficientText,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Date insuficiente",
                    color = LaboratorInsufficientText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Icon(
                    imageVector = InfoCircleIcon,
                    contentDescription = null,
                    tint = LaboratorInsufficientText,
                    modifier = Modifier.size(10.39.dp)
                )
            }
        }
    }
}

private val countFormatter: NumberFormat =
    NumberFormat.getIntegerInstance(Locale.forLanguageTag("ro-RO"))

private fun formatCount(value: Int): String {
    return countFormatter.format(value)
}
