package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MedicalHistoryAnalysisSection(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
        ) {
            CalendarIcon()
            Text(
                text = "Analize",
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            state.isLoading -> {
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

            state.filteredDocuments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray50, RoundedCornerShape(18.dp))
                        .padding(spacing.sectionGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nu există analize disponibile pentru filtrul selectat.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                DocumentList(
                    items = state.filteredDocuments.map { document ->
                        DocumentListEntry(
                            fileName = displayDocumentName(document),
                            uploadStatus = formatAnalysisDate(document),
                            resultsCount = 0
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.8f
        val iconColor = Indigo600
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = iconColor,
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(strokeWidth)
        )
        drawLine(
            color = iconColor,
            start = Offset(w * 0.12f, h * 0.38f),
            end = Offset(w * 0.88f, h * 0.38f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = iconColor,
            start = Offset(w * 0.32f, h * 0.1f),
            end = Offset(w * 0.32f, h * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = iconColor,
            start = Offset(w * 0.68f, h * 0.1f),
            end = Offset(w * 0.68f, h * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun displayDocumentName(document: PatientDocument): String {
    val originalName = document.originalFileName
    val dotIndex = originalName.lastIndexOf('.')
    return if (dotIndex > 0) originalName.substring(0, dotIndex) else originalName
}

private fun formatAnalysisDate(document: PatientDocument): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    return "Analiza din ${document.uploadedAt.atZone(ZoneId.systemDefault()).format(formatter)}"
}
