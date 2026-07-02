package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.components.ErrorDialog
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.BasicIndicatorsCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.EmptyUploadCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.HealthScoreCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.IndicatorDetailSheet
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.ProcessingEmptyCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.ResumeAICard
import com.semanticsoft.patientmobile.ui.shared.upload.ProcessingPollState
import com.semanticsoft.patientmobile.ui.shared.upload.components.ProcessingBanner
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.statusFillColor
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    processingPollState: ProcessingPollState = ProcessingPollState(),
    onUploadClick: () -> Unit = {}
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val dismissedError = remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var activeStatusFilter by rememberSaveable { mutableStateOf("Toate") }
    var selectedDetailIndicator by remember { mutableStateOf<com.semanticsoft.patientmobile.data.model.BasicIndicatorItem?>(null) }

    val allIndicators = state.basicIndicators

    val filteredIndicators = when (activeStatusFilter) {
        "Aten\u021Bie" -> allIndicators.filter { it.status == IndicatorStatus.ATTENTION }
        "La limit\u0103" -> allIndicators.filter { it.status == IndicatorStatus.BORDERLINE }
        "Optim" -> allIndicators.filter { it.status == IndicatorStatus.NORMAL }
        "F\u0103r\u0103 status" -> allIndicators.filter { it.status == IndicatorStatus.NO_REFERENCE }
        else -> allIndicators
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Crossfade(
            targetState = state.isLoading && state.greetingName.isEmpty(),
            animationSpec = tween(300),
            label = "DashboardTransition"
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(message = "Se \u00EEncarc\u0103...")
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBackground
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (false && processingPollState.isPolling) {
                            ProcessingBanner(
                                state = processingPollState,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (state.emptyReason != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = horizontalPadding,
                                            end = horizontalPadding,
                                            bottom = spacing.sectionGap + bottomInset
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val reason = state.emptyReason
                                    if (reason == DashboardEmptyReason.NO_DOCUMENTS || reason == null) {
                                        EmptyUploadCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            onUploadClick = onUploadClick
                                        )
                                    } else {
                                        ProcessingEmptyCard(
                                            reason = reason,
                                            modifier = Modifier.fillMaxWidth(),
                                            onUploadClick = onUploadClick
                                        )
                                    }
                                }
                            } else if (!state.hasUploadedDocuments) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = horizontalPadding,
                                            end = horizontalPadding,
                                            bottom = spacing.sectionGap + bottomInset
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyUploadCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onUploadClick = onUploadClick
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        top = spacing.sectionGap,
                                        bottom = bottomInset + spacing.bottomSpacer
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
                                ) {
                                    item {
                                        BoxWithConstraints(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = horizontalPadding)
                                        ) {
                                            val stackCards = maxWidth < 660.dp
                                            if (stackCards) {
                                                Column(verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)) {
                                                    HealthScoreCard(
                                                        score = state.markerSummary.score,
                                                        normalCount = state.markerSummary.normal,
                                                        borderlineCount = state.markerSummary.borderline,
                                                        attentionCount = state.markerSummary.attention,
                                                        noReferenceCount = state.markerSummary.noReference,
                                                        documentCount = state.documentCount,
                                                        statusText = "",
                                                        onStatusFilterClick = { filter ->
                                                            activeStatusFilter = filter?.let {
                                                                when (it) {
                                                                    "ATTENTION" -> "Aten\u021Bie"
                                                                    "BORDERLINE" -> "La limit\u0103"
                                                                    "NORMAL" -> "Optim"
                                                                    "NO_REFERENCE" -> "F\u0103r\u0103 status"
                                                                    else -> "Toate"
                                                                }
                                                            } ?: "Toate"
                                                            coroutineScope.launch {
                                                                listState.animateScrollToItem(1)
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    ResumeAICard(
                                                        summaryText = state.aiSummary,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        isLoading = state.isAiSummaryLoading
                                                    )
                                                }
                                            } else {
                                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sectionGap)) {
                                                    HealthScoreCard(
                                                        score = state.markerSummary.score,
                                                        normalCount = state.markerSummary.normal,
                                                        borderlineCount = state.markerSummary.borderline,
                                                        attentionCount = state.markerSummary.attention,
                                                        noReferenceCount = state.markerSummary.noReference,
                                                        documentCount = state.documentCount,
                                                        statusText = "",
                                                        onStatusFilterClick = { filter ->
                                                            activeStatusFilter = filter?.let {
                                                                when (it) {
                                                                    "ATTENTION" -> "Aten\u021Bie"
                                                                    "BORDERLINE" -> "La limit\u0103"
                                                                    "NORMAL" -> "Optim"
                                                                    "NO_REFERENCE" -> "F\u0103r\u0103 status"
                                                                    else -> "Toate"
                                                                }
                                                            } ?: "Toate"
                                                            coroutineScope.launch {
                                                                listState.animateScrollToItem(1)
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    ResumeAICard(
                                                        summaryText = state.aiSummary,
                                                        modifier = Modifier.weight(1f),
                                                        isLoading = state.isAiSummaryLoading
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (allIndicators.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Indicatori",
                                                color = Color(0xFF111827),
                                                fontSize = 20.sp,
                                                lineHeight = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = horizontalPadding)
                                            )
                                        }

                                        item {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = horizontalPadding),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    listOf("Toate", "F\u0103r\u0103 status").forEach { filter ->
                                                        FilterPill(
                                                            label = filter,
                                                            selected = activeStatusFilter == filter,
                                                            onClick = { activeStatusFilter = filter }
                                                        )
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    listOf("Optim", "La limit\u0103", "Aten\u021Bie").forEach { filter ->
                                                        FilterPill(
                                                            label = filter,
                                                            selected = activeStatusFilter == filter,
                                                            onClick = { activeStatusFilter = filter }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        items(filteredIndicators, key = { "${it.title}_${it.value}" }) { indicator ->
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
                                                historyPoints = indicator.historyPoints,
                                                referenceLow = indicator.referenceLow,
                                                referenceHigh = indicator.referenceHigh,
                                                onClick = { selectedDetailIndicator = indicator },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = horizontalPadding)
                                            )
                                        }
                                    }

                                    item {
                                        Text(
                                            text = "Adaug\u0103 mai multe fi\u0219iere pentru o analiz\u0103 mai detaliat\u0103.",
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = horizontalPadding, vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        state.errorMessage?.takeIf { it != dismissedError.value }?.let { message ->
            ErrorDialog(
                message = message,
                onDismiss = { dismissedError.value = message },
                onRetry = null,
                title = "Dashboard"
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data: SnackbarData ->
            Snackbar(data)
        }

        selectedDetailIndicator?.let { indicator ->
            IndicatorDetailSheet(
                title = indicator.title,
                value = indicator.value,
                unit = indicator.unit,
                status = indicator.status,
                historyPoints = indicator.historyPoints,
                referenceLow = indicator.referenceLow,
                referenceHigh = indicator.referenceHigh,
                color = statusFillColor(indicator.status),
                onDismiss = { selectedDetailIndicator = null }
            )
        }
    }
}

@Composable
private fun RowScope.FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = label,
        textAlign = TextAlign.Center,
        color = if (selected) Color.White else Color(0xFF4B5563),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Color(0xFF6366F1) else Color.White)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
