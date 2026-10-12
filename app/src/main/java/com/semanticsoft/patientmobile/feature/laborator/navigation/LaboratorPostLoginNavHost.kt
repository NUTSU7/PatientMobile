package com.semanticsoft.patientmobile.feature.laborator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorDashboardScreen
import com.semanticsoft.patientmobile.feature.laborator.profile.LaboratorProfileScreen
import com.semanticsoft.patientmobile.feature.laborator.results.LaboratorResultsScreen
import com.semanticsoft.patientmobile.feature.laborator.submissions.LaboratorSubmissionsScreen

@Composable
fun LaboratorPostLoginNavHost(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = LaboratorTab.Home.route,
        modifier = modifier
    ) {
        composable(LaboratorTab.Home.route) {
            LaboratorDashboardScreen()
        }

        composable(LaboratorTab.Submissions.route) {
            LaboratorSubmissionsScreen()
        }

        composable(LaboratorTab.Results.route) {
            LaboratorResultsScreen()
        }

        composable(LaboratorTab.Profile.route) {
            LaboratorProfileScreen(onLogout = onLogout)
        }
    }
}
