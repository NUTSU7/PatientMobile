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
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.DocumentList
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.DocumentListEntry
import com.semanticsoft.patientmobile.ui.components.FilePickerButton
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicalHistoryFiltersCard
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicalHistoryAnalysisSection
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicalHistoryTopBar
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.MedicinesSection
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.PersonalNotesSection
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

