package com.estcomputer.patient.feature.patient.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.estcomputer.patient.feature.patient.dashboard.DashboardScreen
import com.estcomputer.patient.feature.patient.dashboard.DashboardUiState
import com.estcomputer.patient.feature.patient.upload.ProcessingStatus
import com.estcomputer.patient.feature.patient.medicalHistory.MedicalHistoryScreen
import com.estcomputer.patient.feature.patient.profile.ProfileScreen
import com.estcomputer.patient.feature.patient.uploadedAnalyses.UploadedAnalysesScreen
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
