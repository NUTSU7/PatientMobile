package com.semanticsoft.patientmobile.ui.navigation.laborator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semanticsoft.patientmobile.ui.screens.laborator.dashboard.LaboratorDashboardScreen
import com.semanticsoft.patientmobile.ui.screens.laborator.profile.LaboratorProfileScreen
import com.semanticsoft.patientmobile.ui.screens.laborator.results.LaboratorResultsScreen
import com.semanticsoft.patientmobile.ui.screens.laborator.submissions.LaboratorSubmissionsScreen

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
