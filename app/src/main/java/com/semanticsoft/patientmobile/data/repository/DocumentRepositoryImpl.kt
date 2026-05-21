package com.semanticsoft.patientmobile.data.repository

import android.content.Context
import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.ApiConstants
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.BulkDeleteRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateShareLinkRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.DuplicateCheckRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RenameDocumentRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.toDomain
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.SharedLink
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class DocumentRepositoryImpl(
    private val context: Context,
    private val apiService: PatientApiService,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : DocumentRepository {

    override suspend fun checkDuplicates(
        checksums: List<String>
    ): ApiResult<List<DocumentDuplicateInfo>> {
        if (checksums.isEmpty()) return ApiResult.Success(emptyList())
        if (!networkStateProvider.isOnline()) return ApiResult.Success(emptyList())

        return safeApiCall {
            apiService.checkDuplicates(DuplicateCheckRequest(checksums))
        }.map { response -> response.matches.map { it.toDomain() } }
    }

    override suspend fun uploadDocument(
        file: File,
        force: Boolean
    ): ApiResult<PatientDocument> = withContext(Dispatchers.IO) {
        if (!networkStateProvider.isOnline()) return@withContext ApiResult.NetworkError

        val validationError = validateFile(file)
        if (validationError != null) return@withContext validationError

        val mimeType = resolveMimeType(file)
        val requestBody = file.asRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val forceBody = if (force) "true".toRequestBody("text/plain".toMediaType()) else null

        safeApiCall {
            apiService.uploadDocument(part, forceBody)
        }.map { it.toDomain() }
    }

    override suspend fun getDocuments(
        page: Int,
        size: Int,
        search: String?,
        dateFrom: String?,
        dateTo: String?
    ): ApiResult<List<PatientDocument>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getDocuments(page, size, search, dateFrom, dateTo) }.map { response ->
            response.content.map { it.toDomain() }
        }
    }

    override suspend fun getDocumentById(id: String): ApiResult<PatientDocument> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getDocumentById(id) }.map { it.toDomain() }
    }

    override suspend fun downloadDocument(id: String): ApiResult<File> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.downloadDocument(id)
                if (!response.isSuccessful) {
                    return@withContext ApiResult.HttpError(
                        code = response.code(),
                        message = response.message()
                    )
                }

                val body = response.body() ?: return@withContext ApiResult.HttpError(
                    code = -1,
                    message = "Empty response body"
                )

                val filename = extractFilename(response, id)
                val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                    ?: context.cacheDir
                val outFile = java.io.File(downloadsDir, filename)

                outFile.outputStream().use { output ->
                    body.byteStream().use { input -> input.copyTo(output) }
                }

                body.close()
                ApiResult.Success(outFile)
            } catch (e: Exception) {
                ApiResult.HttpError(code = -1, message = e.message ?: "Download failed")
            }
        }
    }

    override suspend fun renameDocument(id: String, newName: String): ApiResult<PatientDocument> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall {
            apiService.renameDocument(id, RenameDocumentRequest(newName))
        }.map { it.toDomain() }
    }

    override suspend fun deleteDocument(id: String): ApiResult<Unit> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.deleteDocument(id) }.map { }
    }

    override suspend fun bulkDelete(documentIds: List<String>): ApiResult<Unit> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        if (documentIds.isEmpty()) return ApiResult.Success(Unit)
        return safeApiCall {
            apiService.bulkDeleteDocuments(BulkDeleteRequest(documentIds))
        }.map { }
    }

    override suspend fun createShareLink(id: String): ApiResult<SharedLink> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall {
            apiService.shareDocument(id, CreateShareLinkRequest())
        }.map { it.toDomain() }
    }

    suspend fun computeSha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun validateFile(file: File): ApiResult<Nothing>? {
        if (file.length() > ApiConstants.MAX_UPLOAD_BYTES) {
            return ApiResult.HttpError(413, "File too large (max 10 MB).")
        }
        val extension = file.extension.lowercase()
        if (extension !in ApiConstants.ALLOWED_UPLOAD_EXTENSIONS) {
            return ApiResult.HttpError(415, "Unsupported format (pdf, jpg, jpeg, png only).")
        }
        return null
    }

    private fun resolveMimeType(file: File): String = when (file.extension.lowercase()) {
        "pdf" -> "application/pdf"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        else -> "application/octet-stream"
    }

    private fun extractFilename(response: retrofit2.Response<*>, fallbackId: String): String {
        val contentDisposition = response.headers()["Content-Disposition"]
            ?: response.headers()["content-disposition"]
        if (!contentDisposition.isNullOrBlank()) {
            val match = Regex("""filename\*=UTF-8''(.+?)(?:;|$)""").find(contentDisposition)
                ?: Regex("""filename="(.+?)"""").find(contentDisposition)
                ?: Regex("""filename=(.+?)(?:;|$)""").find(contentDisposition)
            match?.groupValues?.get(1)?.trim()?.let { return it }
        }
        val ext = response.headers()["Content-Type"]
            ?.substringAfter("/")
            ?.takeIf { it.isNotBlank() }
            ?: "bin"
        return "document_$fallbackId.$ext"
    }
}