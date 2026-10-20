package com.semanticsoft.patientmobile.feature.laborator.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.Purple500
import com.semanticsoft.patientmobile.core.ui.theme.icons.UploadResultIcon

@Composable
internal fun LaboratorUploadFab(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Indigo600.copy(alpha = 0.22f),
                spotColor = Indigo600.copy(alpha = 0.22f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    0.0f to Blue900,
                    0.5f to Blue500,
                    1.0f to Purple500
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = UploadResultIcon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
