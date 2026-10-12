package com.semanticsoft.patientmobile.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object PatientRegistration : AppDestination("patient-registration")
    data object LaboratorRegistration : AppDestination("laborator-registration")
    data object ForgotPassword : AppDestination("forgot-password")
    data object Patient : AppDestination("patient")
    data object Laborator : AppDestination("laborator")
    data class AnalysisExplanation(val documentId: String) : AppDestination("analysis_explanation/$documentId") {
        companion object {
            const val ROUTE_PATTERN = "analysis_explanation/{documentId}"
            const val ARG_DOCUMENT_ID = "documentId"
        }
    }
    data class ReportResults(val reportId: String) : AppDestination("report_results/$reportId") {
        companion object {
            const val ROUTE_PATTERN = "report_results/{reportId}"
            const val ARG_REPORT_ID = "reportId"
        }
    }
}
