package com.semanticsoft.patientmobile.feature.laborator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorDashboardScreen
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorDashboardUiState
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FrequentAnalysesScreen
import com.semanticsoft.patientmobile.feature.laborator.navigation.components.LaboratorNavPlaceholderScreen
import com.semanticsoft.patientmobile.feature.laborator.profile.LaboratorProfileScreen

@Composable
fun LaboratorPostLoginNavHost(
    navController: NavHostController,
    dashboardState: LaboratorDashboardUiState,
    onPeriodSelected: (LaboratorPeriod) -> Unit,
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
            LaboratorDashboardScreen(
                state = dashboardState,
                onPeriodSelected = onPeriodSelected,
                onCompareToggle = onCompareToggle
            )
        }

        composable(LaboratorTab.Analyses.route) {
            FrequentAnalysesScreen()
        }

        composable(LaboratorTab.Statistics.route) {
            LaboratorNavPlaceholderScreen()
        }

        composable(LaboratorTab.Profile.route) {
            LaboratorProfileScreen(onLogout = onLogout)
        }
    }
}
