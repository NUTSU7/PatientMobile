package com.semanticsoft.patientmobile.ui.shared.upload.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.shared.upload.ErrorType
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileUiState
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileViewModel
import com.semanticsoft.patientmobile.ui.shared.upload.UploadStatus
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
    scale: Float,
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
            .heightIn(max = (540f * scale).dp),
        shape = RoundedCornerShape((16f * scale).dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding((20f * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((36f * scale).dp)
                    .padding(bottom = (12f * scale).dp)
            ) {
                Text(
                    text = "\u00CEncarc\u0103 analize",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = (20.4f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (32f * scale).sp
                    ),
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size((36f * scale).dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Close",
                        modifier = Modifier.size((20f * scale).dp),
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            Text(
                text = "Adaug\u0103 rezultatele tale de laborator",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (13.6f * scale).sp,
                    lineHeight = (24f * scale).sp
                ),
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = (12f * scale).dp),
                textAlign = TextAlign.Center
            )

            UploadZone(
                state = state,
                scale = scale,
                onAddMoreClick = {
                    if (!state.isUploading) showBottomSheet = true
                },
                onRemoveFile = { index -> viewModel.removeFile(index) },
                onRetryFile = { index -> viewModel.retryFile(index) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height((8f * scale).dp))

            val systemicErrorMessage = state.selectedFiles.firstOrNull {
                it.errorType == ErrorType.SYSTEMIC
            }?.errorMessage

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((20f * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                if (systemicErrorMessage != null) {
                    Text(
                        text = systemicErrorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (11.9f * scale).sp,
                            lineHeight = (20f * scale).sp
                        ),
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
                    .height((46f * scale).dp)
                    .alpha(if (buttonEnabled) 1f else 0.5f)
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable(enabled = buttonEnabled) { viewModel.startUpload() },
                contentAlignment = Alignment.Center
            ) {
                if (state.isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size((24f * scale).dp),
                            strokeWidth = (2f * scale).dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width((8f * scale).dp))
                        Text(
                            text = "Se \u00EEncarc\u0103 fi\u0219ierul $currentFileNum din $totalFiles",
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = (20f * scale).sp
                            ),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                } else if (isRetryState) {
                    Text(
                        text = "Eroare. Re\u00EEncearc\u0103?",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = (14f * scale).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "\u00CEncarc\u0103",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = (14f * scale).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height((8f * scale).dp))

            Text(
                text = "Po\u021Bi \u00EEnc\u0103rca: PDF, JPG, JPEG, PNG.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (10.2f * scale).sp,
                    lineHeight = (16f * scale).sp
                ),
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Max 10 MB per fi\u0219ier.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (10.2f * scale).sp,
                    lineHeight = (16f * scale).sp
                ),
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(
                    topStart = (16f * scale).dp,
                    topEnd = (16f * scale).dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = (32f * scale).dp)
                ) {
                    Text(
                        text = "Selecteaz\u0103 sursa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = (24f * scale).dp,
                            vertical = (12f * scale).dp
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
                                    modifier = Modifier.size((24f * scale).dp)
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
