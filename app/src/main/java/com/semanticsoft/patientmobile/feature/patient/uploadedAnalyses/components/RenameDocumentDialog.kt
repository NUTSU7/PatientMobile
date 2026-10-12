package com.semanticsoft.patientmobile.feature.patient.uploadedAnalyses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.components.form.FormInput
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600

@Composable
fun RenameDocumentDialog(
    currentName: String,
    errorText: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val nameWithoutExt = stripExtension(currentName)
    var name by rememberSaveable { mutableStateOf(nameWithoutExt) }
    val isError = errorText != null

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingDefault)
        ) {
            Text(
                text = "Redenumește documentul",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            FormInput(
                value = name,
                placeholder = "Nume fișier",
                onValueChange = { name = it },
                isError = isError,
                errorMessage = errorText
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Anulează",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onSave(name) },
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo600,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Salvează",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

private fun stripExtension(fileName: String): String {
    val dotIndex = fileName.lastIndexOf('.')
    return if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
}
