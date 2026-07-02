package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.semanticsoft.patientmobile.ui.theme.Gray200
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrAttachmentSection(
    isExtracting: Boolean,
    feedbackMessage: String?,
    isFeedbackError: Boolean,
    attachmentFileName: String?,
    onFileSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { copyUriToCache(context, it)?.let(onFileSelected) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { copyUriToCache(context, it)?.let(onFileSelected) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraFile?.let { file ->
                if (file.exists()) onFileSelected(file.absolutePath)
            }
        }
        pendingCameraFile = null
    }

    Column(modifier = modifier) {
        androidx.compose.material3.OutlinedButton(
            onClick = {
                if (!isExtracting) showBottomSheet = true
            },
            enabled = !isExtracting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Indigo600)
        ) {
            if (isExtracting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Indigo600
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Se extrag datele din fi\u0219ier...",
                    color = Indigo600,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Indigo600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Extrage date din fi\u0219ier",
                    color = Indigo600,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pentru rezultate mai bune, \u00EEncarc\u0103 fotografia \u00EEn pozi\u021Bia corect\u0103, cu textul drept \u0219i clar.",
            color = Gray500,
            fontSize = 12.sp,
            fontWeight = FontWeight.W600,
            lineHeight = 16.sp
        )

        if (feedbackMessage != null && !isExtracting) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = feedbackMessage,
                color = if (isFeedbackError) Color(0xFFB91C1C) else Color(0xFF16A34A),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        if (attachmentFileName != null && !isExtracting) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = attachmentFileName,
                    color = Color(0xFF111827),
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
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
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )

                val items = listOf(
                    Triple(
                        "Document / Fi\u0219ier",
                        Icons.Default.Description,
                        {
                            filePickerLauncher.launch(
                                arrayOf("application/pdf", "image/jpeg", "image/png")
                            )
                            showBottomSheet = false
                        }
                    ),
                    Triple(
                        "Camer\u0103",
                        Icons.Default.PhotoCamera,
                        {
                            val file = File(context.cacheDir, "camera/OCR_${System.currentTimeMillis()}.jpg").apply {
                                parentFile?.mkdirs()
                            }
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            pendingCameraFile = file
                            cameraLauncher.launch(uri)
                            showBottomSheet = false
                        }
                    ),
                    Triple(
                        "Galerie",
                        Icons.Default.Collections,
                        {
                            galleryLauncher.launch("image/*")
                            showBottomSheet = false
                        }
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

private fun copyUriToCache(context: android.content.Context, uri: Uri): String? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val ext = when {
            mimeType.contains("pdf") -> ".pdf"
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
            mimeType.contains("png") -> ".png"
            else -> ".tmp"
        }
        val fileName = getFileName(context, uri) ?: "ocr_${System.currentTimeMillis()}$ext"
        val outFile = File(context.cacheDir, "ocr_uploads/$fileName").apply {
            parentFile?.mkdirs()
        }
        contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        outFile.absolutePath
    } catch (_: Exception) {
        null
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else null
            } else null
        }
    } catch (_: Exception) {
        null
    }
}
