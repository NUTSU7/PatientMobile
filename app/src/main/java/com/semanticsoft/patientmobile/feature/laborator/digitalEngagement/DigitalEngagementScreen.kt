package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.core.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components.CohortEvolutionCard
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components.EngagementFiltersCard
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components.EngagementPrivacyFooter
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components.LocationEngagementCard
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components.RepeatedAnalysesCard

@Composable
fun DigitalEngagementScreen(
    modifier: Modifier = Modifier,
    viewModel: DigitalEngagementViewModel = hiltViewModel()
) {
    SetStatusBar(color = Color.White, darkIcons = true)

    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LaboratorDashboardBackground),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "filters") {
            EngagementFiltersCard(
                selectedPeriod = state.selectedPeriod,
                locationFilter = state.locationFilter,
                compareEnabled = state.compareEnabled,
                onPeriodSelected = viewModel::onPeriodSelected,
                onLocationSelected = viewModel::onLocationSelected,
                onCompareToggle = viewModel::onCompareToggle
            )
        }

        item(key = "cohort-evolution") {
            CohortEvolutionCard(state = state)
        }

        item(key = "locations") {
            LocationEngagementCard(state = state)
        }

        item(key = "repeated") {
            RepeatedAnalysesCard(
                state = state,
                onQueryChange = viewModel::onRepeatedQueryChange,
                onAnalysisToggle = viewModel::onAnalysisToggle
            )
        }

        item(key = "privacy-footer") {
            EngagementPrivacyFooter(text = PRIVACY_FOOTNOTE)
        }
    }
}
