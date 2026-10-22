package com.semanticsoft.patientmobile.feature.laborator.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.semanticsoft.patientmobile.core.ui.components.AppBottomNavDestination
import com.semanticsoft.patientmobile.core.ui.theme.icons.DashboardIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.FrequentAnalysesIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.PatientsEngagementIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ProfileIcon

sealed class LaboratorTab(
    override val route: String,
    override val label: String,
    override val icon: ImageVector,
    override val tintable: Boolean = true
) : AppBottomNavDestination {
    data object Home : LaboratorTab("lab-home", "Acasă", DashboardIcon, tintable = false)
    data object Analyses : LaboratorTab("lab-analyses", "Analize", FrequentAnalysesIcon, tintable = false)
    data object Statistics : LaboratorTab("lab-statistics", "Implicare", PatientsEngagementIcon, tintable = false)
    data object Profile : LaboratorTab("lab-profile", "Profil", ProfileIcon, tintable = false)
}
