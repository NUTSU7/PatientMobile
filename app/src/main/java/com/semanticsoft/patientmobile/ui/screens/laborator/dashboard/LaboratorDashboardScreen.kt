package com.semanticsoft.patientmobile.ui.screens.laborator.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.ui.theme.AppDimens

@Composable
fun LaboratorDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: LaboratorDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppDimens.paddingDefault),
        verticalArrangement = Arrangement.spacedBy(AppDimens.gapSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Panou laborator",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (state.isLoading) "Se incarca..." else "Lucrari primite: ${state.submissions.size}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
