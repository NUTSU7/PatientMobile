package com.semanticsoft.patientmobile.ui.shared.upload.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.shared.upload.FileErrorIcon
import com.semanticsoft.patientmobile.ui.shared.upload.SelectedFile
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileUiState
import com.semanticsoft.patientmobile.ui.shared.upload.UploadStatus
import com.semanticsoft.patientmobile.ui.theme.icons.CloseIcon
import com.semanticsoft.patientmobile.ui.theme.icons.CloudDownloadIcon
import com.semanticsoft.patientmobile.ui.theme.icons.ImageFileIcon
import com.semanticsoft.patientmobile.ui.theme.icons.PdfDocumentIcon
import com.semanticsoft.patientmobile.ui.theme.icons.RefreshIcon

@Composable
fun UploadZone(
    state: UploadFileUiState,
    scale: Float,
    onAddMoreClick: () -> Unit,
    onRemoveFile: (Int) -> Unit,
    onRetryFile: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val files = state.selectedFiles
    val baseHeight = if (scale >= 1.0f) 325f else 272f
    val zoneHeight = baseHeight * scale

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((zoneHeight).dp)
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape((12f * scale).dp)
            )
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color(0xFFA5B4FC),
                    cornerRadius = CornerRadius((12f * scale).dp.toPx(), (12f * scale).dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                )
            }
            .padding((16f * scale).dp),
        contentAlignment = if (files.isEmpty()) Alignment.Center else Alignment.TopStart
    ) {
        if (files.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(role = Role.Button) { onAddMoreClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size((48f * scale).dp)
                        .background(
                            color = Color(0xFFEEF2FF),
                            shape = RoundedCornerShape((9999f * scale).dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CloudDownloadIcon,
                        contentDescription = "Upload",
                        modifier = Modifier.size((24f * scale).dp),
                        tint = Color(0xFF6366F1)
                    )
                }

                Spacer(modifier = Modifier.height((12f * scale).dp))

                Text(
                    text = "Apas\u0103 pentru a selecta fi\u0219iere",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (13.6f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = (24f * scale).sp
                    ),
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height((4f * scale).dp))

                Text(
                    text = "PDF, JPG, JPEG, PNG",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (11.9f * scale).sp,
                        lineHeight = (20f * scale).sp
                    ),
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy((4f * scale).dp)
                ) {
                    itemsIndexed(files) { index, file ->
                        FileRow(
                            file = file,
                            scale = scale,
                            onRemove = { onRemoveFile(index) },
                            onRetry = { onRetryFile(index) }
                        )
                    }
                    if (state.errorFiles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height((4f * scale).dp))
                            Text(
                                text = "Fi\u0219iere respinse:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = (10.2f * scale).sp,
                                    lineHeight = (16f * scale).sp
                                ),
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(vertical = (2f * scale).dp)
                            )
                            state.errorFiles.forEach { err ->
                                Text(
                                    text = "${err.name}: ${err.message}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = (10.2f * scale).sp,
                                        lineHeight = (16f * scale).sp
                                    ),
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height((4f * scale).dp))

                Text(
                    text = "+ Adaug\u0103 mai multe",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (13.6f * scale).sp,
                        lineHeight = (24f * scale).sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF4F46E5),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onAddMoreClick() }
                        .padding(vertical = (2f * scale).dp)
                )
            }
        }
    }
}

@Composable
private fun FileRow(
    file: SelectedFile,
    scale: Float,
    onRemove: () -> Unit,
    onRetry: () -> Unit
) {
    val isDuplicateWarning = file.status == UploadStatus.PENDING_FORCE
    val isError = file.status == UploadStatus.ERROR
    val showRetry = isError && file.errorIcon == FileErrorIcon.RETRY
    val bgColor = when {
        isDuplicateWarning -> Color(0xFFFFF7ED)
        isError -> Color(0xFFFEF2F2)
        else -> Color.White
    }
    val accentColor = when {
        isDuplicateWarning -> Color(0xFFEA580C)
        isError -> Color(0xFFDC2626)
        else -> Color(0xFF9CA3AF)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = bgColor,
                shape = RoundedCornerShape((8f * scale).dp)
            )
            .padding((8f * scale).dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val isPdf = file.mimeType.contains("pdf", ignoreCase = true) ||
                file.name.endsWith(".pdf", ignoreCase = true)
            Icon(
                imageVector = if (isPdf) PdfDocumentIcon else ImageFileIcon,
                contentDescription = null,
                modifier = Modifier.size((24f * scale).dp),
                tint = when {
                    isError -> Color(0xFFDC2626)
                    isPdf -> Color(0xFFEF4444)
                    else -> Color(0xFF8B5CF6)
                }
            )

            Spacer(modifier = Modifier.width((10f * scale).dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name.middleTruncate(
                        maxLen = when {
                            scale >= 1.0f -> 28
                            else -> 22
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (11.9f * scale).sp,
                        lineHeight = (20f * scale).sp
                    ),
                    color = when {
                        isDuplicateWarning -> Color(0xFFEA580C)
                        isError -> Color(0xFFDC2626)
                        else -> Color(0xFF374151)
                    },
                    maxLines = 1
                )
                Text(
                    text = if (isDuplicateWarning || isError) (file.errorMessage ?: "Eroare") else formatFileSize(file.sizeBytes),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (11f * scale).sp,
                        lineHeight = (18f * scale).sp
                    ),
                    color = when {
                        isDuplicateWarning -> Color(0xFFEA580C)
                        isError -> Color(0xFFDC2626)
                        else -> Color(0xFF9CA3AF)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showRetry || isDuplicateWarning) {
                Icon(
                    imageVector = RefreshIcon,
                    contentDescription = "Re\u00EEncarc\u0103",
                    modifier = Modifier
                        .size((24f * scale).dp)
                        .clickable { onRetry() },
                    tint = Color(0xFFDC2626)
                )
                Spacer(modifier = Modifier.width((6f * scale).dp))
            }
            Icon(
                imageVector = CloseIcon,
                contentDescription = "Elimin\u0103",
                modifier = Modifier
                    .size((24f * scale).dp)
                    .clickable { onRemove() },
                tint = if (isError) Color(0xFFDC2626) else accentColor
            )
        }
    }
}

private fun String.middleTruncate(maxLen: Int = 24): String {
    if (length <= maxLen) return this
    val ext = substringAfterLast('.', "")
    val dotAndExt = if (ext.isNotEmpty() && ext.length < length) ".$ext" else ""
    val base = if (dotAndExt.isNotEmpty()) substringBeforeLast('.') else this
    val available = maxLen - dotAndExt.length - 3
    if (available <= 2) return take(maxLen - 3) + "..."
    val left = available / 2
    val right = available - left
    return "${base.take(left)}...${base.takeLast(right)}$dotAndExt"
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}
