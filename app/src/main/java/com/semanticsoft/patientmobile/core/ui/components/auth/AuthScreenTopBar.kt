package com.semanticsoft.patientmobile.core.ui.components.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Purple500
import com.semanticsoft.patientmobile.core.ui.theme.icons.PatientMarkIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ShieldCheckIcon

@Composable
fun AuthScreenTopBar(
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = PatientMarkIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 32.dp, height = 30.36.dp)
            )
            Text(
                text = "Patient",
                color = Color.Unspecified,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.6).sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(
                        0.0f to Blue900,
                        0.5f to Blue500,
                        1.0f to Purple500
                    )
                )
            )
        }

        if (trailing != null) {
            trailing()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = ShieldCheckIcon,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(width = 12.dp, height = 14.dp)
                )
                Text(
                    text = "Conexiune securizată SSL",
                    color = Gray500,
                    fontSize = 12.sp
                )
            }
        }
    }
}
