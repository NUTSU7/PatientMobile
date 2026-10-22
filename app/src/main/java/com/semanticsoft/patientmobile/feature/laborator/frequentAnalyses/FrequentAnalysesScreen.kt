package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses

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
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components.FrequentAnalysesFiltersCard
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components.FrequentAnalysesListCard

@Composable
fun FrequentAnalysesScreen(
    modifier: Modifier = Modifier,
    viewModel: FrequentAnalysesViewModel = hiltViewModel()
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
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "filters") {
            FrequentAnalysesFiltersCard(
                selectedPeriod = state.selectedPeriod,
                query = state.query,
                onPeriodSelected = viewModel::onPeriodSelected,
                onQueryChange = viewModel::onQueryChange
            )
        }

        item(key = "analyses") {
            FrequentAnalysesListCard(
                state = state,
                onSortSelected = viewModel::onSortSelected,
                onPageSelected = viewModel::onPageSelected,
                onPreviousPage = viewModel::onPreviousPage,
                onNextPage = viewModel::onNextPage
            )
        }
    }
}
