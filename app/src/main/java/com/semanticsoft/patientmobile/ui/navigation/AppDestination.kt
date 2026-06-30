package com.semanticsoft.patientmobile.ui.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Registration : AppDestination("registration")
    data object Dashboard : AppDestination("dashboard")
    data object Upload : AppDestination("upload")
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
