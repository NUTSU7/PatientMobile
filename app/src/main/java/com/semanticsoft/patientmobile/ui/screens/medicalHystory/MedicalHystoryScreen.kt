package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.ui.components.ErrorDialog
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicalHistoryAnalysisSection
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicalHistoryFiltersCard
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicinesSection
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.PersonalNotesSection
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun MedicalHystoryScreen(
    viewModel: MedicalHystoryViewModel = hiltViewModel(),
    refreshTrigger: SharedFlow<Unit> = kotlinx.coroutines.flow.MutableSharedFlow(),
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(refreshTrigger) {
        refreshTrigger.collect { viewModel.refresh() }
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshErrors.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    val hasData = state.allDocuments.isNotEmpty()
    val showBlockingError = state.errorMessage != null && !hasData

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding
        val isWideLayout = maxWidth >= 840.dp

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 1152.dp)
                                .padding(
                                    start = horizontalPadding,
                                    top = spacing.sectionGap,
                                    end = horizontalPadding,
                                    bottom = spacing.bottomSpacer
                                ),
                            verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
                        ) {
                            MedicalHistoryFiltersCard(
                                state = state,
                                spacing = spacing,
                                onTimelineValueSelected = viewModel::onTimelineValueSelected,
                                onYearSelected = viewModel::onYearSelected
                            )

                            if (isWideLayout) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.sectionGap),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
                                    ) {
                                        MedicalHistoryAnalysisSection(
                                            state = state,
                                            spacing = spacing,
                                            onDocumentClick = viewModel::onDocumentSelected
                                        )
                                        MedicinesSection(
                                            state = state,
                                            spacing = spacing,
                                            onAddClick = {
                                                viewModel.addMedicine(
                                                    "Ibuprofen",
                                                    "1 tablet\u0103 \u2022 La nevoie",
                                                    "La nevoie",
                                                    10
                                                )
                                            }
                                        )
                                    }

                                    Box(modifier = Modifier.width(400.dp)) {
                                        PersonalNotesSection(
                                            state = state,
                                            spacing = spacing,
                                            onAttachFileClick = {},
                                            onAddNoteClick = {
                                                viewModel.addNote(
                                                    "Noti\u021B\u0103 nou\u0103",
                                                    "Aceasta este o noti\u021B\u0103 rapid\u0103 ad\u0103ugat\u0103 din interfa\u021B\u0103."
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                                    MedicalHistoryAnalysisSection(
                                        state = state,
                                        spacing = spacing,
                                        onDocumentClick = viewModel::onDocumentSelected
                                    )
                                    MedicinesSection(
                                        state = state,
                                        spacing = spacing,
                                        onAddClick = {
                                            viewModel.addMedicine(
                                                "Ibuprofen",
                                                "1 tablet\u0103 \u2022 La nevoie",
                                                "La nevoie",
                                                10
                                            )
                                        }
                                    )
                                    PersonalNotesSection(
                                        state = state,
                                        spacing = spacing,
                                        onAttachFileClick = {},
                                        onAddNoteClick = {
                                            viewModel.addNote(
                                                "Noti\u021B\u0103 nou\u0103",
                                                "Aceasta este o noti\u021B\u0103 rapid\u0103 ad\u0103ugat\u0103 din interfa\u021B\u0103."
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) { data: SnackbarData ->
                    Snackbar(data)
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(message = "Se \u00EEncarc\u0103 istoricul medical...")
                    }
                }
            }
        }

        if (showBlockingError) {
            state.errorMessage?.let { message ->
                ErrorDialog(
                    message = message,
                    title = "Istoric medical",
                    onDismiss = { },
                    onRetry = { viewModel.refresh() }
                )
            }
        }
    }
}
