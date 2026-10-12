package com.semanticsoft.patientmobile.feature.patient.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semanticsoft.patientmobile.feature.patient.dashboard.DashboardScreen
import com.semanticsoft.patientmobile.feature.patient.dashboard.DashboardUiState
import com.semanticsoft.patientmobile.domain.model.patient.ProcessingStatus
import com.semanticsoft.patientmobile.feature.patient.medicalHistory.MedicalHistoryScreen
import com.semanticsoft.patientmobile.feature.patient.profile.ProfileScreen
import com.semanticsoft.patientmobile.feature.patient.uploadedAnalyses.UploadedAnalysesScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PatientPostLoginNavHost(
    navController: NavHostController,
    dashboardState: StateFlow<DashboardUiState>,
    processingStatus: ProcessingStatus = ProcessingStatus(),
    onUploadClick: () -> Unit,
    onLogout: () -> Unit,
    navigateToExplanation: (String) -> Unit = { },
    navigateToReportResults: (String) -> Unit = { },
    modifier: Modifier = Modifier
) {
    val state by dashboardState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = PatientTab.Home.route,
        modifier = modifier
    ) {
        composable(PatientTab.Home.route) {
            DashboardScreen(
                state = state,
                processingStatus = processingStatus,
                onUploadClick = onUploadClick
            )
        }

        composable(PatientTab.History.route) {
            MedicalHistoryScreen(navigateToReportResults = navigateToReportResults)
        }

        composable(PatientTab.Analyses.route) {
            UploadedAnalysesScreen(navigateToExplanation = navigateToExplanation)
        }

        composable(PatientTab.Profile.route) {
            ProfileScreen(
                onLogout = onLogout,
                onUploadClick = onUploadClick
            )
        }
    }
}
