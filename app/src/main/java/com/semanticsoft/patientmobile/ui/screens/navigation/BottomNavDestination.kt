package com.semanticsoft.patientmobile.ui.screens.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Home : BottomNavDestination("home", "Acasă", Icons.Filled.Home)
    data object History : BottomNavDestination("history", "Istoric", Icons.Outlined.Description)
    data object Analyses : BottomNavDestination("analyses", "Analize", Icons.Outlined.FolderOpen)
    data object Profile : BottomNavDestination("profile", "Profil", Icons.Filled.Person)
}
