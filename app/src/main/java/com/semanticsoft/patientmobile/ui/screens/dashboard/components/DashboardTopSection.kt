package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardUiState
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500

@Composable
fun DashboardTopSection(
    state: DashboardUiState,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    onMenuClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    ScreenTopBar(
        horizontalPadding = horizontalPadding,
        onMenuClick = onMenuClick,
        titleContent = { dimensions ->
            Text(
                text = "Salut ",
                color = Color(0xFF111827),
                fontSize = dimensions.titleSize,
                lineHeight = dimensions.titleLineHeight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.greetingName,
                color = Color(0xFF4F46E5),
                fontSize = dimensions.titleSize,
                lineHeight = dimensions.titleLineHeight,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = ",",
                color = Color(0xFF111827),
                fontSize = dimensions.titleSize,
                lineHeight = dimensions.titleLineHeight,
                fontWeight = FontWeight.Bold
            )
        },
        titleTrailingContent = { dimensions ->
            IconButton(
                onClick = { },
                modifier = Modifier.size(dimensions.actionButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notific\u0103ri",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(dimensions.actionIconSize)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { },
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
                text = "Se pare c\u0103 ai f\u0103cut ultimele analize pe ${state.lastAnalysisDate}",
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actionsContent = { dimensions ->
            Text(
                text = "\u00CEncarc\u0103 analize",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = dimensions.primaryActionTextSize,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(listOf(Indigo600, Purple500)))
                    .clickable(onClick = onUploadClick)
                    .padding(vertical = 12.dp)
            )
        }
    )
}
