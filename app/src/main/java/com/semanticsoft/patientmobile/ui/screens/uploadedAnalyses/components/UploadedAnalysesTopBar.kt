package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.UploadedAnalysesUiState
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500

@Composable
fun UploadedAnalysesTopBar(
    state: UploadedAnalysesUiState,
    horizontalPadding: Dp,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onUploadClick: () -> Unit = { }
) {
    ScreenTopBar(
        horizontalPadding = horizontalPadding,
        onMenuClick = onMenuClick,
        titleContent = { dimensions ->
            Text(
                text = "Analize ",
                color = Color(0xFF111827),
                fontSize = dimensions.titleSize,
                lineHeight = dimensions.titleLineHeight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "încărcate",
                color = Color(0xFF4F46E5),
                fontSize = dimensions.titleSize,
                lineHeight = dimensions.titleLineHeight,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        titleTrailingContent = { dimensions ->
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(dimensions.actionButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notificări",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(dimensions.actionIconSize)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(dimensions.actionButtonSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Ajutor",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(dimensions.actionIconSize)
                )
            }
        },
        subtitleContent = {
            Text(
                text = state.latestUploadDate?.let { "Ultima analiză încărcată pe $it" }
                    ?: "Documentele tale medicale încărcate",
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actionsContent = { dimensions ->
            Text(
                text = "Încarcă analiză",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = dimensions.primaryActionTextSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(listOf(Indigo600, Purple500)))
                    .clickable { onUploadClick() }
                    .padding(vertical = 12.dp)
            )
        }
    )
}
