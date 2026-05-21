package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryUiState
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.TextSecondary
import com.semanticsoft.patientmobile.ui.theme.icons.CalendarIcon
import java.time.ZoneId

@Composable
fun MedicalHistoryAnalysisSection(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onDocumentClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
        ) {
            Icon(
                imageVector = CalendarIcon,
                contentDescription = "Analize",
                modifier = Modifier.size(20.dp),
                tint = Indigo600
            )
            Text(
                text = "Analize",
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            state.isLoading && state.allDocuments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Indigo600,
                        strokeWidth = 2.dp
                    )
                }
            }

            state.allDocuments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray50, RoundedCornerShape(18.dp))
                        .padding(spacing.sectionGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nu exist\u0103 analize \u00EEnc\u0103rcate.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            state.filteredDocuments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray50, RoundedCornerShape(18.dp))
                        .padding(spacing.sectionGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nu exist\u0103 analize disponibile pentru filtrul selectat.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                    DocumentList(
                        items = state.filteredDocuments.map { document ->
                            DocumentListEntry(
                                documentId = document.id,
                                fileName = displayDocumentName(document),
                                uploadStatus = formatAnalysisDate(document),
                                resultsCount = state.documentResults[document.id]?.size ?: 0
                            )
                        },
                        onDocumentClick = onDocumentClick
                    )

                    state.selectedDocumentId?.let { docId ->
                        val results = state.documentResults[docId]
                        if (!results.isNullOrEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Gray50, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Rezultate OCR",
                                    color = Gray900,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                results.forEach { result ->
                                    Text(
                                        text = buildString {
                                            append(result.originalTestName)
                                            append(": ")
                                            append(result.valueNumeric ?: result.valueText ?: "-")
                                            append(" ")
                                            append(result.unit)
                                        },
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun displayDocumentName(document: PatientDocument): String {
    val originalName = document.originalFileName
    val dotIndex = originalName.lastIndexOf('.')
    return if (dotIndex > 0) originalName.substring(0, dotIndex) else originalName
}

private fun formatAnalysisDate(document: PatientDocument): String {
    val localDate = document.uploadedAt.atZone(ZoneId.systemDefault()).toLocalDate()
    val day = localDate.dayOfMonth
    val monthName = when (localDate.monthValue) {
        1 -> "ianuarie"
        2 -> "februarie"
        3 -> "martie"
        4 -> "aprilie"
        5 -> "mai"
        6 -> "iunie"
        7 -> "iulie"
        8 -> "august"
        9 -> "septembrie"
        10 -> "octombrie"
        11 -> "noiembrie"
        12 -> "decembrie"
        else -> ""
    }
    return "$day $monthName ${localDate.year}"
}
