package com.semanticsoft.patientmobile.ui.screens.upload

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.AppBackground

@Composable
fun UploadScreen(
    state: UploadUiState,
    onDocumentNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onUploadClick: () -> Unit,
    onBack: () -> Unit
) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Upload document", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Încarcă un document medical pentru analiză AI.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = state.documentName,
                            onValueChange = onDocumentNameChange,
                            label = { Text("Nume document") },
                            placeholder = { Text("ex: analize_martie_2026.pdf") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = onNotesChange,
                            label = { Text("Notițe") },
                            placeholder = { Text("Detalii opționale pentru analiză") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )

                        Button(
                            onClick = onUploadClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Încarcă pentru analiză")
                        }

                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Înapoi la dashboard")
                        }
                    }
                }
            }
        }
    }
}
