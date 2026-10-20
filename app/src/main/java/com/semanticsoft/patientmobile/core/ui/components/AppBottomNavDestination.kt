package com.semanticsoft.patientmobile.core.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

interface AppBottomNavDestination {
    val route: String
    val label: String
    val icon: ImageVector
    val tintable: Boolean
}
