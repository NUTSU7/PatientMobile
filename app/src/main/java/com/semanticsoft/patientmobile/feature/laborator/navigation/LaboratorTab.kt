package com.semanticsoft.patientmobile.feature.laborator.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.ui.graphics.vector.ImageVector
import com.semanticsoft.patientmobile.core.ui.theme.icons.DashboardIcon

sealed class LaboratorTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val tintable: Boolean = true
) {
    data object Home : LaboratorTab("lab-home", "Panou", DashboardIcon, tintable = false)
    data object Submissions : LaboratorTab("lab-submissions", "Lucrari", Icons.Outlined.FolderOpen)
    data object Results : LaboratorTab("lab-results", "Rezultate", Icons.Outlined.Description)
    data object Profile : LaboratorTab("lab-profile", "Profil", Icons.Filled.Person)
}
