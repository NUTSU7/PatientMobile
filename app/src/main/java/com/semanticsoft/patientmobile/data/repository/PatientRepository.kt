package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.data.model.MarkerSummary

interface PatientRepository {
    fun getDashboardGreetingName(): String
    fun getAttentionItems(): List<AttentionItem>
    fun getMarkerSummary(): MarkerSummary
    fun getAiSummary(): String
}

class MockPatientRepository : PatientRepository {
    override fun getDashboardGreetingName(): String = "Alexandru"

    override fun getAttentionItems(): List<AttentionItem> = listOf(
        AttentionItem("TSH", "0.3", "mIU/L", "Atenție"),
        AttentionItem("Vitamina D", "18", "ng/mL", "Atenție"),
        AttentionItem("Colesterol LDL", "4.5", "mmol/L", "Atenție"),
        AttentionItem("Glucoza a jeun", "6.8", "mmol/L", "Atenție moderată")
    )

    override fun getMarkerSummary(): MarkerSummary = MarkerSummary(
        normal = 13,
        borderline = 3,
        attention = 4,
        score = 72
    )

    override fun getAiSummary(): String =
        "Rezultatele tale sunt în mare parte stabile. LDL este peste limita sigură, " +
            "iar vitamina D rămâne scăzută. Restul markerilor majori sunt în interval normal."
}
