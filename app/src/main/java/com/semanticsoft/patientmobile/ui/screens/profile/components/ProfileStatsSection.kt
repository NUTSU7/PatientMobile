package com.semanticsoft.patientmobile.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AppShapes
import com.semanticsoft.patientmobile.ui.theme.icons.IconChart
import com.semanticsoft.patientmobile.ui.theme.icons.IconClock

private val IconBgShape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
private val IconBgSize = 44.dp
private val IconSize = 22.dp
private val ChartIconBg = Color(0xFFEEF2FF)
private val ClockIconBg = Color(0xFFFDF2F8)
private val ChartIconTint = Color(0xFF818CF8)
private val ClockIconTint = Color(0xFFF472B6)

@Composable
internal fun ProfileStatsSection(
    totalAnalyses: Int,
    daysSinceLastAnalysis: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.gapDefault)
    ) {
        StatCard(
            icon = {
                Box(
                    modifier = Modifier
                        .size(IconBgSize)
                        .background(ChartIconBg, IconBgShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconChart,
                        contentDescription = null,
                        tint = ChartIconTint,
                        modifier = Modifier.size(IconSize)
                    )
                }
            },
            label = "TOTAL DATE MEDICALE",
            value = "$totalAnalyses analize \u00EEnc\u0103rcate"
        )
        StatCard(
            icon = {
                Box(
                    modifier = Modifier
                        .size(IconBgSize)
                        .background(ClockIconBg, IconBgShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconClock,
                        contentDescription = null,
                        tint = ClockIconTint,
                        modifier = Modifier.size(IconSize)
                    )
                }
            },
            label = "ACTIVITATE RECENT\u0102",
            value = "Ultima analiz\u0103: acum $daysSinceLastAnalysis zile"
        )
    }
}

@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.paddingDefault),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(AppDimens.paddingDefault))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
