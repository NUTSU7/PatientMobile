package com.semanticsoft.patientmobile.feature.laborator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semanticsoft.patientmobile.feature.laborator.common.Period
import com.semanticsoft.patientmobile.feature.laborator.dashboard.DashboardScreen
import com.semanticsoft.patientmobile.feature.laborator.dashboard.DashboardUiState
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.DigitalEngagementScreen
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FrequentAnalysesScreen
import com.semanticsoft.patientmobile.feature.laborator.profile.ProfileScreen

@Composable
fun LaboratorPostLoginNavHost(
    navController: NavHostController,
    dashboardState: DashboardUiState,
    onPeriodSelected: (Period) -> Unit,
    onCompareToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = LaboratorTab.Home.route,
        modifier = modifier
    ) {
        composable(LaboratorTab.Home.route) {
            DashboardScreen(
                state = dashboardState,
                onPeriodSelected = onPeriodSelected,
                onCompareToggle = onCompareToggle
            )
        }

        composable(LaboratorTab.Analyses.route) {
            FrequentAnalysesScreen()
        }

        composable(LaboratorTab.Statistics.route) {
            DigitalEngagementScreen()
        }

        composable(LaboratorTab.Profile.route) {
            ProfileScreen(onLogout = onLogout)
        }
    }
}
