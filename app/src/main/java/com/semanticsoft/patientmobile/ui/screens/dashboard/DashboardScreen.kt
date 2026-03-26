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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.ui.components.BasicIndicatorsCard
import com.semanticsoft.patientmobile.ui.components.GeneralMarkersCard
import com.semanticsoft.patientmobile.ui.components.HealthScoreCard
import com.semanticsoft.patientmobile.ui.components.MarkerOverviewSection
import com.semanticsoft.patientmobile.ui.components.ResumeAICard
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.AttentionModerate
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onUploadClick: () -> Unit
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedCategoryName = state.markerCategories.getOrNull(selectedCategoryIndex)?.name ?: "Toate"
    val filteredGeneralMarkers = if (selectedCategoryName.equals("Toate", ignoreCase = true)) {
        state.generalMarkerCards
    } else {
        state.generalMarkerCards.filter { it.category.equals(selectedCategoryName, ignoreCase = true) }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = when {
            maxWidth >= 600.dp -> 40.dp
            maxWidth >= 400.dp -> 24.dp
            else -> 16.dp
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .height(141.dp)
                    ) {
                        IconButton(
                            onClick = {},
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
                            onClick = onUploadClick,
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
                            style = MaterialTheme.typography.titleLarge,
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
                                verticalArrangement = Arrangement.spacedBy(10.dp)
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

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Atenție", style = MaterialTheme.typography.titleMedium)
                            state.attentionItems.forEach { item ->
                                AttentionRow(item = item)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun AttentionRow(item: AttentionItem) {
    val color = when (item.severity) {
        "Atenție" -> AttentionHigh
        "Atenție moderată" -> AttentionModerate
        else -> SuccessGreen
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text("${item.marker}: ${item.value} ${item.unit}")
        }
        Text(item.severity, color = color)
    }
}
