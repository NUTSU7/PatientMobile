package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses

/**
 * Temporary prototype data for the frequent analyses screen.
 *
 * Replace with a repository-backed data source once laborator endpoints exist.
 */
internal const val FREQUENT_ANALYSES_TOTAL_COUNT = 124

internal fun mockFrequentAnalyses(): List<FrequentAnalysis> {
    return listOf(
        FrequentAnalysis(
            id = "hemoleucograma-completa",
            name = "Hemoleucogramă completă",
            subtitle = "Hematologie automată 5-diff",
            accessedResults = 1_240,
            uniquePatients = 890
        ),
        FrequentAnalysis(
            id = "profil-lipidic",
            name = "Profil lipidic",
            subtitle = "Colesterol, HDL, LDL, Trigliceride",
            accessedResults = 820,
            uniquePatients = 614
        ),
        FrequentAnalysis(
            id = "glicemie-serica",
            name = "Glicemie serică",
            subtitle = "Spectrofotometrie enzimatică",
            accessedResults = 540,
            uniquePatients = 421
        ),
        FrequentAnalysis(
            id = "tsh",
            name = "TSH (Hormon de stimulare)",
            subtitle = "Imunochimie cu chemiluminescență",
            accessedResults = 355,
            uniquePatients = 280
        ),
        FrequentAnalysis(
            id = "vitamina-d",
            name = "Vitamina D (25-OH)",
            subtitle = "ECLIA - Analizor Cobas",
            accessedResults = 87,
            uniquePatients = null
        ),
        FrequentAnalysis(
            id = "creatinina-serica",
            name = "Creatinină serică",
            subtitle = "Fiziologie renală, spectrofotometrie",
            accessedResults = 78,
            uniquePatients = null
        ),
        FrequentAnalysis(
            id = "alat-tgp",
            name = "ALAT (TGP)",
            subtitle = "Biochimie funcție hepatică",
            accessedResults = 64,
            uniquePatients = null
        ),
        FrequentAnalysis(
            id = "vsh",
            name = "VSH",
            subtitle = "Test de inflamație nespecific",
            accessedResults = 52,
            uniquePatients = null
        )
    )
}
