package com.semanticsoft.patientmobile.ui.screens.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardScreen
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardUiState
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

private enum class PostLoginTab {
    Dashboard,
    AnalysisHistory
}

@Composable
fun NavScreen(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(PostLoginTab.Dashboard) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = max(0.86f, min(maxWidth.value / 360f, 1.1f))
        val drawerWidth = min(maxWidth.value * 0.8f, 300f).dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight(),
                    drawerContainerColor = Color(0xFF1E1B4B),
                    drawerContentColor = Color.White
                ) {
                    PostLoginDrawerContent(
                        fullName = if (state.fullName.isBlank()) state.greetingName else state.fullName,
                        role = if (state.role.isBlank()) "Pacient" else state.role,
                        profilePhotoResId = state.profilePhotoResId,
                        selectedTab = selectedTab,
                        scale = scale,
                        onClose = { scope.launch { drawerState.close() } },
                        onSelectDashboard = {
                            selectedTab = PostLoginTab.Dashboard
                            scope.launch { drawerState.close() }
                        },
                        onSelectHistory = {
                            selectedTab = PostLoginTab.AnalysisHistory
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            when (selectedTab) {
                PostLoginTab.Dashboard -> {
                    DashboardScreen(
                        state = state,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }

                PostLoginTab.AnalysisHistory -> {
                    AnalysisHistoryScreen(
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun PostLoginDrawerContent(
    fullName: String,
    role: String,
    profilePhotoResId: Int?,
    selectedTab: PostLoginTab,
    scale: Float,
    onClose: () -> Unit,
    onSelectDashboard: () -> Unit,
    onSelectHistory: () -> Unit
) {
    val initials = remember(fullName) {
        fullName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "AP" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B4B))
            .padding(horizontal = (16f * scale).dp, vertical = (24f * scale).dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size((36f * scale).dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape((6f * scale).dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size((20f * scale).dp)
                    )
                }

                Spacer(modifier = Modifier.width((12f * scale).dp))

                Text(
                    text = "PACIENT.MD",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (11.9f * scale).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            IconButton(onClick = onClose, modifier = Modifier.size((24f * scale).dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Închide meniu",
                    tint = Color.White,
                    modifier = Modifier.size((24f * scale).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height((24f * scale).dp))

        DrawerMenuItem(
            label = "Panou principal",
            selected = selectedTab == PostLoginTab.Dashboard,
            scale = scale,
            activeIcon = Icons.Outlined.Dashboard,
            onClick = onSelectDashboard
        )

        Spacer(modifier = Modifier.height((8f * scale).dp))

        DrawerMenuItem(
            label = "Istoric Analize",
            selected = selectedTab == PostLoginTab.AnalysisHistory,
            scale = scale,
            activeIcon = Icons.Outlined.Description,
            onClick = onSelectHistory
        )

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height((72f * scale).dp),
            color = Color.White.copy(alpha = 0.05f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape((12f * scale).dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = (16f * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size((40f * scale).dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(0xFF5A52E5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoResId != null) {
                        Image(
                            painter = painterResource(id = profilePhotoResId),
                            contentDescription = "Fotografie profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = (11.9f * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width((12f * scale).dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = fullName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (11.9f * scale).sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = role,
                        color = Color(0xFFA5B4FC),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (10.2f * scale).sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    label: String,
    selected: Boolean,
    scale: Float,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color.White.copy(alpha = 0.1f) else Color.Transparent
    val foregroundColor = if (selected) Color.White else Color(0xFFA5B4FC)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height((44f * scale).dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape((8f * scale).dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = (16f * scale).dp),
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
                fontSize = (11.9f * scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
