package com.semanticsoft.patientmobile.feature.laborator.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Info
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
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText
import com.semanticsoft.patientmobile.core.ui.theme.PurpleLabel
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorMetric

@Composable
internal fun OverviewMetricsRow(
    metrics: List<LaboratorMetric>,
    compareEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = metrics,
            key = { it.id }
        ) { metric ->
            OverviewMetricCard(
                metric = metric,
                compareEnabled = compareEnabled
            )
        }
    }
}

@Composable
private fun OverviewMetricCard(
    metric: LaboratorMetric,
    compareEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(174.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = metric.label.uppercase(),
                color = Gray500,
                fontSize = 11.sp,
                lineHeight = 16.5.sp,
                letterSpacing = 0.06.sp
            )
            Text(
                text = metric.value,
                color = if (metric.insufficientData) Gray500 else Gray900,
                fontSize = 36.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            MetricFooter(
                metric = metric,
                compareEnabled = compareEnabled
            )
        }
    }
}

@Composable
private fun MetricFooter(
    metric: LaboratorMetric,
    compareEnabled: Boolean
) {
    when {
        metric.insufficientData -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = PurpleLabel,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Date insuficiente",
                    color = PurpleLabel,
                    fontSize = 14.sp,
                    lineHeight = 17.sp
                )
            }
        }

        !compareEnabled -> {
            Text(
                text = "Comparație dezactivată",
                color = LaboratorMutedText,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        metric.changeText != null -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = Emerald600,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = metric.changeText,
                    color = Emerald600,
                    fontSize = 14.sp,
                    lineHeight = 16.8.sp
                )
            }
        }
    }
}
