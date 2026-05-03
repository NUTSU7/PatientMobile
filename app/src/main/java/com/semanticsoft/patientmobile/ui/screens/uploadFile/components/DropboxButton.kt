package com.semanticsoft.patientmobile.ui.screens.uploadFile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DropboxButton(
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height((41.6f * scale).dp)
            .border(
                width = (1f * scale).dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape((8f * scale).dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape((8f * scale).dp)
            )
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Dropbox",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (11.9f * scale).sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (20f * scale).sp
            ),
            color = Color(0xFF374151)
        )
    }
}
