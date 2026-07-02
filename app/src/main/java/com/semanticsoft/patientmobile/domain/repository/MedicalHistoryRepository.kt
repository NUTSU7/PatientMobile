package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.data.remote.api.dto.MedicationOcrDraftResponseDto
import com.semanticsoft.patientmobile.data.remote.api.dto.PersonalNoteOcrDraftResponseDto
import com.semanticsoft.patientmobile.domain.model.Medication
import com.semanticsoft.patientmobile.domain.model.PersonalNote
import com.semanticsoft.patientmobile.util.ApiResult

interface MedicalHistoryRepository {
    suspend fun createMedication(medication: Medication): ApiResult<Medication>
    suspend fun getMedications(): ApiResult<List<Medication>>
    suspend fun updateMedication(id: String, medication: Medication): ApiResult<Medication>
    suspend fun deleteMedication(id: String): ApiResult<Unit>

    suspend fun createNote(note: PersonalNote): ApiResult<PersonalNote>
    suspend fun getNotes(): ApiResult<List<PersonalNote>>
    suspend fun updateNote(id: String, note: PersonalNote): ApiResult<PersonalNote>
    suspend fun deleteNote(id: String): ApiResult<Unit>

    suspend fun extractMedicationOcrDraft(filePath: String): ApiResult<MedicationOcrDraftResponseDto>
    suspend fun extractPersonalNoteOcrDraft(filePath: String): ApiResult<PersonalNoteOcrDraftResponseDto>
}
