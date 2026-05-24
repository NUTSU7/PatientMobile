package com.semanticsoft.patientmobile.ui.screens.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.screens.navigation.BottomNavDestination
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(
        BottomNavDestination.Home,
        BottomNavDestination.History
    )

    val destinationsRight = listOf(
        BottomNavDestination.Analyses,
        BottomNavDestination.Profile
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = Color.White,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { dest ->
                BottomNavItem(
                    icon = dest.icon,
                    label = dest.label,
                    selected = currentRoute == dest.route,
                    onClick = { onNavigate(dest.route) },
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Indigo600, Purple500))
                        )
                        .clickable(onClick = onUploadClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "\u00CEncarc\u0103 analize",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            destinationsRight.forEach { dest ->
                BottomNavItem(
                    icon = dest.icon,
                    label = dest.label,
                    selected = currentRoute == dest.route,
                    onClick = { onNavigate(dest.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
