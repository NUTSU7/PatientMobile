package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement

import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod

/**
 * Temporary prototype data for the laborator digital engagement screen.
 *
 * Replace with a repository-backed data source once laborator analytics endpoints exist.
 */
internal const val COHORT_FOOTNOTE =
    "Cohortele recente au linii întrerupte când perioada de observație nu este completă."

internal const val PRIVACY_FOOTNOTE =
    "Datele sunt agregate pe cohorte și nu expun informații de identificare personală. " +
        "Pragul minim de confidențialitate este aplicat automat."

internal fun buildEngagementMockState(
    period: LaboratorPeriod
): DigitalEngagementUiState {
    return DigitalEngagementUiState(
        selectedPeriod = period,
        chartSubtitle = "Revenirea analizelor · ${period.filterLabel}",
        xLabels = xLabelsFor(period),
        cohortLines = cohortLinesFor(period),
        cohortSnapshot = cohortSnapshotFor(period),
        cohortFootnote = COHORT_FOOTNOTE,
        locationFootnote = "Calculat pe ultimele ${period.filterLabel} calendaristice.",
        locations = locationsFor(period),
        repeatedAnalyses = repeatedAnalysesFor(period),
        expandedAnalysisId = INITIAL_EXPANDED_ANALYSIS_ID
    )
}

private fun xLabelsFor(period: LaboratorPeriod): List<String> {
    return when (period) {
        LaboratorPeriod.SEVEN_DAYS -> listOf("24 oct", "25 oct", "26 oct", "27 oct", "28 oct")
        LaboratorPeriod.THREE_MONTHS -> listOf("Iul", "Aug", "Sep", "Oct")
        LaboratorPeriod.THIRTY_DAYS, LaboratorPeriod.CUSTOM ->
            listOf("1 Sep", "8 Sep", "15 Sep", "22 Sep", "29 Sep")
    }
}

private fun cohortLinesFor(period: LaboratorPeriod): List<EngagementCohortLine> {
    return when (period) {
        LaboratorPeriod.SEVEN_DAYS -> listOf(
            EngagementCohortLine(
                values = listOf(0.08f, 0.14f, 0.12f, 0.22f, 0.24f),
                observed = 4
            ),
            EngagementCohortLine(
                values = listOf(0.12f, 0.18f, 0.20f, 0.19f, 0.17f),
                observed = 5
            ),
            EngagementCohortLine(
                values = listOf(0.02f, 0.04f, 0.03f, 0.05f, 0.02f),
                observed = 5
            )
        )

        LaboratorPeriod.THREE_MONTHS -> listOf(
            EngagementCohortLine(
                values = listOf(0.18f, 0.26f, 0.22f, 0.38f),
                observed = 3
            ),
            EngagementCohortLine(
                values = listOf(0.10f, 0.15f, 0.19f, 0.21f),
                observed = 4
            ),
            EngagementCohortLine(
                values = listOf(0.01f, 0.03f, 0.04f, 0.02f),
                observed = 4
            )
        )

        LaboratorPeriod.THIRTY_DAYS, LaboratorPeriod.CUSTOM -> listOf(
            EngagementCohortLine(
                values = listOf(0.08f, 0.16f, 0.32f, 0.36f, 0.42f),
                observed = 3
            ),
            EngagementCohortLine(
                values = listOf(0.10f, 0.13f, 0.12f, 0.18f, 0.16f),
                observed = 5
            ),
            EngagementCohortLine(
                values = listOf(0.02f, 0.03f, 0.02f, 0.03f, 0.02f),
                observed = 5
            )
        )
    }
}

