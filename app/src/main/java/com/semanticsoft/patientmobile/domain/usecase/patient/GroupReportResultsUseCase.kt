package com.semanticsoft.patientmobile.domain.usecase.patient

import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.domain.model.patient.ResultGroup
import com.semanticsoft.patientmobile.domain.model.patient.ResultItem
import javax.inject.Inject

class GroupReportResultsUseCase @Inject constructor() {

    operator fun invoke(results: List<MedicalResult>): List<ResultGroup> {
        return results
            .groupBy { it.analysisGroup.ifEmpty { "Altele" } }
            .map { (groupName, items) ->
                ResultGroup(
                    groupName = groupName,
                    results = items
                        .sortedBy { it.originalTestName }
                        .map { it.toResultItem() }
                )
            }
    }

    private fun MedicalResult.toResultItem(): ResultItem {
        val valueStr = when {
            valueNumeric != null -> valueNumeric.toString()
            !valueText.isNullOrBlank() -> valueText
            else -> "-"
        }
        val referenceStr = buildString {
            when {
                referenceLow != null && referenceHigh != null -> {
                    append(referenceLow.toString())
                    append(" – ")
                    append(referenceHigh.toString())
                    if (unit.isNotBlank()) {
                        append(" ")
                        append(unit)
                    }
                }
                !referenceText.isNullOrBlank() -> append(referenceText)
                else -> append("-")
            }
        }
        return ResultItem(
            id = id,
            testName = originalTestName,
            value = valueStr,
            unit = unit,
            reference = referenceStr,
            abnormalFlag = abnormalFlag
        )
    }
}
