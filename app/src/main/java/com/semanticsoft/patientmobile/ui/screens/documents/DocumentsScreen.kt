package com.semanticsoft.patientmobile.ui.screens.documents

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DocumentsScreen(
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DocumentEvent.OpenFile -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, context.contentResolver.getType(uri) ?: "application/octet-stream")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (val state = listState) {
            is DocumentListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(message = "Se \u00EEncarc\u0103 documentele...")
                }
            }
            is DocumentListState.Success -> {
                Text(
                    text = "${state.documents.size} documente",
                    modifier = Modifier.padding(16.dp)
                )
            }
            is DocumentListState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }
        }
    }
}
