package com.semanticsoft.patientmobile.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Purple500
import com.semanticsoft.patientmobile.core.ui.theme.icons.ArrowForwardIcon

@Composable
fun GradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = ArrowForwardIcon,
    enabled: Boolean = true,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
) {
    val buttonShape = RoundedCornerShape(AppDimens.cornerRadiusPill)
    val buttonBrush = if (enabled) {
        Brush.linearGradient(
            0.0f to Blue900,
            0.5f to Blue500,
            1.0f to Purple500
        )
    } else {
        Brush.linearGradient(
            0.0f to Blue900.copy(alpha = 0.25f),
            0.5f to Blue500.copy(alpha = 0.25f),
            1.0f to Purple500.copy(alpha = 0.25f)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = buttonShape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(buttonBrush, buttonShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = fontSize,
            lineHeight = 18.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
        if (icon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}