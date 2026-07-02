package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateMedicationRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateMedicationScheduleRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateNoteRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.MedicationOcrDraftResponseDto
import com.semanticsoft.patientmobile.data.remote.api.dto.PersonalNoteOcrDraftResponseDto
import com.semanticsoft.patientmobile.data.remote.api.dto.UpdateMedicationRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.UpdateNoteRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.toDomain
import com.semanticsoft.patientmobile.domain.model.Medication
import com.semanticsoft.patientmobile.domain.model.PersonalNote
import com.semanticsoft.patientmobile.domain.repository.MedicalHistoryRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class MedicalHistoryRepositoryImpl(
    private val apiService: PatientApiService,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : MedicalHistoryRepository {

    override suspend fun createMedication(medication: Medication): ApiResult<Medication> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        val request = CreateMedicationRequest(
            name = medication.name,
            doseValue = medication.doseValue,
            doseUnit = medication.doseUnit.name,
            schedules = medication.schedules.map {
                CreateMedicationScheduleRequest(it.administrationTime, it.mealRelation.name)
            },
            active = medication.active,
            effectiveDate = medication.effectiveDate?.toString(),
            endDate = medication.endDate?.toString(),
            analysisDocumentId = medication.analysisDocumentId,
            attachmentIds = medication.attachmentIds.ifEmpty { null },
            ocrReviewConfirmed = medication.ocrReviewConfirmed.takeIf { medication.analysisDocumentId != null }
        )

        return safeApiCall { apiService.createMedication(request) }.map { it.toDomain() }
    }

    override suspend fun getMedications(): ApiResult<List<Medication>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getMedications() }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateMedication(id: String, medication: Medication): ApiResult<Medication> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        val request = UpdateMedicationRequest(
            name = medication.name,
            doseValue = medication.doseValue,
            doseUnit = medication.doseUnit.name,
            schedules = medication.schedules.map {
                CreateMedicationScheduleRequest(it.administrationTime, it.mealRelation.name)
            },
            active = medication.active,
            effectiveDate = medication.effectiveDate?.toString(),
            endDate = medication.endDate?.toString(),
            analysisDocumentId = medication.analysisDocumentId,
            attachmentIds = medication.attachmentIds.ifEmpty { null },
            ocrReviewConfirmed = medication.ocrReviewConfirmed.takeIf { medication.analysisDocumentId != null }
        )

        return safeApiCall { apiService.updateMedication(id, request) }.map { it.toDomain() }
    }

    override suspend fun deleteMedication(id: String): ApiResult<Unit> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.deleteMedication(id) }.map { }
    }

    override suspend fun createNote(note: PersonalNote): ApiResult<PersonalNote> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        val request = CreateNoteRequest(
            analysisName = note.analysisName,
            doctorLocation = note.doctorLocation,
            clinicalObservations = note.clinicalObservations,
            noteDate = note.noteDate.toString(),
            analysisDocumentId = note.analysisDocumentId,
            attachmentIds = note.attachmentIds.ifEmpty { null }
        )

        return safeApiCall { apiService.createNote(request) }.map { it.toDomain() }
    }

    override suspend fun getNotes(): ApiResult<List<PersonalNote>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getNotes() }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateNote(id: String, note: PersonalNote): ApiResult<PersonalNote> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        val request = UpdateNoteRequest(
            analysisName = note.analysisName,
            doctorLocation = note.doctorLocation,
            clinicalObservations = note.clinicalObservations,
            noteDate = note.noteDate.toString(),
            analysisDocumentId = note.analysisDocumentId,
            attachmentIds = note.attachmentIds.ifEmpty { null }
        )

        return safeApiCall { apiService.updateNote(id, request) }.map { it.toDomain() }
    }

    override suspend fun deleteNote(id: String): ApiResult<Unit> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.deleteNote(id) }.map { }
    }

    override suspend fun extractMedicationOcrDraft(filePath: String): ApiResult<MedicationOcrDraftResponseDto> =
        withContext(Dispatchers.IO) {
            if (!networkStateProvider.isOnline()) return@withContext ApiResult.NetworkError

            val file = File(filePath)
            val mimeType = resolveMimeType(file)
            val requestBody = file.asRequestBody(mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            safeApiCall { apiService.extractMedicationOcrDraft(part) }
        }

    override suspend fun extractPersonalNoteOcrDraft(filePath: String): ApiResult<PersonalNoteOcrDraftResponseDto> =
        withContext(Dispatchers.IO) {
            if (!networkStateProvider.isOnline()) return@withContext ApiResult.NetworkError

            val file = File(filePath)
            val mimeType = resolveMimeType(file)
            val requestBody = file.asRequestBody(mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            safeApiCall { apiService.extractPersonalNoteOcrDraft(part) }
        }

    private fun resolveMimeType(file: File): String = when (file.extension.lowercase()) {
        "pdf" -> "application/pdf"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        else -> "application/octet-stream"
    }
}