private fun cohortSnapshotFor(period: LaboratorPeriod): EngagementCohortSnapshot? {
    return when (period) {
        LaboratorPeriod.SEVEN_DAYS -> EngagementCohortSnapshot(
            cohortLabel = "Cohorta 22–28 octombrie",
            stats = listOf(
                EngagementCohortStat(
                    label = "<30 zile",
                    valueText = "24%",
                    changeText = "+2 pp"
                ),
                EngagementCohortStat(label = "30–90 zile", valueText = "17%", changeText = ""),
                EngagementCohortStat(label = "91–180 zile", valueText = "2%", changeText = "")
            )
        )

        LaboratorPeriod.THREE_MONTHS -> EngagementCohortSnapshot(
            cohortLabel = "Cohorta august",
            stats = listOf(
                EngagementCohortStat(
                    label = "<30 zile",
                    valueText = "38%",
                    changeText = "+6 pp"
                ),
                EngagementCohortStat(label = "30–90 zile", valueText = "21%", changeText = ""),
                EngagementCohortStat(label = "91–180 zile", valueText = "2%", changeText = "")
            )
        )

        LaboratorPeriod.THIRTY_DAYS, LaboratorPeriod.CUSTOM -> EngagementCohortSnapshot(
            cohortLabel = "Cohorta 15–21 septembrie",
            stats = listOf(
                EngagementCohortStat(
                    label = "<30 zile",
                    valueText = "37%",
                    changeText = "+3 pp"
                ),
                EngagementCohortStat(label = "30–90 zile", valueText = "16%", changeText = ""),
                EngagementCohortStat(label = "91–180 zile", valueText = "2%", changeText = "")
            )
        )
    }
}

private fun locationsFor(period: LaboratorPeriod): List<EngagementLocation> {
    return when (period) {
        LaboratorPeriod.SEVEN_DAYS -> listOf(
            EngagementLocation(EngagementLocationFilter.BUIUCANI, 71, "2 zile", 1),
            EngagementLocation(EngagementLocationFilter.CENTRU, 67, "1 zi 4 ore", 2),
            EngagementLocation(EngagementLocationFilter.RISCANI, 62, "1 zi 12 ore", 0),
            EngagementLocation(EngagementLocationFilter.BOTANICA, 55, "2 zile 3 ore", -1)
        )

        LaboratorPeriod.THREE_MONTHS -> listOf(
            EngagementLocation(EngagementLocationFilter.BUIUCANI, 76, "2 zile", 4),
            EngagementLocation(EngagementLocationFilter.CENTRU, 71, "1 zi 4 ore", 3),
            EngagementLocation(EngagementLocationFilter.RISCANI, 65, "1 zi 12 ore", 0),
            EngagementLocation(EngagementLocationFilter.BOTANICA, 58, "2 zile 3 ore", -2)
        )

        LaboratorPeriod.THIRTY_DAYS, LaboratorPeriod.CUSTOM -> listOf(
            EngagementLocation(EngagementLocationFilter.BUIUCANI, 73, "18 ore", 4),
            EngagementLocation(EngagementLocationFilter.CENTRU, 68, "1 zi 4 ore", 2),
            EngagementLocation(EngagementLocationFilter.RISCANI, 61, "1 zi 12 ore", 0),
            EngagementLocation(EngagementLocationFilter.BOTANICA, 54, "2 zile 3 ore", -1)
        )
    }
}

