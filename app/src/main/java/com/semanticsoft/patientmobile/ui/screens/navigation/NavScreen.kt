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
import com.semanticsoft.patientmobile.ui.screens.navigation.components.PostLoginDrawerContent
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.UploadedAnalysesScreen
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

internal enum class PostLoginTab {
    MedicalHystory,
    Dashboard,
    UploadedAnalyses
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
                        onSelectUploadedAnalyses = {
                            selectedTab = PostLoginTab.UploadedAnalyses
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

                PostLoginTab.UploadedAnalyses -> {
                    UploadedAnalysesScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNotificationsClick = { },
                        onInfoClick = { }
                    )
                }
            }
        }
    }
}
