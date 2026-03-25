package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.AttentionModerate
import com.semanticsoft.patientmobile.ui.theme.HeaderBlue
import com.semanticsoft.patientmobile.ui.theme.HeaderBlueDark
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onUploadClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = when {
            maxWidth >= 600.dp -> 40.dp
            maxWidth >= 400.dp -> 24.dp
            else -> 16.dp
        }

        val scoreFont = when {
            maxWidth >= 600.dp -> 44.sp
            maxWidth >= 400.dp -> 40.sp
            else -> 34.sp
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(HeaderBlue, HeaderBlueDark)))
                            .padding(horizontal = horizontalPadding, vertical = 20.dp)
                    ) {
                        Text(
                            text = "Bună ziua, ${state.greetingName} 👋",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Ultima analiză: ${state.lastAnalysisDate} · ${state.markerSummary.attention} valori necesită atenție",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Scorul tău de sănătate", style = MaterialTheme.typography.titleLarge)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "${state.markerSummary.score}/100",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = scoreFont)
                                )
                                Text("Necesită atenție", color = AttentionModerate)
                            }
                            Text(
                                text = "${state.markerSummary.normal} normali · ${state.markerSummary.borderline} la limită · ${state.markerSummary.attention} atenție",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = onUploadClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Încarcă analiză nouă")
                            }
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
                            Text("AI · Rezumat pe înțelesul tău", style = MaterialTheme.typography.titleMedium)
                            Text(state.aiSummary, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Consultați medicul pentru interpretare medicală.",
                                style = MaterialTheme.typography.bodySmall
                            )
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