private fun repeatedAnalysesFor(period: LaboratorPeriod): List<EngagementRepeatedAnalysis> {
    return when (period) {
        LaboratorPeriod.SEVEN_DAYS -> listOf(
            EngagementRepeatedAnalysis(
                id = "tsh",
                name = "TSH (Hormon de stimulare tiroidiană)",
                method = "Imunochimie ECLIA",
                repeatPercent = 44,
                medianInterval = "44 zile",
                distribution = "30–90 zile",
                previousChangePp = 6
            ),
            EngagementRepeatedAnalysis(
                id = "vitamina-d",
                name = "25-OH Vitamina D",
                method = "Electrochemiluminescență",
                repeatPercent = 38,
                medianInterval = "82 zile",
                distribution = "90–180 zile",
                previousChangePp = 1
            ),
            EngagementRepeatedAnalysis(
                id = "hemoleucograma",
                name = "Hemoleucogramă completă cu formulă leucocitară",
                method = "Hematologie automată 5-diff",
                repeatPercent = 27,
                medianInterval = "31 zile",
                distribution = "30–90 zile",
                previousChangePp = 3
            ),
            EngagementRepeatedAnalysis(
                id = "glicemie",
                name = "Glicemie serică",
                method = "Biochimie clinică",
                repeatPercent = 19,
                medianInterval = "28 zile",
                distribution = "<30 zile",
                previousChangePp = 2
            )
        )

        LaboratorPeriod.THREE_MONTHS -> listOf(
            EngagementRepeatedAnalysis(
                id = "tsh",
                name = "TSH (Hormon de stimulare tiroidiană)",
                method = "Imunochimie ECLIA",
                repeatPercent = 40,
                medianInterval = "52 zile",
                distribution = "30–90 zile",
                previousChangePp = 8
            ),
            EngagementRepeatedAnalysis(
                id = "vitamina-d",
                name = "25-OH Vitamina D",
                method = "Electrochemiluminescență",
                repeatPercent = 33,
                medianInterval = "88 zile",
                distribution = "90–180 zile",
                previousChangePp = -2
            ),
            EngagementRepeatedAnalysis(
                id = "hemoleucograma",
                name = "Hemoleucogramă completă cu formulă leucocitară",
                method = "Hematologie automată 5-diff",
                repeatPercent = 31,
                medianInterval = "34 zile",
                distribution = "30–90 zile",
                previousChangePp = 2
            ),
            EngagementRepeatedAnalysis(
                id = "profil-lipidic",
                name = "Profil lipidic (Colesterol total, HDL, LDL, Trigliceride)",
                method = "Spectrofotometrie enzimatică",
                repeatPercent = 26,
                medianInterval = "66 zile",
                distribution = "90–180 zile",
                previousChangePp = 0
            ),
            EngagementRepeatedAnalysis(
                id = "glicemie",
                name = "Glicemie serică",
                method = "Biochimie clinică",
                repeatPercent = 20,
                medianInterval = "30 zile",
                distribution = "<30 zile",
                previousChangePp = 4
            )
        )

        LaboratorPeriod.THIRTY_DAYS, LaboratorPeriod.CUSTOM -> listOf(
            EngagementRepeatedAnalysis(
                id = "tsh",
                name = "TSH (Hormon de stimulare tiroidiană)",
                method = "Imunochimie ECLIA",
                repeatPercent = 42,
                medianInterval = "47 zile",
                distribution = "30–90 zile",
                previousChangePp = 5
            ),
            EngagementRepeatedAnalysis(
                id = "vitamina-d",
                name = "25-OH Vitamina D",
                method = "Electrochemiluminescență",
                repeatPercent = 36,
                medianInterval = "82 zile",
                distribution = "90–180 zile",
                previousChangePp = -2
            ),
            EngagementRepeatedAnalysis(
                id = "hemoleucograma",
                name = "Hemoleucogramă completă cu formulă leucocitară",
                method = "Hematologie automată 5-diff",
                repeatPercent = 29,
                medianInterval = "31 zile",
                distribution = "30–90 zile",
                previousChangePp = 1
            ),
            EngagementRepeatedAnalysis(
                id = "profil-lipidic",
                name = "Profil lipidic (Colesterol total, HDL, LDL, Trigliceride)",
                method = "Spectrofotometrie enzimatică",
                repeatPercent = 24,
                medianInterval = "66 zile",
                distribution = "90–180 zile",
                previousChangePp = 0
            ),
            EngagementRepeatedAnalysis(
                id = "glicemie",
                name = "Glicemie serică",
                method = "Biochimie clinică",
                repeatPercent = 18,
                medianInterval = "28 zile",
                distribution = "<30 zile",
                previousChangePp = 3
            )
        )
    }
}
