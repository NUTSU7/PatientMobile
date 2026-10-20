package com.semanticsoft.patientmobile.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens

@Composable
fun AppBottomNavBar(
    items: List<AppBottomNavDestination>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCenterClick: () -> Unit,
    centerContentDescription: String,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit
) {
    val splitIndex = (items.size + 1) / 2
    val startItems = items.take(splitIndex)
    val endItems = items.drop(splitIndex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.bottomNavHeight)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimens.bottomNavHeight),
            shape = RoundedCornerShape(
                topStart = AppDimens.cornerRadiusMedium,
                topEnd = AppDimens.cornerRadiusMedium
            ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                startItems.forEach { destination ->
                    AppBottomNavItem(
                        destination = destination,
                        selected = currentRoute == destination.route,
                        onClick = { onNavigate(destination.route) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                endItems.forEach { destination ->
                    AppBottomNavItem(
                        destination = destination,
                        selected = currentRoute == destination.route,
                        onClick = { onNavigate(destination.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(AppDimens.bottomNavFabTouchTarget)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                        role = Role.Button,
                        onClick = onCenterClick
                    )
                    .semantics { contentDescription = centerContentDescription }
            )

            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                centerContent()
            }
        }
    }
}
