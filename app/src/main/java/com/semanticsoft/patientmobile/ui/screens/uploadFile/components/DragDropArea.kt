package com.semanticsoft.patientmobile.ui.screens.uploadFile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.theme.icons.CloudDownloadIcon
import kotlin.math.max
import kotlin.math.min

@Composable
fun DragDropArea(
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((179.2f * scale).dp)
            .border(
                width = (1f * scale).dp,
                color = Color(0xFFD1D5DB),
                shape = RoundedCornerShape((12f * scale).dp)
            )
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape((12f * scale).dp)
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size((48f * scale).dp)
                    .background(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape((9999f * scale).dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CloudDownloadIcon,
                    contentDescription = "Upload",
                    modifier = Modifier.size((24f * scale).dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height((12f * scale).dp))

            Text(
                text = "Trage aici PDF-uri/imagini",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (13.6f * scale).sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = (24f * scale).sp
                ),
                color = Color(0xFF374151),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height((4f * scale).dp))

            Text(
                text = "sau click pentru a naviga",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (11.9f * scale).sp,
                    lineHeight = (20f * scale).sp
                ),
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}
