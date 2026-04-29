package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.ui.components.DocumentList
import com.semanticsoft.patientmobile.ui.components.DocumentListEntry
import com.semanticsoft.patientmobile.ui.components.EmptyUploadCard
import com.semanticsoft.patientmobile.ui.components.FilePickerButton
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Gray100
import com.semanticsoft.patientmobile.ui.theme.Gray200
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo200
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Indigo700
import com.semanticsoft.patientmobile.ui.theme.Purple500
import com.semanticsoft.patientmobile.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen
import com.semanticsoft.patientmobile.ui.theme.TextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MedicalHystoryScreen(
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit,
    viewModel: MedicalHystoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding
        val isWideLayout = maxWidth >= 1024.dp

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MedicalHistoryTopBar(
                    state = state,
                    horizontalPadding = horizontalPadding,
                    onMenuClick = onMenuClick,
                    onNotificationsClick = onNotificationsClick,
                    onInfoClick = onInfoClick,
                    viewModel = viewModel
                )

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
                        FiltersCard(
                            state = state,
                            spacing = spacing,
                            onTimelineValueSelected = viewModel::onTimelineValueSelected,
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
                                    AnalysisSection(state = state, spacing = spacing)
                                    MedicinesSection(state = state, spacing = spacing, onAddClick = {
                                        viewModel.addMedicine("Ibuprofen", "La nevoie", 10)
                                    })
                                }

                                Box(modifier = Modifier.width(400.dp)) {
                                    PersonalNotesSection(state = state, spacing = spacing, onAttachFileClick = {
                                        viewModel.attachFile(
                                            PatientDocument(
                                                id = "demo-new-${System.currentTimeMillis()}",
                                                ownerUserId = "demo-user",
                                                originalFileName = "Atașament.jpg",
                                                mimeType = "image/jpeg",
                                                fileSizeBytes = 500_000,
                                                uploadedAt = java.time.Instant.now(),
                                                syncStatus = com.semanticsoft.patientmobile.domain.model.SyncStatus.SYNCED
                                            )
                                        )
                                    }, onAddNoteClick = {
                                        viewModel.addNote("Notiță nouă", "Aceasta este o notiță rapidă adăugată din interfață.")
                                    })
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                                AnalysisSection(state = state, spacing = spacing)
                                MedicinesSection(state = state, spacing = spacing, onAddClick = {
                                    viewModel.addMedicine("Ibuprofen", "La nevoie", 10)
                                })
                                PersonalNotesSection(state = state, spacing = spacing, onAttachFileClick = {
                                    viewModel.attachFile(
                                        PatientDocument(
                                            id = "demo-new-${System.currentTimeMillis()}",
                                            ownerUserId = "demo-user",
                                            originalFileName = "Atașament.jpg",
                                            mimeType = "image/jpeg",
                                            fileSizeBytes = 500_000,
                                            uploadedAt = java.time.Instant.now(),
                                            syncStatus = com.semanticsoft.patientmobile.domain.model.SyncStatus.SYNCED
                                        )
                                    )
                                }, onAddNoteClick = {
                                    viewModel.addNote("Notiță nouă", "Aceasta este o notiță rapidă adăugată din interfață.")
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalHistoryTopBar(
    state: MedicalHystoryUiState,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit,
    viewModel: MedicalHystoryViewModel
) {
    val lastAnalysisDate = state.analysisDocuments
        .maxByOrNull { it.uploadedAt }
        ?.uploadedAt
        ?.toString()
        ?.substringBefore("T")

    ScreenTopBar(
        horizontalPadding = horizontalPadding,
        onMenuClick = onMenuClick,
        titleContent = { dimensions ->
            Text(
                text = "Istoric ",
                color = Color(0xFF111827),
                fontSize = dimensions.titleSize,
                lineHeight = dimensions.titleLineHeight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "medical",
                color = Color(0xFF4F46E5),
                fontSize = dimensions.titleSize,
                lineHeight = dimensions.titleLineHeight,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        titleTrailingContent = { dimensions ->
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(dimensions.actionButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notificări",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(dimensions.actionIconSize)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(dimensions.actionButtonSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Ajutor",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(dimensions.actionIconSize)
                )
            }
        },
        subtitleContent = {
            Text(
                text = lastAnalysisDate?.let { "Se pare că ai făcut ultimele analize pe $it" }
                    ?: "Se pare că ai făcut ultimele analize.",
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actionsContent = { dimensions ->
            Text(
                text = "Încarcă analize",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = dimensions.primaryActionTextSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(listOf(Indigo600, Purple500)))
                    .clickable {
                        viewModel.attachFile(
                            PatientDocument(
                                id = "demo-new-${System.currentTimeMillis()}",
                                ownerUserId = "demo-user",
                                originalFileName = "Analiză noua.pdf",
                                mimeType = "application/pdf",
                                fileSizeBytes = 500_000,
                                uploadedAt = java.time.Instant.now(),
                                syncStatus = com.semanticsoft.patientmobile.domain.model.SyncStatus.SYNCED
                            )
                        )
                    }
                    .padding(vertical = 12.dp)
            )
        }
    )
}

@Composable
private fun FiltersCard(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onTimelineValueSelected: (Int?) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    val months = listOf("Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Noi", "Dec")
    val years = if (state.availableYears.isNotEmpty()) state.availableYears else listOf(state.selectedYear)
    var yearMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
    ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { yearMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray50,
                        contentColor = Gray900
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.selectedYear.toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = yearMenuExpanded,
                    onDismissRequest = { yearMenuExpanded = false }
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year.toString()) },
                            onClick = {
                                yearMenuExpanded = false
                                onYearSelected(year)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
            ) {
                months.forEachIndexed { index, month ->
                    val isSelected = state.selectedTimelineValue == index + 1
                    Box(
                        modifier = Modifier
                            .widthIn(min = 34.dp)
                            .clickable { onTimelineValueSelected(index + 1) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month,
                            color = if (isSelected) Gray900 else Gray500,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

@Composable
private fun AnalysisSection(state: MedicalHystoryUiState, spacing: DashboardSpacing) {
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
private fun MedicinesSection(state: MedicalHystoryUiState, spacing: DashboardSpacing, onAddClick: () -> Unit) {
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
                PillIcon(isVertical = true)
                Text(
                    text = "Medicamente",
                    color = Gray900,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            FilePickerButton(
                label = "+ Adaugă medicament",
                onClick = onAddClick,
                buttonHeight = 44.dp,
                cornerRadius = 999.dp,
                textSize = 13.sp,
                backgroundBrush = Brush.horizontalGradient(listOf(Indigo600, Purple500))
            )

            if (state.medicines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray50, RoundedCornerShape(18.dp))
                        .padding(spacing.sectionGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nu există medicamente adăugate.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                    state.medicines.forEach { medicine ->
                        MedicineItemRow(item = medicine)
                    }
                }
            }
        }
    }

@Composable
private fun AddMedicationButton() {
    Button(
        onClick = { },
        modifier = Modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
        shape = RoundedCornerShape(999.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "+ Adaugă medicament",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PersonalNotesSection(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onAttachFileClick: () -> Unit,
    onAddNoteClick: () -> Unit
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
                FileTextIcon()
                Text(
                    text = "Notițe personale",
                    color = Gray900,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray50, RoundedCornerShape(18.dp))
                        .padding(spacing.sectionGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nu există notițe personale adăugate.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
                ) {
                    state.notes.forEach { note ->
                        PersonalNoteCard(note = note)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                FilePickerButton(
                    label = "Atașează fișier",
                    onClick = onAttachFileClick,
                    buttonHeight = 48.dp,
                    cornerRadius = 16.dp,
                    textSize = 14.sp,
                    dashed = true,
                    dashedColor = Gray500,
                    contentColor = Gray900,
                    containerColor = Color.Transparent
                )
                FilePickerButton(
                    label = "+ Adaugă notiță personală",
                    onClick = onAddNoteClick,
                    buttonHeight = 48.dp,
                    cornerRadius = 16.dp,
                    textSize = 14.sp,
                    backgroundBrush = Brush.horizontalGradient(listOf(Indigo600, Purple500))
                )
            }
        }
    }

@Composable
private fun MedicineItemRow(item: MedicineItem) {
    val badgeType = resolveMedicineBadgeType(item)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicineBadge(type = badgeType)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    color = Gray900,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.schedule,
                    color = Gray500,
                    fontSize = 14.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIV",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${item.daysRemaining} zile rămase",
                        color = Gray500,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalNoteCard(note: PersonalNoteItem) {
    val accentColor = when (note.severity) {
        NoteSeverity.GOOD -> SuccessGreen
        NoteSeverity.OK -> Indigo600
        NoteSeverity.BAD -> AttentionHigh
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = note.title,
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = note.content,
                    color = Gray900,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "— ${note.author}",
                        color = Gray500,
                        fontSize = 11.sp
                    )
                    Text(
                        text = note.dateLabel,
                        color = Gray500,
                        fontSize = 11.sp
                    )
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
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    return "Analiza din ${document.uploadedAt.atZone(ZoneId.systemDefault()).format(formatter)}"
}

private enum class MedicineBadgeType {
    PILL,
    CAPSULE,
    SPRAY,
    SYRUP,
    INJECTION,
    DROPS
}

private fun resolveMedicineBadgeType(item: MedicineItem): MedicineBadgeType {
    val descriptor = "${item.name} ${item.schedule}".lowercase()
    return when {
        descriptor.contains("spray") || descriptor.contains("inhal") -> MedicineBadgeType.SPRAY
        descriptor.contains("capsul") -> MedicineBadgeType.CAPSULE
        descriptor.contains("sirop") -> MedicineBadgeType.SYRUP
        descriptor.contains("inject") -> MedicineBadgeType.INJECTION
        descriptor.contains("picăt") || descriptor.contains("picat") || descriptor.contains("drops") -> MedicineBadgeType.DROPS
        else -> MedicineBadgeType.PILL
    }
}

@Composable
private fun MedicineBadge(type: MedicineBadgeType) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Indigo600.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val strokeWidth = 1.9f
            val iconColor = Indigo600
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            when (type) {
                MedicineBadgeType.PILL -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.3f, h * 0.15f),
                        size = Size(w * 0.4f, h * 0.7f),
                        cornerRadius = CornerRadius(w * 0.2f, w * 0.2f),
                        style = Stroke(strokeWidth)
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.5f, h * 0.15f),
                        end = Offset(w * 0.5f, h * 0.85f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                MedicineBadgeType.CAPSULE -> {
                    drawCircle(
                        color = iconColor,
                        radius = minOf(w, h) * 0.28f,
                        center = Offset(cx, cy),
                        style = Stroke(strokeWidth)
                    )
                }

                MedicineBadgeType.SPRAY -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.18f, h * 0.34f),
                        size = Size(w * 0.36f, h * 0.44f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(strokeWidth)
                    )
                }

                MedicineBadgeType.SYRUP -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.28f, h * 0.28f),
                        size = Size(w * 0.44f, h * 0.5f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(strokeWidth)
                    )
                }

                MedicineBadgeType.INJECTION -> {
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.24f, h * 0.74f),
                        end = Offset(w * 0.7f, h * 0.28f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                MedicineBadgeType.DROPS -> {
                    val dropPath = Path().apply {
                        moveTo(cx, h * 0.18f)
                        cubicTo(w * 0.72f, h * 0.34f, w * 0.74f, h * 0.56f, cx, h * 0.82f)
                        cubicTo(w * 0.26f, h * 0.56f, w * 0.28f, h * 0.34f, cx, h * 0.18f)
                        close()
                    }
                    drawPath(dropPath, color = iconColor, style = Stroke(strokeWidth))
                }
            }
        }
    }
}

@Composable
private fun CalendarIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.8f
        val iconColor = Indigo600
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = iconColor,
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = Size(w * 0.76f, h * 0.68f),
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

@Composable
private fun PillIcon(isVertical: Boolean = false) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.8f
        val iconColor = Indigo600
        val w = size.width
        val h = size.height

        if (isVertical) {
            drawRoundRect(
                color = iconColor,
                topLeft = Offset(w * 0.3f, h * 0.15f),
                size = Size(w * 0.4f, h * 0.7f),
                cornerRadius = CornerRadius(w * 0.2f, w * 0.2f),
                style = Stroke(strokeWidth)
            )
            drawLine(
                color = iconColor,
                start = Offset(w * 0.5f, h * 0.15f),
                end = Offset(w * 0.5f, h * 0.85f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        } else {
            drawRoundRect(
                color = iconColor,
                topLeft = Offset(w * 0.12f, h * 0.36f),
                size = Size(w * 0.76f, h * 0.28f),
                cornerRadius = CornerRadius(h * 0.16f, h * 0.16f),
                style = Stroke(strokeWidth)
            )
            drawLine(
                color = iconColor,
                start = Offset(w * 0.5f, h * 0.36f),
                end = Offset(w * 0.5f, h * 0.64f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun FileTextIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.8f
        val iconColor = Indigo600
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = iconColor,
            topLeft = Offset(w * 0.15f, h * 0.1f),
            size = Size(w * 0.7f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(strokeWidth)
        )

        val fold = Path().apply {
            moveTo(w * 0.6f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.3f)
            lineTo(w * 0.6f, h * 0.3f)
            close()
        }
        drawPath(path = fold, color = iconColor, style = Stroke(strokeWidth))

        repeat(3) { index ->
            val y = h * (0.42f + index * 0.16f)
            drawLine(
                color = iconColor,
                start = Offset(w * 0.28f, y),
                end = Offset(w * 0.72f, y),
                strokeWidth = strokeWidth * 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}