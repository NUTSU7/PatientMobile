package com.semanticsoft.patientmobile.domain.repository.laborator

import com.semanticsoft.patientmobile.domain.model.laborator.LabResult
import com.semanticsoft.patientmobile.domain.model.laborator.LabSubmission
import com.semanticsoft.patientmobile.util.ApiResult

interface LaboratorRepository {
    suspend fun getSubmissions(): ApiResult<List<LabSubmission>>
    suspend fun getResults(submissionId: String): ApiResult<List<LabResult>>
    suspend fun submitResult(result: LabResult): ApiResult<LabResult>
}
