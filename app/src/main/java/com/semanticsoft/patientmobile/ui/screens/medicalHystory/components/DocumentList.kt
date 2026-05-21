package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DocumentList(
    items: List<DocumentListEntry>,
    onDocumentClick: (String) -> Unit,
    onRefresh: (() -> Unit)? = null,
    onLoadNextPage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            DocumentListItem(
                fileName = item.fileName,
                uploadStatus = item.uploadStatus,
                resultsCount = item.resultsCount,
                onClick = { onDocumentClick(item.documentId) }
            )
        }

        if (onRefresh != null) {
            TextButton(onClick = onRefresh, modifier = Modifier.padding(top = 4.dp)) {
                Text(text = "Re\u00EEmprosp\u0103teaz\u0103", color = Color(0xFF4F46E5))
            }
        }

        if (onLoadNextPage != null) {
            TextButton(onClick = onLoadNextPage) {
                Text(text = "\u00CEncarc\u0103 mai multe", color = Color(0xFF4F46E5))
            }
        }
    }
}

data class DocumentListEntry(
    val documentId: String,
    val fileName: String,
    val uploadStatus: String,
    val resultsCount: Int
)
