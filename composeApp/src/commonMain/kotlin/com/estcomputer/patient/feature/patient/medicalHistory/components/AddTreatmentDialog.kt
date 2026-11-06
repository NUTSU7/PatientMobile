package com.estcomputer.patient.feature.patient.medicalHistory.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estcomputer.patient.designsystem.theme.AppDimens
import com.estcomputer.patient.designsystem.theme.Indigo600
import com.estcomputer.patient.feature.patient.medicalHistory.MedicationDraft
import com.estcomputer.patient.feature.patient.uploadedAnalyses.PatientDocument
import java.time.LocalDate
import kotlinx.coroutines.delay

private sealed interface PickerTarget {
    data class Date(val draftId: String, val initialDate: LocalDate) : PickerTarget
    data class Time(val draftId: String, val scheduleIndex: Int, val initialTime: String) : PickerTarget
}

/**
 * "Adaugă tratament" / "Editează tratament" dialog card. Stateless: all data and every change
 * flows through the ViewModel (RULES.md). The scrim wrapper lives in the Screen file.
 *
 * [showTreatmentName] is false only when editing a standalone medication: that mode keeps a single
 * card and cannot add more (same restriction as web).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTreatmentDialog(
    isEditing: Boolean,
    showTreatmentName: Boolean,
    treatmentName: String,
    drafts: List<MedicationDraft>,
    expandedDraftId: String?,
    analysisDocumentIds: Set<String>,
    documents: List<PatientDocument>,
    lateStartDraftIds: Set<String>,
    errorText: String?,
    isSaving: Boolean,
    ocrIsExtracting: Boolean,
    ocrFeedbackMessage: String?,
    ocrFeedbackError: Boolean,
    ocrAttachmentFileName: String?,
    onTreatmentNameChange: (String) -> Unit,
    onAnalysisDocumentsChange: (Set<String>) -> Unit,
    onAddDraft: () -> Unit,
    onRemoveDraft: (draftId: String) -> Unit,
    onToggleDraft: (draftId: String) -> Unit,
    onUpdateDraft: (draftId: String, transform: (MedicationDraft) -> MedicationDraft) -> Unit,
    onOcrFileSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = errorText != null
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    var activePicker by remember { mutableStateOf<PickerTarget?>(null) }

    // Auto-scroll down to newly added draft so the user is moved to it
    LaunchedEffect(drafts.size) {
        if (drafts.size > 1) {
            delay(100)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val documentOptions = buildList {
        add(DialogDropdownOption(key = "", label = "F\u0103r\u0103 analize asociate"))
        analysisDocumentIds.filter { id -> documents.none { it.id == id } }.forEach { id ->
            add(DialogDropdownOption(key = id, label = "Analiz\u0103 ($id)"))
        }
        addAll(documents.map { DialogDropdownOption(key = it.id, label = it.originalFileName) })
    }

    AnimatedContent(
        targetState = activePicker,
        transitionSpec = {
            (fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) + scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )).togetherWith(
                fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                )
            )
        },
        label = "TreatmentPickerTransition",
        modifier = modifier.fillMaxSize()
    ) { target ->
        when (target) {
            null -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppDimens.paddingDefault)
            ) {
                Text(
                    text = if (isEditing) "Editeaz\u0103 tratament" else "Adaug\u0103 tratament",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    OcrAttachmentSection(
                        isExtracting = ocrIsExtracting,
                        feedbackMessage = ocrFeedbackMessage,
                        isFeedbackError = ocrFeedbackError,
                        attachmentFileName = ocrAttachmentFileName,
                        onFileSelected = onOcrFileSelected
                    )
                    Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                    // 1. Analize asociate (placed above Denumire tratament)
                    DialogFieldLabel("Analize asociate")
                    Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                    DialogMultiSelectDropdownField(
                        selectedKeys = analysisDocumentIds,
                        placeholder = "F\u0103r\u0103 analize asociate",
                        options = documentOptions,
                        onSelectionChange = onAnalysisDocumentsChange
                    )
                    Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                    // 2. Denumire tratament
                    if (showTreatmentName) {
                        DialogFieldLabel("Denumire tratament")
                        Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                        DialogTextField(
                            value = treatmentName,
                            onValueChange = onTreatmentNameChange,
                            placeholder = "Ex: Tratamentul meu",
                            isError = isError && treatmentName.isBlank()
                        )
                        Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                    }
                    Text(
                        text = "Verific\u0103 fiecare medicament. C\u00E2mpurile necunoscute pot r\u0103m\u00E2ne goale. Gruparea nu reprezint\u0103 o recomandare sau validare medical\u0103.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                    // 3. Medication cards (closable / accordion style)
                    drafts.forEachIndexed { index, draft ->
                        MedicationDraftCard(
                            index = index,
                            draft = draft,
                            expanded = (draft.draftId == expandedDraftId),
                            canRemove = showTreatmentName && drafts.size > 1,
                            showLateStartNote = draft.draftId in lateStartDraftIds,
                            onToggle = { onToggleDraft(draft.draftId) },
                            onChange = { transform -> onUpdateDraft(draft.draftId, transform) },
                            onRemove = { onRemoveDraft(draft.draftId) },
                            onPickDate = {
                                focusManager.clearFocus()
                                activePicker = PickerTarget.Date(draft.draftId, draft.startDate)
                            },
                            onPickTime = { scheduleIndex ->
                                focusManager.clearFocus()
                                val initialTime = draft.schedules.getOrNull(scheduleIndex)?.administrationTime ?: "08:00"
                                activePicker = PickerTarget.Time(draft.draftId, scheduleIndex, initialTime)
                            }
                        )
                        Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                    }

                    if (showTreatmentName) {
                        OutlinedButton(
                            onClick = onAddDraft,
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Indigo600),
                            shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Indigo600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Adaug\u0103 medicament \u00EEn tratament",
                                color = Indigo600,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Anuleaz\u0103",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onSave,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Indigo600,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (isEditing) "Salveaz\u0103 modific\u0103rile" else "Salveaz\u0103",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
    is PickerTarget.Date -> {
                TreatmentDatePickerDialog(
                    initialDate = target.initialDate,
                    onDismiss = { activePicker = null },
                    onConfirm = { selectedDate ->
                        onUpdateDraft(target.draftId) { draft ->
                            draft.copy(startDate = selectedDate)
                        }
                        activePicker = null
                    }
                )
            }
            is PickerTarget.Time -> {
                TreatmentTimePickerDialog(
                    initialTime = target.initialTime,
                    onDismiss = { activePicker = null },
                    onConfirm = { time ->
                        onUpdateDraft(target.draftId) { draft ->
                            draft.copy(
                                schedules = draft.schedules.mapIndexed { i, schedule ->
                                    if (i == target.scheduleIndex) schedule.copy(administrationTime = time) else schedule
                                }
                            )
                        }
                        activePicker = null
                    }
                )
            }
        }
    }
}
