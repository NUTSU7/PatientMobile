package com.semanticsoft.patientmobile.feature.laborator.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.semanticsoft.patientmobile.core.ui.components.AppBottomNavBar
import com.semanticsoft.patientmobile.core.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorDashboardUiState
import com.semanticsoft.patientmobile.feature.laborator.navigation.components.LaboratorUploadFab
import kotlinx.coroutines.flow.StateFlow

private val laboratorTabs = listOf(
    LaboratorTab.Home,
    LaboratorTab.Analyses,
    LaboratorTab.Statistics,
    LaboratorTab.Profile
)

@Composable
fun LaboratorNavScreen(
    dashboardState: StateFlow<LaboratorDashboardUiState>,
    onPeriodSelected: (LaboratorPeriod) -> Unit,
    onCompareToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dashboard by dashboardState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            when (currentRoute) {
                LaboratorTab.Home.route -> ScreenTopBar(
                    titlePrefix = "Salut, ${dashboard.labName}",
                    titleHighlight = ""
                )
                LaboratorTab.Analyses.route -> ScreenTopBar(
                    titlePrefix = "Analize ",
                    titleHighlight = "frecvente"
                )
                LaboratorTab.Statistics.route -> ScreenTopBar(
                    titlePrefix = "Pacienți și ",
                    titleHighlight = "Implicare"
                )
                LaboratorTab.Profile.route -> ScreenTopBar(
                    titlePrefix = "Profilul ",
                    titleHighlight = "meu"
                )
                else -> ScreenTopBar(
                    titlePrefix = "Laborator",
                    titleHighlight = ""
                )
            }
        },
        bottomBar = {
            AppBottomNavBar(
                items = laboratorTabs,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(LaboratorTab.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onCenterClick = {},
                centerContentDescription = "Adaugă rezultat"
            ) {
                LaboratorUploadFab()
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LaboratorPostLoginNavHost(
                navController = navController,
                dashboardState = dashboard,
                onPeriodSelected = onPeriodSelected,
                onCompareToggle = onCompareToggle,
                onLogout = onLogout
            )
        }
    }
}
