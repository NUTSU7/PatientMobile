package com.semanticsoft.patientmobile.feature.laborator.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.semanticsoft.patientmobile.core.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText

@Composable
internal fun LaboratorNavPlaceholderScreen(modifier: Modifier = Modifier) {
    SetStatusBar(color = Color.White, darkIcons = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LaboratorDashboardBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Conținut în curând",
            style = MaterialTheme.typography.bodyMedium,
            color = LaboratorMutedText
        )
    }
}
