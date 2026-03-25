package com.semanticsoft.patientmobile.ui.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Registration : AppDestination("registration")
    data object Dashboard : AppDestination("dashboard")
    data object Upload : AppDestination("upload")
}
