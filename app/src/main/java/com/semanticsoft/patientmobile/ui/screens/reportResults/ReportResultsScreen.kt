package com.semanticsoft.patientmobile.ui.screens.reportResults

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.components.ErrorDialog
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.screens.reportResults.components.ResultCard
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AttentionModerate
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo500
import com.semanticsoft.patientmobile.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.ui.theme.icons.WarningIcon

@Composable
fun ReportResultsScreen(
    state: ReportResultsUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)

    val title = buildString {
        if (state.observedAtLabel.isNotBlank()) {
            append("Analiza din ")
            append(state.observedAtLabel)
        } else if (state.reportTitle.isNotBlank()) {
            append(state.reportTitle)
        } else {
            append("Rezultate")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceWhite,
        topBar = {
            ScreenTopBar(
                titlePrefix = "",
                titleHighlight = title,
                titleSuffix = "",
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "\u00CEnchide"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = state.isLoading,
            label = "ReportResultsTransition"
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(message = "Se \u00EEncarc\u0103 rezultatele...")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 720.dp)
                            .padding(
                                start = AppDimens.paddingDefault,
                                end = AppDimens.paddingDefault,
                                top = AppDimens.paddingDefault,
                                bottom = AppDimens.paddingXXLarge
                            ),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.gapLarge)
                    ) {
                        if (state.requiresReview) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        AttentionModerate.copy(alpha = 0.12f),
                                        RoundedCornerShape(AppDimens.cornerRadiusMedium)
                                    )
                                    .padding(AppDimens.paddingMedium)
                            ) {
                                    Row(
                                    horizontalArrangement = Arrangement.spacedBy(AppDimens.gapSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = WarningIcon,
                                        contentDescription = null,
                                        tint = AttentionModerate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Necesit\u0103 verificare medical\u0103",
                                        color = AttentionModerate,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (state.isNonLabReport && state.summary != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(AppDimens.cornerRadiusMedium))
                                    .padding(AppDimens.paddingDefault)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppDimens.gapSmall)) {
                                    if (state.summary.isNotBlank()) {
                                        Text(
                                            text = state.summary,
                                            color = Gray900,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Text(
                                        text = "Acest tip de raport nu con\u021Bine rezultate tabelare.",
                                        color = Gray500,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        if (state.isEmpty && !state.isNonLabReport) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nu s-au g\u0103sit rezultate pentru aceast\u0103 analiz\u0103.",
                                    color = Gray500,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        state.groupedResults.forEach { group ->
                            Text(
                                text = group.groupName.uppercase(),
                                color = Indigo500,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                            group.results.forEach { item ->
                                ResultCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.errorMessage != null) {
        ErrorDialog(
            message = state.errorMessage,
            title = "Eroare",
            onDismiss = onClose,
            onRetry = onRetry
        )
    }
}
