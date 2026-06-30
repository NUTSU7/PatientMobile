package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.ErrorLightBg
import com.semanticsoft.patientmobile.ui.theme.ErrorText
import com.semanticsoft.patientmobile.ui.theme.Gray400
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.InputBackground

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.inputHeightDefault)
                    .background(
                        color = if (isError) ErrorLightBg else InputBackground,
                        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
                    )
                    .padding(horizontal = AppDimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (name.isEmpty()) {
                        Text(
                            text = "Nume fișier",
                            color = if (isError) ErrorText else Gray400,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = TextStyle(
                            color = if (isError) ErrorText else Gray900,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

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
                TextButton(onClick = { onSave(name) }) {
                    Text(
                        text = "Salvează",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
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
