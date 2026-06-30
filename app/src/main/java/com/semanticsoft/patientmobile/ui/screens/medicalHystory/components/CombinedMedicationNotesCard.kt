package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components


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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.CombinedSectionTab
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryUiState
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.GrayLightBg
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500
import com.semanticsoft.patientmobile.ui.theme.icons.NoteIcon
import com.semanticsoft.patientmobile.ui.theme.icons.NotiteIcon
import com.semanticsoft.patientmobile.ui.theme.icons.PillIcon

@Composable
fun CombinedMedicationNotesCard(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onAddMedicineClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onTabSelected: (CombinedSectionTab) -> Unit,
    onDeleteMedicineClick: (String) -> Unit,
    onDeleteNoteClick: (String) -> Unit,
    onEditMedicineClick: (String) -> Unit,
    onEditNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab = state.selectedCombinedSectionTab

    val addClick = when (selectedTab) {
        CombinedSectionTab.MEDICINES -> onAddMedicineClick
        CombinedSectionTab.NOTES -> onAddNoteClick
    }
    val hasItems = when (selectedTab) {
        CombinedSectionTab.MEDICINES -> state.filteredMedicines.isNotEmpty()
        CombinedSectionTab.NOTES -> state.filteredNotes.isNotEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
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

        Row(
                modifier = Modifier
                    .background(GrayLightBg, RoundedCornerShape(999.dp))
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isNotesSelected = selectedTab == CombinedSectionTab.NOTES
                Box(
                    modifier = Modifier
                        .background(
                            if (isNotesSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(999.dp)
                        )
                        .clickable { onTabSelected(CombinedSectionTab.NOTES) }
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = NotiteIcon,
                            contentDescription = "Notite",
                            modifier = Modifier.size(20.dp),
                            tint = Purple500
                        )
                        Text(
                            text = "Notițe",
                            color = if (isNotesSelected) Gray900 else Gray500,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                val isMedSelected = selectedTab == CombinedSectionTab.MEDICINES
                Box(
                    modifier = Modifier
                        .background(
                            if (isMedSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(999.dp)
                        )
                        .clickable { onTabSelected(CombinedSectionTab.MEDICINES) }
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PillIcon(
                            modifier = Modifier.size(16.dp),
                            color = Purple500
                        )
                        Text(
                            text = "Medicamente",
                            color = if (isMedSelected) Gray900 else Gray500,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            CombinedSectionTab.MEDICINES -> {
                if (state.filteredMedicines.isEmpty()) {
                    MedicinesEmptyState(
                        spacing = spacing,
                        onAddClick = onAddMedicineClick
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                        state.filteredMedicines.forEach { medicine ->
                            MedicineItemRow(
                                item = medicine,
                                onDeleteClick = { onDeleteMedicineClick(medicine.id) },
                                onClick = { onEditMedicineClick(medicine.id) }
                            )
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
                            PersonalNoteCard(
                                note = note,
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
private fun MedicinesEmptyState(
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
                .background(Purple500, RoundedCornerShape(16.dp))
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Adaug\u0103 medicament",
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
                .background(Purple500, RoundedCornerShape(16.dp))
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
