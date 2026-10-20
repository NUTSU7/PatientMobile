package com.semanticsoft.patientmobile.feature.patient.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.semanticsoft.patientmobile.core.ui.components.AppBottomNavDestination
import com.semanticsoft.patientmobile.core.ui.theme.icons.DashboardIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.MedicalHistoryIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ProfileIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.UploadedAnalysesIcon

sealed class PatientTab(
    override val route: String,
    override val label: String,
    override val icon: ImageVector,
    override val tintable: Boolean = true
) : AppBottomNavDestination {
    data object Home : PatientTab("home", "Acasă", DashboardIcon, tintable = false)
    data object History : PatientTab("history", "Istoric", MedicalHistoryIcon, tintable = false)
    data object Analyses : PatientTab("analyses", "Analize", UploadedAnalysesIcon, tintable = false)
    data object Profile : PatientTab("profile", "Profil", ProfileIcon, tintable = false)
}
