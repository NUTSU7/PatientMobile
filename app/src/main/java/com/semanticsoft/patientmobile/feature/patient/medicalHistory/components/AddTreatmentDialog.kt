package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.domain.model.patient.MedicationDraft
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import kotlinx.coroutines.delay

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
    var timePickerTarget by remember { mutableStateOf<Pair<String, Int>?>(null) }

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

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                            onPickTime = { scheduleIndex -> timePickerTarget = draft.draftId to scheduleIndex }
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

            timePickerTarget?.let { (draftId, scheduleIndex) ->
                val target = drafts.find { it.draftId == draftId }?.schedules?.getOrNull(scheduleIndex)
                if (target == null) {
                    timePickerTarget = null
                } else {
                    TimePickerOverlay(
                        initialTime = target.administrationTime,
                        onDismiss = { timePickerTarget = null },
                        onConfirm = { time ->
                            onUpdateDraft(draftId) { draft ->
                                draft.copy(
                                    schedules = draft.schedules.mapIndexed { i, schedule ->
                                        if (i == scheduleIndex) schedule.copy(administrationTime = time) else schedule
                                    }
                                )
                            }
                            timePickerTarget = null
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerOverlay(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = initialTime.split(":")
    val timePickerState = rememberTimePickerState(
        initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8,
        initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = true
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(AppDimens.paddingDefault)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(AppDimens.cornerRadiusMedium),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.paddingDefault),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = Indigo600)) {
                    TimePicker(state = timePickerState)
                }
                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Anuleaz\u0103", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(
                        onClick = {
                            val hour = timePickerState.hour.toString().padStart(2, '0')
                            val minute = timePickerState.minute.toString().padStart(2, '0')
                            onConfirm("$hour:$minute")
                        }
                    ) { Text("OK") }
                }
            }
        }
    }
}
