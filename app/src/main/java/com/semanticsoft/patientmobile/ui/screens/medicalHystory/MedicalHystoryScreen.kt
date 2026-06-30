package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.ui.components.DeleteConfirmationDialog
import com.semanticsoft.patientmobile.ui.components.ErrorDialog
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.AddMedicationDialog
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.AddPersonalNoteDialog
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.CombinedMedicationNotesCard
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicalHistoryAnalysisSection
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryEvent
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicalHistoryFiltersCard
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.ScheduleEntryData
import com.semanticsoft.patientmobile.domain.model.MealRelation
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.AppDimens

@Composable
fun MedicalHystoryScreen(
    viewModel: MedicalHystoryViewModel = hiltViewModel(),
    navigateToReportResults: (String) -> Unit = { },
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshErrors.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MedicalHystoryEvent.NavigateToReportResults ->
                    navigateToReportResults(event.reportId)
            }
        }
    }

    val hasData = state.allDocuments.isNotEmpty()
    val showBlockingError = state.errorMessage != null && !hasData

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding
        val isWideLayout = maxWidth >= 840.dp

        Crossfade(
            targetState = state.isLoading && state.allDocuments.isEmpty() && state.medicines.isEmpty(),
            animationSpec = tween(300),
            label = "HistoryTransition"
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(message = "Se \u00EEncarc\u0103 istoricul medical...")
                }
            } else {
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
                                onDateSelected = viewModel::onDateSelected,
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
                                    }

                                    Box(modifier = Modifier.width(400.dp)) {
                                        CombinedMedicationNotesCard(
                                            state = state,
                                            spacing = spacing,
                                            onAddMedicineClick = { viewModel.onShowAddMedicationDialog() },
                                            onAddNoteClick = { viewModel.onShowAddNoteDialog() },
                                            onTabSelected = viewModel::onCombinedSectionTabSelected,
                                            onDeleteMedicineClick = viewModel::onShowDeleteMedicationDialog,
                                            onDeleteNoteClick = viewModel::onShowDeleteNoteDialog,
                                            onEditMedicineClick = viewModel::onShowEditMedicationDialog,
                                            onEditNoteClick = viewModel::onShowEditNoteDialog
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
                                    CombinedMedicationNotesCard(
                                        state = state,
                                        spacing = spacing,
                                        onAddMedicineClick = { viewModel.onShowAddMedicationDialog() },
                                        onAddNoteClick = { viewModel.onShowAddNoteDialog() },
                                        onTabSelected = viewModel::onCombinedSectionTabSelected,
                                        onDeleteMedicineClick = viewModel::onShowDeleteMedicationDialog,
                                        onDeleteNoteClick = viewModel::onShowDeleteNoteDialog,
                                        onEditMedicineClick = viewModel::onShowEditMedicationDialog,
                                        onEditNoteClick = viewModel::onShowEditNoteDialog
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

        AnimatedVisibility(
            visible = state.showAddMedicationDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            val isEditingMedication = state.medicationDialogTargetId != null && state.medicationDialogTargetId != "add"
            val editingMed = if (isEditingMedication) state.medicines.find { it.id == state.medicationDialogTargetId } else null
            val durationDays = editingMed?.let { med ->
                if (med.effectiveDate != null && med.endDate != null) {
                    (med.endDate.toEpochDay() - med.effectiveDate.toEpochDay()).toInt()
                } else 0
            } ?: 0
            val editingDocName = editingMed?.analysisDocumentId?.let { id ->
                state.analysisDocuments.find { it.id == id }?.originalFileName
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onDismissAddMedicationDialog()
                        }
                    )
                    .padding(AppDimens.paddingDefault),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showAddMedicationDialog,
                    enter = scaleIn(initialScale = 0.9f, animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        AddMedicationDialog(
                            errorText = state.medicationDialogError,
                            documents = state.filteredDocuments.ifEmpty { state.analysisDocuments },
                            initialAnalysisDocumentId = editingMed?.analysisDocumentId,
                            initialAnalysisDocumentName = editingDocName,
                            initialName = editingMed?.name,
                            initialDoseValue = editingMed?.doseValue,
                            initialDoseUnit = editingMed?.doseUnit?.name,
                            initialSchedules = editingMed?.schedules?.map { schedule ->
                                ScheduleEntryData(
                                    administrationTime = schedule.administrationTime,
                                    mealRelation = schedule.mealRelation.name
                                )
                            },
                            initialDurationDays = durationDays.takeIf { it > 0 },
                            isEditing = isEditingMedication,
                            onDismiss = {
                                focusManager.clearFocus()
                                viewModel.onDismissAddMedicationDialog()
                            },
                            onSave = { name, doseValue, doseUnit, schedules, durationDays, associatedDocumentId ->
                                focusManager.clearFocus()
                                viewModel.addMedicine(name, doseValue, doseUnit, schedules, durationDays, associatedDocumentId)
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.showAddNoteDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            val isEditingNote = state.noteDialogTargetId != null && state.noteDialogTargetId != "add"
            val editingNote = if (isEditingNote) state.notes.find { it.id == state.noteDialogTargetId } else null
            val editingNoteDocName = editingNote?.analysisDocumentId?.let { id ->
                state.analysisDocuments.find { it.id == id }?.originalFileName
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onDismissAddNoteDialog()
                        }
                    )
                    .padding(AppDimens.paddingDefault),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showAddNoteDialog,
                    enter = scaleIn(initialScale = 0.9f, animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        AddPersonalNoteDialog(
                            errorText = state.noteDialogError,
                            documents = state.filteredDocuments.ifEmpty { state.analysisDocuments },
                            initialAnalysisDocumentId = editingNote?.analysisDocumentId,
                            initialAnalysisDocumentName = editingNoteDocName,
                            initialTitle = editingNote?.title,
                            initialDoctorLocation = editingNote?.author?.takeUnless { it == "Eu" },
                            initialContent = editingNote?.content,
                            isEditing = isEditingNote,
                            onDismiss = {
                                focusManager.clearFocus()
                                viewModel.onDismissAddNoteDialog()
                            },
                            onSave = { title, doctorLocation, content, analysisDocumentId ->
                                focusManager.clearFocus()
                                viewModel.addNote(title, doctorLocation, content, analysisDocumentId)
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.showDeleteMedicationDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onDismissDeleteMedicationDialog()
                        }
                    )
                    .padding(AppDimens.paddingDefault),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showDeleteMedicationDialog,
                    enter = scaleIn(initialScale = 0.9f, animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        state.deleteMedicationTargetId?.let { medId ->
                            val med = state.medicines.find { it.id == medId }
                            if (med != null) {
                                DeleteConfirmationDialog(
                                    title = "\u0218terge medicamentul",
                                    itemName = med.name,
                                    onDismiss = {
                                        focusManager.clearFocus()
                                        viewModel.onDismissDeleteMedicationDialog()
                                    },
                                    onConfirm = {
                                        focusManager.clearFocus()
                                        viewModel.onDeleteMedication(medId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.showDeleteNoteDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onDismissDeleteNoteDialog()
                        }
                    )
                    .padding(AppDimens.paddingDefault),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showDeleteNoteDialog,
                    enter = scaleIn(initialScale = 0.9f, animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        state.deleteNoteTargetId?.let { noteId ->
                            val note = state.notes.find { it.id == noteId }
                            if (note != null) {
                                DeleteConfirmationDialog(
                                    title = "\u0218terge noti\u021Ba",
                                    itemName = note.title,
                                    onDismiss = {
                                        focusManager.clearFocus()
                                        viewModel.onDismissDeleteNoteDialog()
                                    },
                                    onConfirm = {
                                        focusManager.clearFocus()
                                        viewModel.onDeleteNote(noteId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
