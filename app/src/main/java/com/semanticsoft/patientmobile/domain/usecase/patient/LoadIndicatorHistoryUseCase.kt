package com.semanticsoft.patientmobile.domain.usecase.patient

import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalResultRepository
import javax.inject.Inject

class LoadIndicatorHistoryUseCase @Inject constructor(
    private val medicalResultRepository: MedicalResultRepository
) {
    suspend operator fun invoke(): List<MedicalResult> {
        val all = mutableListOf<MedicalResult>()
        var page = 0
        do {
            val result = medicalResultRepository.getAllResultsPaginated(
                page = page,
                size = PAGE_SIZE,
                sortBy = "observedAt",
                sortDir = "desc"
            )
            if (result !is ApiResult.Success) break
            val pageData = result.data
            all.addAll(pageData.items)
            page++
            if (!pageData.hasNext) break
        } while (page < MAX_PAGES)
        return all
    }

    private companion object {
        const val PAGE_SIZE = 100
        const val MAX_PAGES = 10
    }
}
