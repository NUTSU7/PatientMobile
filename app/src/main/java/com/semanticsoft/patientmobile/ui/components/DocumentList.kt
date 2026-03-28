package com.semanticsoft.patientmobile.ui.components

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
    onRefresh: (() -> Unit)? = null,
    onLoadNextPage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { item ->
            DocumentListItem(
                fileName = item.fileName,
                uploadStatus = item.uploadStatus,
                resultsCount = item.resultsCount
            )
        }

        if (onRefresh != null) {
            TextButton(onClick = onRefresh, modifier = Modifier.padding(top = 4.dp)) {
                Text(text = "Reîmprospătează", color = Color(0xFF4F46E5))
            }
        }

        if (onLoadNextPage != null) {
            TextButton(onClick = onLoadNextPage) {
                Text(text = "Încarcă mai multe", color = Color(0xFF4F46E5))
            }
        }
    }
}

data class DocumentListEntry(
    val fileName: String,
    val uploadStatus: String,
    val resultsCount: Int
)
