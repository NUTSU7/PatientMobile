package com.estcomputer.patient.feature.patient.medicalHistory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estcomputer.patient.designsystem.common.DashboardSpacing
import com.estcomputer.patient.designsystem.theme.AppDimens
import com.estcomputer.patient.designsystem.theme.AttentionHigh
import com.estcomputer.patient.designsystem.theme.GrayLightBorder
import com.estcomputer.patient.designsystem.theme.Gray500
import com.estcomputer.patient.designsystem.theme.Gray900
import com.estcomputer.patient.designsystem.theme.Indigo600
import com.estcomputer.patient.designsystem.theme.icons.TrashIcon
import com.estcomputer.patient.feature.patient.medicalHistory.MedicalHistoryUiState
import com.estcomputer.patient.feature.patient.medicalHistory.MedicineListEntry
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val importDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/** Collapsible "TRATAMENT · N MEDICAMENTE" group (web `.mh-treatment-group`). */
@Composable
internal fun TreatmentGroupCard(
    group: MedicineListEntry.Treatment,
    state: MedicalHistoryUiState,
    spacing: DashboardSpacing,
    expanded: Boolean,
    onToggle: (expanded: Boolean) -> Unit,
    onOpenClick: () -> Unit,
    onDeleteTreatmentClick: () -> Unit,
    onDeleteMedicineClick: (String) -> Unit,
    onEditMedicineClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val count = group.items.size
    val importDate = group.items
        .mapNotNull { it.createdAt }
        .minOrNull()
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDate()
        ?.format(importDateFormatter)
        ?: "Dat\u0103 neprecizat\u0103"
    val analysisSummary = group.items
        .flatMap { it.analysisDocumentIds.ifEmpty { listOfNotNull(it.analysisDocumentId) } }
        .distinct()
        .mapNotNull { id -> state.documentDisplayNames[id] ?: state.allDocuments.find { it.id == id }?.originalFileName }
        .joinToString(", ")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GrayLightBorder, shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.treatmentCardHeaderHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenClick)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = "TRATAMENT \u00B7 $count " + if (count == 1) "MEDICAMENT" else "MEDICAMENTE",
                    color = Indigo600,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = group.name.ifBlank { "Tratament" },
                    color = Gray900,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "Import: $importDate", color = Gray500, fontSize = 12.sp)
                if (analysisSummary.isNotEmpty()) {
                    Text(
                        text = "Analize asociate: $analysisSummary",
                        color = Gray500,
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggle(!expanded) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Restr\u00E2nge tratamentul" else "Extinde tratamentul",
                    tint = Gray500,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AttentionHigh.copy(alpha = 0.08f))
                    .clickable(onClick = onDeleteTreatmentClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TrashIcon,
                    contentDescription = "\u0218terge tratamentul",
                    tint = AttentionHigh,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (expanded) {
            Text(text = "Grupare personal\u0103 de medicamente.", color = Gray500, fontSize = 12.sp)
            group.items.forEach { item ->
                MedicineItemRow(
                    item = item,
                    onDeleteClick = { onDeleteMedicineClick(item.id) },
                    onClick = { onEditMedicineClick(item.id) }
                )
            }
        }
    }
}
