package com.semanticsoft.patientmobile.ui.shared.upload.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("unused")
@Composable
fun UploadButton(
    label: String,
    icon: ImageVector,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height((48f * scale).dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5A52E5),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape((8f * scale).dp),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size((20f * scale).dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.size((8f * scale).dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = (13.6f * scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
