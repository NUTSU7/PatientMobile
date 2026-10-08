package com.semanticsoft.patientmobile.ui.shared.upload.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.shared.upload.ErrorType
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileUiState
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileViewModel
import com.semanticsoft.patientmobile.ui.shared.upload.UploadStatus
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500
import com.semanticsoft.patientmobile.ui.theme.icons.CloseIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadFileDialogContent(
    state: UploadFileUiState,
    viewModel: UploadFileViewModel,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFilePickerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPendingOrError = state.selectedFiles.any {
        it.status == UploadStatus.PENDING || it.status == UploadStatus.ERROR
    }
    val totalFiles = state.selectedFiles.size
    val uploadingIndex = state.selectedFiles.indexOfFirst { it.status == UploadStatus.UPLOADING }
    val currentFileNum = if (uploadingIndex >= 0) uploadingIndex + 1 else 0

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 540.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.paddingLarge)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "\u00CEncarc\u0103 analize",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            Text(
                text = "Adaug\u0103 rezultatele tale de laborator",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            UploadZone(
                state = state,
                onAddMoreClick = {
                    if (!state.isUploading) showBottomSheet = true
                },
                onRemoveFile = { index -> viewModel.removeFile(index) },
                onRetryFile = { index -> viewModel.retryFile(index) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppDimens.gapMedium))

            val systemicErrorMessage = state.selectedFiles.firstOrNull {
                it.errorType == ErrorType.SYSTEMIC
            }?.errorMessage

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (systemicErrorMessage != null) {
                    Text(
                        text = systemicErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB91C1C),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            val buttonEnabled = hasPendingOrError && !state.isUploading && !state.isSystemicError
            val isRetryState = state.hasNetworkError && !state.isUploading && hasPendingOrError
            val gradientColors = if (isRetryState) {
                listOf(Color(0xFFDC2626), Color(0xFFB91C1C))
            } else {
                listOf(Indigo600, Purple500)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.buttonHeightLarge)
                    .alpha(if (buttonEnabled) 1f else 0.5f)
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                        shape = RoundedCornerShape(AppDimens.cornerRadiusPill)
                    )
                    .clickable(enabled = buttonEnabled) { viewModel.startUpload() },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = when {
                        state.isUploading -> ButtonLabel.UPLOADING
                        isRetryState -> ButtonLabel.RETRY
                        else -> ButtonLabel.IDLE
                    },
                    animationSpec = tween(150)
                ) { label ->
                    when (label) {
                        ButtonLabel.UPLOADING -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Se \u00EEncarc\u0103 fi\u0219ierul $currentFileNum din $totalFiles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        ButtonLabel.RETRY -> {
                            Text(
                                text = "Eroare. Re\u00EEncearc\u0103?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        ButtonLabel.IDLE -> {
                            Text(
                                text = "\u00CEncarc\u0103",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.gapMedium))

            Text(
                text = "Po\u021Bi \u00EEnc\u0103rca: PDF, JPG, JPEG, PNG.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Max 10 MB per fi\u0219ier.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Selecteaz\u0103 sursa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = AppDimens.paddingXLarge,
                            vertical = AppDimens.paddingMedium
                        )
                    )

                    val items = listOf(
                        Triple(
                            "Document / Fi\u0219ier",
                            Icons.Default.Description,
                            { onFilePickerClick(); showBottomSheet = false }
                        ),
                        Triple(
                            "Camer\u0103",
                            Icons.Default.PhotoCamera,
                            { onCameraClick(); showBottomSheet = false }
                        ),
                        Triple(
                            "Galerie",
                            Icons.Default.Collections,
                            { onGalleryClick(); showBottomSheet = false }
                        )
                    )

                    items.forEach { (label, iconRef, onClickRef) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            leadingContent = {
                                Icon(
                                    imageVector = iconRef,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClickRef
                            )
                        )
                    }
                }
            }
        }
    }
}

private enum class ButtonLabel {
    IDLE,
    UPLOADING,
    RETRY
}