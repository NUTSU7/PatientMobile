package com.semanticsoft.patientmobile.ui.screens.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerMenuItem(
    label: String,
    selected: Boolean,
    scale: Float,
    activeIcon: ImageVector,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color.White.copy(alpha = 0.2f) else Color.Transparent
    val foregroundColor = if (selected) Color.White else Color.White.copy(alpha = 0.82f)
    val borderColor = if (selected) Color.White.copy(alpha = 0.32f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height((46f * scale).dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape((12f * scale).dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape((12f * scale).dp))
            .clickable(onClick = onClick)
            .padding(horizontal = (14f * scale).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = activeIcon,
            contentDescription = null,
            tint = foregroundColor,
            modifier = Modifier.size((20f * scale).dp)
        )

        Spacer(modifier = Modifier.width((16f * scale).dp))

        Text(
            text = label,
            color = foregroundColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (12.2f * scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
