package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPlaceholderText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.icons.InfoCircleIcon

@Composable
internal fun EngagementPrivacyFooter(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceWhite.copy(alpha = 0.72f), RoundedCornerShape(14.dp))
            .border(1.25.dp, LaboratorPanelBg, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = InfoCircleIcon,
            contentDescription = null,
            tint = LaboratorPlaceholderText,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(20.dp)
        )
        Text(
            text = text,
            color = LaboratorPlaceholderText,
            fontSize = 10.sp,
            lineHeight = 15.5.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}
