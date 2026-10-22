package com.semanticsoft.patientmobile.feature.laborator.common

enum class LaboratorPeriod(
    val label: String,
    val filterLabel: String,
    val activityLabel: String
) {
    SEVEN_DAYS("7 zile", "7 zile", "ULTIMELE 7 ZILE"),
    THIRTY_DAYS("30 zile", "30 zile", "ULTIMELE 30 ZILE"),
    THREE_MONTHS("3 luni", "3 luni", "ULTIMELE 3 LUNI"),
    CUSTOM("Personalizat", "Filtrare personalizată", "PERIOADĂ PERSONALIZATĂ")
}
