package com.semanticsoft.patientmobile.feature.laborator.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.semanticsoft.patientmobile.core.ui.components.ScreenTopBar

@Composable
fun LaboratorNavScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val tabs = listOf(
        LaboratorTab.Home,
        LaboratorTab.Submissions,
        LaboratorTab.Results,
        LaboratorTab.Profile
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ScreenTopBar(titlePrefix = "Laborator", titleHighlight = "")
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(LaboratorTab.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (!tab.tintable && currentRoute == tab.route) {
                                    Color.Unspecified
                                } else {
                                    LocalContentColor.current
                                }
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
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
                onLogout = onLogout
            )
        }
    }
}
