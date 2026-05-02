package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.semanticsoft.patientmobile.ui.components.FilePickerButton
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.components.MedicalHistoryFiltersCard
import com.semanticsoft.patientmobile.ui.components.MedicalHistoryAnalysisSection
import com.semanticsoft.patientmobile.ui.components.MedicinesSection
import com.semanticsoft.patientmobile.ui.components.PersonalNotesSection
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500
import com.semanticsoft.patientmobile.ui.theme.TextSecondary

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
                        MedicalHistoryFiltersCard(
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
                                    MedicalHistoryAnalysisSection(state = state, spacing = spacing)
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
                                MedicalHistoryAnalysisSection(state = state, spacing = spacing)
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

