package com.semanticsoft.patientmobile.feature.laborator.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.feature.laborator.dashboard.components.ActivityChartCard
import com.semanticsoft.patientmobile.feature.laborator.dashboard.components.OverviewMetricsRow
import com.semanticsoft.patientmobile.feature.laborator.dashboard.components.PeriodFilterCard
import com.semanticsoft.patientmobile.feature.laborator.dashboard.components.TopAnalysesCard
import com.semanticsoft.patientmobile.feature.laborator.dashboard.components.TrendCard

@Composable
fun LaboratorDashboardScreen(
    state: LaboratorDashboardUiState,
    onPeriodSelected: (LaboratorPeriod) -> Unit,
    onCompareToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LaboratorDashboardBackground),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "period-filter") {
            PeriodFilterCard(
                selectedPeriod = state.selectedPeriod,
                compareEnabled = state.compareEnabled,
                onPeriodSelected = onPeriodSelected,
                onCompareToggle = onCompareToggle,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }

        item(key = "overview-metrics") {
            OverviewMetricsRow(
                metrics = state.metrics,
                compareEnabled = state.compareEnabled,
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        item(key = "activity-chart") {
            ActivityChartCard(
                activity = state.activity,
                period = state.selectedPeriod,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)
            )
        }

        item(key = "activity-trend") {
            TrendCard(
                trend = state.trend,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }

        item(key = "top-analyses") {
            TopAnalysesCard(
                analyses = state.topAnalyses,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)
            )
        }
    }
}
