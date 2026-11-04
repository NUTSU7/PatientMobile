package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.feature.patient.medicalHistory.CombinedSectionTab
import com.semanticsoft.patientmobile.feature.patient.medicalHistory.MedicalHistoryUiState
import com.semanticsoft.patientmobile.feature.patient.medicalHistory.MedicineListEntry
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.GrayLightBg
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.Purple500
import com.semanticsoft.patientmobile.core.ui.theme.icons.NoteIcon

@Composable
fun CombinedTreatmentNotesCard(
    state: MedicalHistoryUiState,
    spacing: DashboardSpacing,
    onAddTreatmentClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onTabSelected: (CombinedSectionTab) -> Unit,
    onDeleteMedicineClick: (String) -> Unit,
    onDeleteTreatmentClick: (String) -> Unit,
    onToggleTreatment: (treatmentId: String, expanded: Boolean) -> Unit,
    onDeleteNoteClick: (String) -> Unit,
    onEditMedicineClick: (String) -> Unit,
    onEditNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab = state.selectedCombinedSectionTab

    val addClick = when (selectedTab) {
        CombinedSectionTab.TREATMENTS -> onAddTreatmentClick
        CombinedSectionTab.NOTES -> onAddNoteClick
    }
    val hasItems = when (selectedTab) {
        CombinedSectionTab.TREATMENTS -> state.filteredMedicines.isNotEmpty()
        CombinedSectionTab.NOTES -> state.filteredNotes.isNotEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
            ) {
                Icon(
                    imageVector = NoteIcon,
                    contentDescription = "Context Personal",
                    modifier = Modifier.size(22.dp),
                    tint = Purple500
                )
                Text(
                    text = "Context Personal",
                    color = Gray900,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (hasItems) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .border(1.dp, Indigo600, RoundedCornerShape(999.dp))
                        .clickable(onClick = addClick)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+ Adaug\u0103",
                        color = Indigo600,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrayLightBg, RoundedCornerShape(999.dp))
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isNotesSelected = selectedTab == CombinedSectionTab.NOTES
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isNotesSelected) Color.White else Color.Transparent
                    )
                    .clickable { onTabSelected(CombinedSectionTab.NOTES) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Notițe",
                    color = if (isNotesSelected) Gray900 else Gray500,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val isTreatmentsSelected = selectedTab == CombinedSectionTab.TREATMENTS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isTreatmentsSelected) Color.White else Color.Transparent
                    )
                    .clickable { onTabSelected(CombinedSectionTab.TREATMENTS) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tratamente",
                    color = if (isTreatmentsSelected) Gray900 else Gray500,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            CombinedSectionTab.TREATMENTS -> {
                if (state.filteredMedicines.isEmpty()) {
                    TreatmentsEmptyState(
                        spacing = spacing,
                        onAddClick = onAddTreatmentClick
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                        state.medicineEntries.forEach { entry ->
                            when (entry) {
                                is MedicineListEntry.Single -> MedicineItemRow(
                                    item = entry.item,
                                    onDeleteClick = { onDeleteMedicineClick(entry.item.id) },
                                    onClick = { onEditMedicineClick(entry.item.id) }
                                )
                                is MedicineListEntry.Treatment -> TreatmentGroupCard(
                                    group = entry,
                                    state = state,
                                    spacing = spacing,
                                    expanded = state.treatmentExpanded[entry.treatmentId] ?: false,
                                    onToggle = { expanded -> onToggleTreatment(entry.treatmentId, expanded) },
                                    onOpenClick = { onEditMedicineClick(entry.items.first().id) },
                                    onDeleteTreatmentClick = { onDeleteTreatmentClick(entry.treatmentId) },
                                    onDeleteMedicineClick = onDeleteMedicineClick,
                                    onEditMedicineClick = onEditMedicineClick
                                )
                            }
                        }
                    }
                }
            }
            CombinedSectionTab.NOTES -> {
                if (state.filteredNotes.isEmpty()) {
                    NotesEmptyState(
                        spacing = spacing,
                        onAddClick = onAddNoteClick
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
                    ) {
                        state.filteredNotes.forEach { note ->
                            val docIds = note.analysisDocumentIds.ifEmpty { listOfNotNull(note.analysisDocumentId) }
                            val noteAnalysisSummary = docIds
                                .distinct()
                                .mapNotNull { id ->
                                    state.documentDisplayNames[id] ?: state.allDocuments.find { it.id == id }?.originalFileName
                                }
                                .joinToString(", ")
                                .ifBlank { note.analysisSummary }

                            PersonalNoteCard(
                                note = note,
                                analysisSummary = noteAnalysisSummary,
                                onDeleteClick = { onDeleteNoteClick(note.id) },
                                onClick = { onEditNoteClick(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreatmentsEmptyState(
    spacing: DashboardSpacing,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.sectionGap),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Purple500)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Adaug\u0103 tratament",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NotesEmptyState(
    spacing: DashboardSpacing,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.sectionGap),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Purple500)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Adaug\u0103 noti\u021B\u0103 personal\u0103",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
