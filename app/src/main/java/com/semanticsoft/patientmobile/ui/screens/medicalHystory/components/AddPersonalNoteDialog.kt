package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.Gray900

@Composable
fun AddPersonalNoteDialog(
    errorText: String?,
    documents: List<PatientDocument>,
    initialAnalysisDocumentId: String? = null,
    initialAnalysisDocumentName: String? = null,
    initialTitle: String? = null,
    initialDoctorLocation: String? = null,
    initialContent: String? = null,
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (title: String, doctorLocation: String, content: String, analysisDocumentId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    var selectedDocumentId by rememberSaveable { mutableStateOf(initialAnalysisDocumentId.orEmpty()) }
    var doctorLocation by rememberSaveable { mutableStateOf("") }
    var clinicalObservations by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialTitle) { initialTitle?.let { title = it } }
    LaunchedEffect(initialDoctorLocation) { initialDoctorLocation?.let { doctorLocation = it } }
    LaunchedEffect(initialContent) { initialContent?.let { clinicalObservations = it } }
    LaunchedEffect(initialAnalysisDocumentId) { initialAnalysisDocumentId?.let { selectedDocumentId = it } }

    val selectedDocumentLabel = when {
        selectedDocumentId.isEmpty() -> "F\u0103r\u0103 analiz\u0103 asociat\u0103"
        else -> documents.firstOrNull { it.id == selectedDocumentId }?.originalFileName ?: ""
    }

    val documentOptions = buildList {
        add(DialogDropdownOption(key = "", label = "F\u0103r\u0103 analiz\u0103 asociat\u0103"))
        if (initialAnalysisDocumentId != null && initialAnalysisDocumentId.isNotEmpty()) {
            val inList = documents.any { it.id == initialAnalysisDocumentId }
            if (!inList && initialAnalysisDocumentName != null) {
                add(DialogDropdownOption(key = initialAnalysisDocumentId, label = "${initialAnalysisDocumentName} (indisponibil\u0103)"))
            }
        }
        addAll(documents.map { doc ->
            DialogDropdownOption(key = doc.id, label = doc.originalFileName)
        })
    }

    val isError = errorText != null

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingDefault)
        ) {
            Text(
                text = if (isEditing) "Editeaz\u0103 noti\u021B\u0103" else "Noti\u021B\u0103 personal\u0103",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Titlu noti\u021B\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Analize s\u00EEnge (Profil complet)",
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Analiz\u0103 asociat\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogDropdownField(
                selectedText = selectedDocumentLabel,
                placeholder = "F\u0103r\u0103 analiz\u0103 asociat\u0103",
                options = documentOptions,
                onSelect = { option ->
                    if (option.key.isEmpty()) {
                        selectedDocumentId = ""
                    } else {
                        selectedDocumentId = option.key
                        if (title.isEmpty()) {
                            title = option.label
                        }
                    }
                },
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Doctor / Loca\u021Bie")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogTextField(
                value = doctorLocation,
                onValueChange = { doctorLocation = it },
                placeholder = "Completeaz\u0103 cu spitalul, doctorul cu care te-ai consultat...",
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Observa\u021Bii clinice")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogMultilineField(
                value = clinicalObservations,
                onValueChange = { clinicalObservations = it },
                placeholder = "Introduce\u021Bi observa\u021Biile medicului...",
                minLines = 4,
                isError = isError
            )

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
                TextButton(
                    onClick = { onSave(title, doctorLocation, clinicalObservations, selectedDocumentId.ifEmpty { null }) }
                ) {
                    Text(
                        text = "Salveaz\u0103",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
