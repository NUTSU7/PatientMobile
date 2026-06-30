package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh

@Composable
fun DeleteDocumentDialog(
    documentName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    bulkCount: Int? = null,
    isLoading: Boolean = false
) {
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
                text = if (bulkCount != null) "Șterge documentele" else "Șterge documentul",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            Text(
                text = if (bulkCount != null) "Ești sigur că vrei să ștergi $bulkCount documente? Această acțiune nu poate fi anulată." else "Ești sigur că vrei să ștergi \"$documentName\"? Această acțiune nu poate fi anulată.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text(
                        text = "Anulează",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = AttentionHigh
                    )
                }
                TextButton(onClick = onConfirm, enabled = !isLoading) {
                    Text(
                        text = "Șterge",
                        style = MaterialTheme.typography.labelLarge,
                        color = AttentionHigh
                    )
                }
            }
        }
    }
}
