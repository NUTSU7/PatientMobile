package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600

@Composable
fun AddPersonalNoteDialog(
    errorText: String?,
    documents: List<PatientDocument>,
    initialAnalysisDocumentId: String? = null,
    initialAnalysisDocumentIds: List<String> = emptyList(),
    initialAnalysisDocumentName: String? = null,
    initialTitle: String? = null,
    initialDoctorLocation: String? = null,
    initialContent: String? = null,
    isEditing: Boolean = false,
    ocrIsExtracting: Boolean = false,
    ocrFeedbackMessage: String? = null,
    ocrFeedbackError: Boolean = false,
    ocrAttachmentFileName: String? = null,
    ocrInitialTitle: String? = null,
    ocrInitialDoctorLocation: String? = null,
    ocrInitialContent: String? = null,
    onOcrFileSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (title: String, doctorLocation: String, content: String, analysisDocumentIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    val initialEffectiveIds = (initialAnalysisDocumentIds.ifEmpty { listOfNotNull(initialAnalysisDocumentId) }).toSet()
    var selectedDocumentIds by rememberSaveable { mutableStateOf(initialEffectiveIds) }
    var doctorLocation by rememberSaveable { mutableStateOf("") }
    var clinicalObservations by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialTitle) { initialTitle?.let { title = it } }
    LaunchedEffect(initialDoctorLocation) { initialDoctorLocation?.let { doctorLocation = it } }
    LaunchedEffect(initialContent) { initialContent?.let { clinicalObservations = it } }
    LaunchedEffect(initialAnalysisDocumentId, initialAnalysisDocumentIds) {
        val updated = (initialAnalysisDocumentIds.ifEmpty { listOfNotNull(initialAnalysisDocumentId) }).toSet()
        if (updated.isNotEmpty()) {
            selectedDocumentIds = updated
        }
    }

    LaunchedEffect(ocrInitialTitle) { ocrInitialTitle?.let { title = it } }
    LaunchedEffect(ocrInitialDoctorLocation) { ocrInitialDoctorLocation?.let { doctorLocation = it } }
    LaunchedEffect(ocrInitialContent) { ocrInitialContent?.let { clinicalObservations = it } }

    val documentOptions = buildList {
        add(DialogDropdownOption(key = "", label = "Fără analiză asociată"))
        selectedDocumentIds.forEach { id ->
            val inList = documents.any { it.id == id }
            if (!inList) {
                val labelName = if (id == initialAnalysisDocumentId && initialAnalysisDocumentName != null) {
                    "$initialAnalysisDocumentName (indisponibilă)"
                } else {
                    "Analiză ($id)"
                }
                add(DialogDropdownOption(key = id, label = labelName))
            }
        }
        addAll(documents.map { doc ->
            DialogDropdownOption(key = doc.id, label = doc.originalFileName)
        })
    }

    val isError = errorText != null

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.paddingDefault)
        ) {
            // === FIXED HEADER ===
            Text(
                text = if (isEditing) "Editează notiță" else "Adaugă notiță personală",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            // === SCROLLABLE CONTENT ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                OcrAttachmentSection(
                    isExtracting = ocrIsExtracting,
                    feedbackMessage = ocrFeedbackMessage,
                    isFeedbackError = ocrFeedbackError,
                    attachmentFileName = ocrAttachmentFileName,
                    onFileSelected = onOcrFileSelected
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                DialogFieldLabel("Titlu notiță")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Analize sânge (Profil complet)",
                    isError = isError
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                DialogFieldLabel("Analiză asociată")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogMultiSelectDropdownField(
                    selectedKeys = selectedDocumentIds,
                    placeholder = "Fără analiză asociată",
                    options = documentOptions,
                    onSelectionChange = { nextSelected ->
                        selectedDocumentIds = nextSelected
                        if (title.isEmpty() && nextSelected.isNotEmpty()) {
                            val firstOption = documentOptions.firstOrNull { it.key == nextSelected.first() }
                            if (firstOption != null && firstOption.key.isNotEmpty()) {
                                title = firstOption.label
                            }
                        }
                    },
                    isError = isError
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                DialogFieldLabel("Doctor / Locație")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogTextField(
                    value = doctorLocation,
                    onValueChange = { doctorLocation = it },
                    placeholder = "Completează cu spitalul, doctorul cu care te-ai consultat...",
                    isError = isError
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                DialogFieldLabel("Observații clinice")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogMultilineField(
                    value = clinicalObservations,
                    onValueChange = { clinicalObservations = it },
                    placeholder = "Introduceți observațiile medicului...",
                    minLines = 4,
                    isError = isError
                )
            }

            // === FIXED FOOTER ===
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
                        text = "Anulează",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onSave(title, doctorLocation, clinicalObservations, selectedDocumentIds.filter { it.isNotEmpty() }) },
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo600,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Salvează",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
