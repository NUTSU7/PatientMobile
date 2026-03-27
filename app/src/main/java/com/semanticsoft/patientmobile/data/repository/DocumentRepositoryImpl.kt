package com.semanticsoft.patientmobile.data.repository

import android.content.Context
import com.semanticsoft.patientmobile.data.local.dao.DocumentDao
import com.semanticsoft.patientmobile.data.local.db.extensions.toDomain
import com.semanticsoft.patientmobile.data.local.db.extensions.toEntity
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.SyncStatus
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.Resource
import com.semanticsoft.patientmobile.util.toUserMessage
import com.semanticsoft.patientmobile.util.exceptions.FileTooLargeException
import com.semanticsoft.patientmobile.util.exceptions.FileUploadException
import com.semanticsoft.patientmobile.util.exceptions.UnsupportedMediaTypeException
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class DocumentRepositoryImpl(
    private val context: Context,
    private val apiService: PatientApiService,
    private val documentDao: DocumentDao,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : DocumentRepository {

    override suspend fun uploadDocument(file: File): PatientDocument {
        validateFile(file)

        if (!networkStateProvider.isOnline()) {
            val pending = PatientDocument(
                id = UUID.randomUUID().toString(),
                ownerUserId = "local-user",
                originalFileName = file.name,
                mimeType = resolveMimeType(file),
                fileSizeBytes = file.length(),
                uploadedAt = Instant.now(),
                localFilePath = file.absolutePath,
                syncStatus = SyncStatus.PENDING
            )
            documentDao.insert(pending.toEntity())
            return pending
        }

        return try {
            val requestBody = file.asRequestBody(resolveMimeType(file).toMediaType())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            val uploaded = apiService.uploadDocument(part)
            val cached = uploaded.copy(
                localFilePath = uploaded.localFilePath ?: file.absolutePath,
                syncStatus = SyncStatus.SYNCED
            )
            documentDao.insert(cached.toEntity())
            cached
        } catch (t: Throwable) {
            throw if (t is FileTooLargeException || t is UnsupportedMediaTypeException) {
                t
            } else {
                FileUploadException(t.toUserMessage(), t)
            }
        }
    }

    override fun getDocuments(): Flow<Resource<List<PatientDocument>>> = flow {
        emit(Resource.Loading)

        val cached = documentDao.getAllDocuments().map { it.toDomain() }
        if (!networkStateProvider.isOnline()) {
            emit(Resource.Success(cached))
            return@flow
        }

        runCatching {
            val remote = apiService.getDocuments().content
            documentDao.insertAll(remote.map { it.copy(syncStatus = SyncStatus.SYNCED).toEntity() })
            remote
        }.onSuccess {
            emit(Resource.Success(it))
        }.onFailure {
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Error(it.toUserMessage()))
            }
        }
    }

    override fun getDocumentById(id: String): Flow<Resource<PatientDocument>> = flow {
        emit(Resource.Loading)

        val local = documentDao.getDocumentById(id)?.toDomain()
        if (!networkStateProvider.isOnline()) {
            if (local != null) {
                emit(Resource.Success(local))
            } else {
                emit(Resource.Error("Document not available offline."))
            }
            return@flow
        }

        runCatching {
            val remote = apiService.getDocumentById(id)
            documentDao.insert(remote.copy(syncStatus = SyncStatus.SYNCED).toEntity())
            remote
        }.onSuccess {
            emit(Resource.Success(it))
        }.onFailure {
            if (local != null) {
                emit(Resource.Success(local))
            } else {
                emit(Resource.Error(it.toUserMessage()))
            }
        }
    }

    override suspend fun downloadDocument(id: String): File {
        val localDocument = documentDao.getDocumentById(id)?.toDomain()

        if (!networkStateProvider.isOnline()) {
            val localPath = localDocument?.localFilePath
            if (!localPath.isNullOrBlank()) {
                val localFile = File(localPath)
                if (localFile.exists()) return localFile
            }
            throw FileUploadException("Document is not available offline.")
        }

        val body = apiService.downloadDocument(id)
        val outFile = File(context.cacheDir, "document_$id.bin")
        outFile.outputStream().use { output ->
            body.byteStream().use { input -> input.copyTo(output) }
        }

        localDocument?.let {
            documentDao.update(it.copy(localFilePath = outFile.absolutePath).toEntity())
        }

        return outFile
    }

    private fun validateFile(file: File) {
        val sizeBytes = file.length()
        if (sizeBytes > MAX_UPLOAD_BYTES) {
            throw FileTooLargeException("File too large (max 10 MB).")
        }

        val extension = file.extension.lowercase()
        if (extension !in ALLOWED_EXTENSIONS) {
            throw UnsupportedMediaTypeException("Unsupported format (pdf, jpg, jpeg, png only).")
        }
    }

    private fun resolveMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }
    }

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("pdf", "jpg", "jpeg", "png")
        private const val MAX_UPLOAD_BYTES = 10L * 1024L * 1024L
    }
}
