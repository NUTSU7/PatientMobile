package com.semanticsoft.patientmobile.feature.patient.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.semanticsoft.patientmobile.core.ui.theme.Purple500

@Composable
internal fun PatientUploadFab(modifier: Modifier = Modifier) {
    val fabShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(
                elevation = 6.dp,
                shape = fabShape,
                ambientColor = Purple500.copy(alpha = 0.35f),
                spotColor = Purple500.copy(alpha = 0.35f)
            )
            .clip(fabShape)
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
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
