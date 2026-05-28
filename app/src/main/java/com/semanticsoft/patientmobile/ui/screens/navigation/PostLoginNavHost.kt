package com.semanticsoft.patientmobile.ui.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardScreen
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardUiState
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryScreen
import com.semanticsoft.patientmobile.ui.screens.profile.ProfileScreen
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.UploadedAnalysesScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PostLoginNavHost(
    navController: NavHostController,
    dashboardState: StateFlow<DashboardUiState>,
    onUploadClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by dashboardState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = BottomNavDestination.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavDestination.Home.route) {
            DashboardScreen(
                state = state,
                onUploadClick = onUploadClick
            )
        }

        composable(BottomNavDestination.History.route) {
            MedicalHystoryScreen()
        }

        composable(BottomNavDestination.Analyses.route) {
            UploadedAnalysesScreen()
        }

        composable(BottomNavDestination.Profile.route) {
            ProfileScreen(
                onLogout = onLogout,
                onUploadClick = onUploadClick
            )
        }
    }
}
