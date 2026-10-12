package com.semanticsoft.patientmobile.data.repository.patient

import android.content.Context
import com.semanticsoft.patientmobile.core.network.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.core.network.api.ApiConstants
import com.semanticsoft.patientmobile.core.network.api.PatientApiService
import com.semanticsoft.patientmobile.core.network.dto.patient.BulkDeleteRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.CreateShareLinkRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.DuplicateCheckRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.RenameDocumentRequest
import com.semanticsoft.patientmobile.data.mapper.common.parseInstantSafe
import com.semanticsoft.patientmobile.data.mapper.patient.toDomain
import com.semanticsoft.patientmobile.core.network.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.core.network.NetworkStateProvider
import com.semanticsoft.patientmobile.domain.model.patient.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.patient.DocumentStats
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import com.semanticsoft.patientmobile.domain.model.patient.SharedLink
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.core.util.map
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
        dateTo: String?,
        sortBy: String?,
        sortDir: String?
    ): ApiResult<List<PatientDocument>> = requireOnline {
        safeApiCall { apiService.getDocuments(page, size, search, dateFrom, dateTo, sortBy, sortDir) }
            .map { response -> response.content.map { it.toDomain() } }
    }

    override suspend fun getDocumentById(id: String): ApiResult<PatientDocument> = requireOnline {
        safeApiCall { apiService.getDocumentById(id) }.map { it.toDomain() }
    }

    override suspend fun downloadDocument(id: String): ApiResult<File> {
        val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            ?: context.cacheDir
        return downloadTo(id, downloadsDir)
    }

    override suspend fun downloadDocumentFile(documentId: String): ApiResult<File> =
        downloadTo(documentId, context.cacheDir)

    override suspend fun getDocumentExplanation(documentId: String): ApiResult<String> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        val extractionsResult = safeApiCall { apiService.listExtractions(documentId) }
        if (extractionsResult !is ApiResult.Success) return ApiResult.Success("")

        val successExtraction = extractionsResult.data
            .firstOrNull { it.status.equals("SUCCESS", ignoreCase = true) }
            ?: return ApiResult.Success("")

        val detailResult = safeApiCall {
            apiService.getExtractionStatus(documentId, successExtraction.id)
        }
        if (detailResult !is ApiResult.Success) return ApiResult.Success("")

        val reportId = detailResult.data.reports.firstOrNull()?.id
            ?: return ApiResult.Success("")

        return safeApiCall { apiService.getAiExplanation(reportId) }
            .map { it.explanation }
    }

    override suspend fun renameDocument(id: String, newName: String): ApiResult<PatientDocument> =
        requireOnline {
            safeApiCall {
                apiService.renameDocument(id, RenameDocumentRequest(newName))
            }.map { it.toDomain() }
        }

    override suspend fun deleteDocument(id: String): ApiResult<Unit> = requireOnline {
        safeApiCall { apiService.deleteDocument(id) }.map { }
    }

    override suspend fun bulkDelete(documentIds: List<String>): ApiResult<Unit> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        if (documentIds.isEmpty()) return ApiResult.Success(Unit)
        return safeApiCall {
            apiService.bulkDeleteDocuments(BulkDeleteRequest(documentIds))
        }.map { }
    }

    override suspend fun createShareLink(id: String): ApiResult<SharedLink> = requireOnline {
        safeApiCall {
            apiService.shareDocument(id, CreateShareLinkRequest())
        }.map { it.toDomain() }
    }

    override suspend fun getDocumentStats(): ApiResult<DocumentStats> = requireOnline {
        safeApiCall {
            apiService.getDocuments(
                page = 0,
                size = 1,
                sortBy = "uploadedAt",
                sortDir = "desc"
            )
        }.map { response ->
            DocumentStats(
                totalCount = response.totalElements.toInt(),
                lastUploadedAt = response.content.firstOrNull()?.uploadedAt?.let(::parseInstantSafe)
            )
        }
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

    private suspend fun <T> requireOnline(block: suspend () -> ApiResult<T>): ApiResult<T> =
        if (networkStateProvider.isOnline()) block() else ApiResult.NetworkError

    private suspend fun downloadTo(documentId: String, directory: File): ApiResult<File> =
        requireOnline {
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.downloadDocument(documentId)
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

                    val filename = extractFilename(response, documentId)
                    val outFile = File(directory, filename)

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
