package com.semanticsoft.patientmobile.ui.navigation.laborator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.ui.graphics.vector.ImageVector

sealed class LaboratorTab(val route: String, val label: String, val icon: ImageVector) {
    data object Home : LaboratorTab("lab-home", "Panou", Icons.Filled.Home)
    data object Submissions : LaboratorTab("lab-submissions", "Lucrari", Icons.Outlined.FolderOpen)
    data object Results : LaboratorTab("lab-results", "Rezultate", Icons.Outlined.Description)
    data object Profile : LaboratorTab("lab-profile", "Profil", Icons.Filled.Person)
}
