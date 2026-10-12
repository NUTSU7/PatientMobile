package com.semanticsoft.patientmobile.core.ui.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Purple500

@Composable
fun GradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    val buttonShape = RoundedCornerShape(AppDimens.cornerRadiusPill)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = buttonShape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(
                Brush.linearGradient(
                    0.0f to Blue900,
                    0.5f to Blue500,
                    1.0f to Purple500
                ),
                buttonShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}