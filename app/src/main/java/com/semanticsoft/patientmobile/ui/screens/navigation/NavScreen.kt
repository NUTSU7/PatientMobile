package com.semanticsoft.patientmobile.ui.screens.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import com.semanticsoft.patientmobile.R
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardScreen
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardUiState
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryScreen
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

private enum class PostLoginTab {
    MedicalHystory,
    Dashboard
}

@Composable
fun NavScreen(
    state: DashboardUiState,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(PostLoginTab.Dashboard) }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage?.lowercase().orEmpty()
        if (message.contains("session expired") || message.contains("unauthorized") || message.contains("401")) {
            onLogout()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = max(0.84f, min(maxWidth.value / 375f, 1.1f))
        val drawerWidth = min(max(maxWidth.value * 0.84f, 248f), 320f).dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)),
                                    startY = 0f,
                                    endY = size.height
                                )
                            )
                        },
                    drawerContainerColor = Color.Transparent,
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
                            selectedTab = PostLoginTab.MedicalHystory
                            scope.launch { drawerState.close() }
                        },
                        onLogout = {
                            onLogout()
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            when (selectedTab) {
                PostLoginTab.MedicalHystory -> {
                    MedicalHystoryScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNotificationsClick = { },
                        onInfoClick = { }
                    )
                }

                PostLoginTab.Dashboard -> {
                    DashboardScreen(
                        state = state,
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
    onSelectHistory: () -> Unit,
    onLogout: () -> Unit
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
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = (16f * scale).dp, vertical = (18f * scale).dp)
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
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape((10f * scale).dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sidebar_pulse),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size((20f * scale).dp)
                    )
                }

                Spacer(modifier = Modifier.width((12f * scale).dp))

                Text(
                    text = "Patient.md",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (24f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.2).sp
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

        Spacer(modifier = Modifier.height((26f * scale).dp))

        DrawerMenuItem(
            label = "Panou principal",
            selected = selectedTab == PostLoginTab.Dashboard,
            scale = scale,
            activeIcon = Icons.Outlined.GridView,
            onClick = onSelectDashboard
        )

        Spacer(modifier = Modifier.height((8f * scale).dp))

        DrawerMenuItem(
            label = "Analize încărcate",
            selected = false,
            scale = scale,
            activeIcon = Icons.Outlined.Description,
            onClick = { }
        )

        Spacer(modifier = Modifier.height((8f * scale).dp))

        DrawerMenuItem(
            label = "Istoric medical",
            selected = selectedTab == PostLoginTab.MedicalHystory,
            scale = scale,
            activeIcon = Icons.Outlined.Description,
            onClick = onSelectHistory
        )

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height((76f * scale).dp),
            color = Color.White.copy(alpha = 0.12f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape((14f * scale).dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = (14f * scale).dp),
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName.split(" ").firstOrNull().orEmpty().ifBlank { "Diana" },
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

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size((32f * scale).dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Mai multe opțiuni",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size((20f * scale).dp)
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
