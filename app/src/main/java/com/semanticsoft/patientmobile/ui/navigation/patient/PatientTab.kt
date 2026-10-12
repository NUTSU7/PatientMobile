package com.semanticsoft.patientmobile.ui.navigation.patient

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.ui.graphics.vector.ImageVector

sealed class PatientTab(val route: String, val label: String, val icon: ImageVector) {
    data object Home : PatientTab("home", "Acasă", Icons.Filled.Home)
    data object History : PatientTab("history", "Istoric", Icons.Outlined.Description)
    data object Analyses : PatientTab("analyses", "Analize", Icons.Outlined.FolderOpen)
    data object Profile : PatientTab("profile", "Profil", Icons.Filled.Person)
}
