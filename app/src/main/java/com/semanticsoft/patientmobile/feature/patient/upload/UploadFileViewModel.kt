package com.semanticsoft.patientmobile.feature.patient.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UploadFileViewModel : ViewModel() {

    private val _state = MutableStateFlow(UploadFileUiState())
    val state: StateFlow<UploadFileUiState> = _state.asStateFlow()

    private val _processingStatus = MutableStateFlow(ProcessingStatus())
    val processingStatus: StateFlow<ProcessingStatus> = _processingStatus.asStateFlow()

    private val _events = MutableSharedFlow<UploadFileEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    fun resumeProcessingIfPending() {
        // Default no-op
    }

    fun onFilesSelected(uris: List<String>) {
        val oversizeErrors = mutableListOf<ErrorFileEntry>()
        val validFiles = uris.mapNotNull { uri ->
            val file = resolveFile(uri) ?: return@mapNotNull null
            if (!file.exists()) return@mapNotNull null
            val extension = file.extension.lowercase()
            if (extension !in ALLOWED_EXTENSIONS) return@mapNotNull null
            if (file.length() > MAX_UPLOAD_BYTES) {
                oversizeErrors.add(ErrorFileEntry(file.name, "Fișier prea mare (max 10 MB)."))
                return@mapNotNull null
            }
            val normalized = if (ImageNormalizer.shouldNormalize(file)) {
                ImageNormalizer.normalize(file)
            } else null
            val uploadFile = normalized ?: file
            val displayName = if (normalized != null) {
                val dot = file.name.lastIndexOf('.')
                val base = if (dot > 0) file.name.substring(0, dot) else file.name
                "${base}-normalizata.jpg"
            } else file.name
            val displaySize = uploadFile.length()
            SelectedFile(
                name = displayName,
                uri = uploadFile.absolutePath.let { "file://$it" },
                sizeBytes = displaySize,
                mimeType = if (normalized != null) "image/jpeg" else resolveMimeType(file)
            )
        }

        if (validFiles.isEmpty()) {
            val current = _state.value.selectedFiles
            if (current.all { it.status != UploadStatus.ERROR }) return
            val updated = current.map { file ->
                if (file.status == UploadStatus.PENDING || file.status == UploadStatus.UPLOADING) {
                    file.copy(errorMessage = "Niciun fișier valid selectat.")
                } else file
            }
            _state.update { it.copy(selectedFiles = updated) }
            return
        }

        _state.update {
            it.copy(
                selectedFiles = it.selectedFiles + validFiles,
                uploadComplete = false,
                isSystemicError = false,
                errorFiles = oversizeErrors
            )
        }
    }

    fun startUpload() {
        if (_state.value.isUploading) return
        if (_state.value.selectedFiles.none {
                it.status == UploadStatus.PENDING || it.status == UploadStatus.ERROR
            }) return

        viewModelScope.launch {
            _state.update { current ->
                val updated = current.selectedFiles.map {
                    if (it.status == UploadStatus.PENDING || it.status == UploadStatus.PENDING_FORCE) {
                        it.copy(status = UploadStatus.SUCCESS)
                    } else it
                }
                current.copy(selectedFiles = updated, isUploading = false, uploadComplete = true)
            }
            _events.emit(UploadFileEvent.AllFilesUploaded)
        }
    }

    fun retryFile(index: Int) {
        val files = _state.value.selectedFiles.toMutableList()
        if (index !in files.indices) return
        files[index] = files[index].copy(status = UploadStatus.PENDING, errorMessage = null)
        _state.update { it.copy(selectedFiles = files) }
    }

    fun removeFile(index: Int) {
        if (_state.value.isUploading) return
        val updated = _state.value.selectedFiles.toMutableList().apply { removeAt(index) }
        _state.update { it.copy(selectedFiles = updated) }
    }

    fun resetUploadComplete() {
        _state.update { it.copy(uploadComplete = false) }
    }

    fun reset() {
        _state.update { UploadFileUiState() }
    }

    private fun resolveFile(uri: String): File? {
        val path = when {
            uri.startsWith("file://") -> uri.removePrefix("file://")
            uri.startsWith("/") -> uri
            else -> return null
        }
        return File(path)
    }

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("pdf", "jpg", "jpeg", "png")
        private const val MAX_UPLOAD_BYTES = 10L * 1024L * 1024L

        private fun resolveMimeType(file: File): String = when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }
    }
}
