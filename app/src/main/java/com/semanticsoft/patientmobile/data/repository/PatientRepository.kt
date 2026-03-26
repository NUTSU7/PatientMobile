package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.data.model.BasicIndicatorItem
import com.semanticsoft.patientmobile.data.model.IndicatorSegments
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.data.model.IndicatorTrendDirection
import com.semanticsoft.patientmobile.data.model.MarkerSummary

interface PatientRepository {
    fun getDashboardGreetingName(): String
    fun getAttentionItems(): List<AttentionItem>
    fun getBasicIndicators(): List<BasicIndicatorItem>
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

    override fun getBasicIndicators(): List<BasicIndicatorItem> = listOf(
        BasicIndicatorItem(
            title = "Hemoglobina",
            value = "14.2",
            unit = "g/dL",
            status = IndicatorStatus.NORMAL,
            trendDirection = IndicatorTrendDirection.STABLE,
            trendDelta = "",
            trendDescription = "Stabil față de ultima analiză",
            markerPosition = 0.34f,
            segments = IndicatorSegments(good = 0.60f, borderline = 0.20f, risk = 0.20f)
        ),
        BasicIndicatorItem(
            title = "Glucoza a jeun",
            value = "6.8",
            unit = "mmol/L",
            status = IndicatorStatus.BORDERLINE,
            trendDirection = IndicatorTrendDirection.UP,
            trendDelta = "0.6",
            trendDescription = "față de ultima analiză",
            markerPosition = 0.66f,
            segments = IndicatorSegments(good = 0.60f, borderline = 0.20f, risk = 0.20f)
        ),
        BasicIndicatorItem(
            title = "ALT",
            value = "55",
            unit = "U/L",
            status = IndicatorStatus.ATTENTION,
            trendDirection = IndicatorTrendDirection.DOWN,
            trendDelta = "0.9",
            trendDescription = "față de ultima analiză",
            markerPosition = 0.88f,
            segments = IndicatorSegments(good = 0.60f, borderline = 0.20f, risk = 0.20f)
        )
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
