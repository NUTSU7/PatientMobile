package com.semanticsoft.patientmobile.feature.laborator.dashboard

/**
 * Temporary prototype data for the laborator dashboard.
 *
 * Replace with a repository-backed data source once laborator endpoints exist.
 */
internal fun buildLaboratorDashboardMockState(
    period: LaboratorPeriod,
    compareEnabled: Boolean,
    lastSyncLabel: String
): LaboratorDashboardUiState {
    return LaboratorDashboardUiState(
        lastSyncLabel = lastSyncLabel,
        selectedPeriod = period,
        compareEnabled = compareEnabled,
        metrics = metricsFor(period),
        activity = activityFor(period),
        trend = TREND,
        topAnalyses = TOP_ANALYSES
    )
}

private fun metricsFor(period: LaboratorPeriod): List<LaboratorMetric> {
    return when (period) {
        LaboratorPeriod.SEVEN_DAYS -> listOf(
            LaboratorMetric(
                id = "processed",
                label = "Rezultate procesate",
                value = "312",
                changeText = "8% față de perioada anterioară"
            ),
            LaboratorMetric(
                id = "accessed",
                label = "Rezultate accesate",
                value = "2.104",
                changeText = "3.2% față de perioada anterioară"
            ),
            LaboratorMetric(
                id = "return-rate",
                label = "Rata de revenire",
                value = "—",
                insufficientData = true
            ),
            LaboratorMetric(
                id = "unique-patients",
                label = "Pacienți unici în grup",
                value = "214",
                changeText = "4% față de perioada anterioară"
            )
        )

        LaboratorPeriod.THREE_MONTHS -> listOf(
            LaboratorMetric(
                id = "processed",
                label = "Rezultate procesate",
                value = "3.680",
                changeText = "18% față de perioada anterioară"
            ),
            LaboratorMetric(
                id = "accessed",
                label = "Rezultate accesate",
                value = "24.870",
                changeText = "12% față de perioada anterioară"
            ),
            LaboratorMetric(
                id = "return-rate",
                label = "Rata de revenire",
                value = "—",
                insufficientData = true
            ),
            LaboratorMetric(
                id = "unique-patients",
                label = "Pacienți unici în grup",
                value = "2.140",
                changeText = "15% față de perioada anterioară"
            )
        )

        LaboratorPeriod.THIRTY_DAYS,
        LaboratorPeriod.CUSTOM -> listOf(
            LaboratorMetric(
                id = "processed",
                label = "Rezultate procesate",
                value = "1.240",
                changeText = "12% față de perioada anterioară"
            ),
            LaboratorMetric(
                id = "accessed",
                label = "Rezultate accesate",
                value = "8.302",
                changeText = "5.4% față de perioada anterioară"
            ),
            LaboratorMetric(
                id = "return-rate",
                label = "Rata de revenire",
                value = "—",
                insufficientData = true
            ),
            LaboratorMetric(
                id = "unique-patients",
                label = "Pacienți unici în grup",
                value = "945",
                changeText = "8% față de perioada anterioară"
            )
        )
    }
}

private fun activityFor(period: LaboratorPeriod): LaboratorActivitySeries {
    return when (period) {
        LaboratorPeriod.SEVEN_DAYS -> LaboratorActivitySeries(
            processed = listOf(0.20f, 0.25f, 0.23f, 0.30f, 0.28f, 0.36f, 0.34f),
            accessed = listOf(0.35f, 0.40f, 0.38f, 0.47f, 0.45f, 0.55f, 0.52f),
            uniquePatients = listOf(0.10f, 0.12f, 0.13f, 0.15f, 0.16f, 0.18f, 0.20f)
        )

        LaboratorPeriod.THREE_MONTHS -> LaboratorActivitySeries(
            processed = listOf(0.22f, 0.28f, 0.25f, 0.34f, 0.38f, 0.35f, 0.46f, 0.52f, 0.49f, 0.60f, 0.65f, 0.62f),
            accessed = listOf(0.36f, 0.42f, 0.39f, 0.51f, 0.56f, 0.53f, 0.65f, 0.71f, 0.68f, 0.80f, 0.86f, 0.82f),
            uniquePatients = listOf(0.12f, 0.15f, 0.17f, 0.20f, 0.22f, 0.24f, 0.28f, 0.31f, 0.34f, 0.38f, 0.42f, 0.45f)
        )

        LaboratorPeriod.THIRTY_DAYS,
        LaboratorPeriod.CUSTOM -> LaboratorActivitySeries(
            processed = listOf(0.30f, 0.32f, 0.35f, 0.33f, 0.39f, 0.37f, 0.46f, 0.43f, 0.56f, 0.50f, 0.62f, 0.59f),
            accessed = listOf(0.48f, 0.52f, 0.56f, 0.53f, 0.64f, 0.60f, 0.72f, 0.74f, 0.90f, 0.74f, 0.82f, 0.88f),
            uniquePatients = listOf(0.16f, 0.18f, 0.20f, 0.22f, 0.23f, 0.25f, 0.28f, 0.30f, 0.33f, 0.36f, 0.40f, 0.42f)
        )
    }
}

private val TREND = LaboratorTrendSummary(
    prefix = "În ultimele 3 luni, numărul pacienților unici care au accesat rezultate a crescut cu ",
    highlight = "14%",
    suffix = " față de perioada precedentă.",
    footnote = "Date insuficiente pentru a genera tendințe predictive (necesar minim 30 pacienți unici în interval)."
)

private val TOP_ANALYSES = listOf(
    LaboratorTopAnalysis(
        rank = 1,
        title = "Hemoleucogramă completă",
        subtitle = "Hematologie automată 5-diff",
        percent = 42
    ),
    LaboratorTopAnalysis(
        rank = 2,
        title = "Profil lipidic",
        subtitle = "Colesterol, HDL, LDL, Trigliceride",
        percent = 28
    ),
    LaboratorTopAnalysis(
        rank = 3,
        title = "Glicemie serică",
        subtitle = "Spectrofotometrie enzimatică",
        percent = 18
    ),
    LaboratorTopAnalysis(
        rank = 4,
        title = "TSH (Hormon de stimulare)",
        subtitle = "Imunochimie cu chemiluminescență",
        percent = 12
    ),
    LaboratorTopAnalysis(
        rank = 5,
        title = "Vitamina D (25-OH)",
        subtitle = "ECLIA - Analizor Cobas",
        insufficientData = true
    )
)
