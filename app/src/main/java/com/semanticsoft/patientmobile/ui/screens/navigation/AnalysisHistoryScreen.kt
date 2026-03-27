package com.semanticsoft.patientmobile.ui.screens.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.theme.AppBackground

@Composable
fun AnalysisHistoryScreen(
    onMenuClick: () -> Unit,
    onRetry: () -> Unit,
    viewModel: AnalysisHistoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val uiState by viewModel.stateFlow.collectAsState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .height(120.dp)
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (horizontalPadding - 8.dp), y = 42.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Meniu",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Istoric Analize",
                        color = Color(0xFF111827),
                        fontSize = 20.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = horizontalPadding + 48.dp, top = 50.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = uiState) {
                        AnalysisHistoryUiState.Loading -> {
                            CircularProgressIndicator(color = Color(0xFF5A52E5))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Se încarcă istoricul analizelor...",
                                color = Color(0xFF6B7280),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        AnalysisHistoryUiState.Empty -> {
                            Text(
                                text = "Nu există analize în istoric.",
                                color = Color(0xFF6B7280),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                onRetry()
                                viewModel.loadHistory()
                            }) {
                                Text("Reîncearcă")
                            }
                        }

                        is AnalysisHistoryUiState.Error -> {
                            Text(
                                text = state.message,
                                color = Color(0xFFB91C1C),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                onRetry()
                                viewModel.loadHistory()
                            }) {
                                Text("Reîncearcă")
                            }
                        }

                        is AnalysisHistoryUiState.Data -> {
                            Text(
                                text = "Documente analizate: ${state.documents.size}",
                                color = Color(0xFF111827),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Rezultate totale: ${state.resultsByDocumentId.values.sumOf { it.size }}",
                                color = Color(0xFF6B7280),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = {
                                onRetry()
                                viewModel.loadHistory()
                            }) {
                                Text("Actualizează")
                            }
                        }
                    }
                }
            }
        }
    }
}
