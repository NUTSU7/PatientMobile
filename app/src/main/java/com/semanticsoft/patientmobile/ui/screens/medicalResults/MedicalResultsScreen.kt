package com.semanticsoft.patientmobile.ui.screens.medicalResults

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator

@Composable
fun MedicalResultsScreen(
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicalResultsViewModel = hiltViewModel()
) {
    val resultsState by viewModel.resultsState.collectAsStateWithLifecycle()
    val aiSummaryState by viewModel.aiSummaryState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        when (val state = resultsState) {
            is ResultsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(message = "Se \u00EEncarc\u0103 rezultatele...")
                }
            }
            is ResultsState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    state.groupedResults.forEach { (group, items) ->
                        item(key = "header_$group") {
                            Text(
                                text = group,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                        items(
                            items = items,
                            key = { it.id }
                        ) { item ->
                            Text(
                                text = "${item.originalTestName}: ${item.valueNumeric ?: item.valueText ?: "-"} ${item.unit}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            is ResultsState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }
        }
    }
}
