package com.semanticsoft.patientmobile.ui.shared.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

data class UploadFileLaunchers(
    val onCameraClick: () -> Unit,
    val onGalleryClick: () -> Unit,
    val onFilePickerClick: () -> Unit
)

@Composable
fun rememberUploadFileLaunchers(viewModel: UploadFileViewModel): UploadFileLaunchers {
    val context = LocalContext.current
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraFile?.let { file ->
                if (file.exists()) {
                    viewModel.onFilesSelected(listOf(file.absolutePath))
                }
            }
        }
        pendingCameraFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val paths = uris.mapNotNull { uri -> copyUriToCache(context, uri) }
        if (paths.isNotEmpty()) {
            viewModel.onFilesSelected(paths)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val paths = uris.mapNotNull { uri -> copyUriToCache(context, uri) }
        if (paths.isNotEmpty()) {
            viewModel.onFilesSelected(paths)
        }
    }

    return UploadFileLaunchers(
        onCameraClick = {
            val file = File(context.cacheDir, "camera/IMG_${System.currentTimeMillis()}.jpg").apply {
                parentFile?.mkdirs()
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            pendingCameraFile = file
            cameraLauncher.launch(uri)
        },
        onGalleryClick = {
            galleryLauncher.launch("image/*")
        },
        onFilePickerClick = {
            filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
        }
    )
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
        val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}$ext"
        val outFile = File(context.cacheDir, "uploads/$fileName").apply {
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
