package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.semanticsoft.patientmobile.ui.components.BasicIndicatorsCard
import com.semanticsoft.patientmobile.ui.components.ClinicalPillarCard
import com.semanticsoft.patientmobile.ui.components.GeneralMarkersCard
import com.semanticsoft.patientmobile.ui.components.HealthScoreCard
import com.semanticsoft.patientmobile.ui.components.MarkerOverviewSection
import com.semanticsoft.patientmobile.ui.components.ResumeAICard
import com.semanticsoft.patientmobile.ui.components.WarningCard
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.screens.uploadFile.UploadFileScreen
import com.semanticsoft.patientmobile.ui.screens.uploadFile.UploadFileViewModel
import com.semanticsoft.patientmobile.ui.theme.AppBackground

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onMenuClick: () -> Unit = {},
    onUploadClick: () -> Unit = {}
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var showUploadModal by rememberSaveable { mutableStateOf(false) }
    val uploadFileViewModel = androidx.lifecycle.viewmodel.compose.viewModel<UploadFileViewModel>()
    val selectedCategoryName = state.markerCategories.getOrNull(selectedCategoryIndex)?.name ?: "Toate"
    val filteredGeneralMarkers = if (selectedCategoryName.equals("Toate", ignoreCase = true)) {
        state.generalMarkerCards
    } else {
        state.generalMarkerCards.filter { it.category.equals(selectedCategoryName, ignoreCase = true) }
    }
    val visibleClinicalPillars = state.clinicalPillarCards.filter { it.reportCount > 0 }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .height(141.dp)
                    ) {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = (horizontalPadding - 8.dp), y = 50.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Meniu",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = horizontalPadding + 48.dp, top = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Bună ziua, ",
                                    color = Color(0xFF111827),
                                    fontSize = 20.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = state.greetingName,
                                    color = Color(0xFF111827),
                                    fontSize = 20.sp,
                                    lineHeight = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "👋",
                                fontSize = 20.sp,
                                lineHeight = 32.sp
                            )

                            Text(
                                text = "Ultima analiză: ${state.lastAnalysisDate} · ${state.markerSummary.attention} valori",
                                color = Color(0xFF6B7280),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "necesită atenție",
                                color = Color(0xFF6B7280),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        IconButton(
                            onClick = { showUploadModal = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = -horizontalPadding, y = 50.dp)
                                .width(32.dp)
                                .height(40.dp)
                                .background(Color(0xFF5A52E5), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Adaugă",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Warning Cards - above Health Score
                items(state.warningCards) { warningCard ->
                    WarningCard(
                        warningCard = warningCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                    )
                }

                item {
                    HealthScoreCard(
                        score = state.markerSummary.score,
                        normalCount = state.markerSummary.normal,
                        borderlineCount = state.markerSummary.borderline,
                        attentionCount = state.markerSummary.attention,
                        statusText = "Necesită atenție",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                    )
                }

                item {
                    ResumeAICard(
                        summaryText = state.aiSummary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                    )
                }

                if (state.basicIndicators.isNotEmpty()) {
                    item {
                        Text(
                            text = "Indicatori de bază",
                            color = Color(0xFF111827),
                            fontSize = 17.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        )
                    }

                    items(state.basicIndicators) { indicator ->
                        BasicIndicatorsCard(
                            title = indicator.title,
                            value = indicator.value,
                            unit = indicator.unit,
                            status = indicator.status,
                            trendDirection = indicator.trendDirection,
                            trendDelta = indicator.trendDelta,
                            trendDescription = indicator.trendDescription,
                            markerPosition = indicator.markerPosition,
                            segments = indicator.segments,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                        )
                    }

                    if (state.markerCategories.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding),
                                verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
                            ) {
                                Text(
                                    text = "Prezentare generală markeri",
                                    color = Color(0xFF111827),
                                    fontSize = 17.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                MarkerOverviewSection(
                                    categories = state.markerCategories,
                                    selectedIndex = selectedCategoryIndex,
                                    onCategorySelected = { selectedCategoryIndex = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        items(filteredGeneralMarkers) { markerCard ->
                            GeneralMarkersCard(
                                title = markerCard.title,
                                category = markerCard.category,
                                value = markerCard.value,
                                unit = markerCard.unit,
                                status = markerCard.status,
                                normalRange = markerCard.normalRange,
                                borderlineRange = markerCard.borderlineRange,
                                attentionRange = markerCard.attentionRange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                            )
                        }
                    }
                }

                if (visibleClinicalPillars.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding),
                            verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
                        ) {
                            Text(
                                text = "Piloni clinici",
                                color = Color(0xFF111827),
                                fontSize = 17.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            val rows = visibleClinicalPillars.chunked(2)
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.clinicalGridGap)) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(spacing.clinicalGridGap)
                                    ) {
                                        rowItems.forEach { item ->
                                            ClinicalPillarCard(
                                                item = item,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(spacing.bottomSpacer)) }
            }
        }

        // Upload File Modal
        if (showUploadModal) {
            UploadFileScreen(
                state = uploadFileViewModel.state,
                viewModel = uploadFileViewModel,
                onDismiss = { showUploadModal = false }
            )
        }
    }
}
