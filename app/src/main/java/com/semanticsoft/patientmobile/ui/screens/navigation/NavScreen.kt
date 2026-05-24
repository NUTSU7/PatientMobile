package com.semanticsoft.patientmobile.ui.screens.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileScreen
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileViewModel
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileEvent
import com.semanticsoft.patientmobile.ui.shared.upload.rememberUploadFileLaunchers
import com.semanticsoft.patientmobile.ui.screens.navigation.components.BottomNavBar
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardUiState
import kotlinx.coroutines.flow.collect
import androidx.compose.material3.Scaffold

@Composable
fun NavScreen(
    dashboardState: StateFlow<DashboardUiState>,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUploadModal by rememberSaveable { mutableStateOf(false) }
    val uploadFileViewModel: UploadFileViewModel = hiltViewModel()
    val uploadFileState by uploadFileViewModel.state.collectAsStateWithLifecycle()
    val uploadLaunchers = rememberUploadFileLaunchers(uploadFileViewModel)
    val refreshTrigger = remember { MutableSharedFlow<Unit>() }
    val profileState by dashboardState.collectAsStateWithLifecycle()

    LaunchedEffect(uploadFileViewModel) {
        uploadFileViewModel.events.collect { event ->
            when (event) {
                is UploadFileEvent.AllFilesUploaded -> {
                    showUploadModal = false
                    uploadFileViewModel.resetUploadComplete()
                    refreshTrigger.emit(Unit)
                }
            }
        }
    }

    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            when (currentRoute) {
                BottomNavDestination.Home.route -> ScreenTopBar(
                    titlePrefix = "Salut ",
                    titleHighlight = profileState.greetingName,
                    titleSuffix = ","
                )
                BottomNavDestination.History.route -> ScreenTopBar(
                    titlePrefix = "Istoric ",
                    titleHighlight = "medical"
                )
                BottomNavDestination.Analyses.route -> ScreenTopBar(
                    titlePrefix = "Analize ",
                    titleHighlight = "încărcate"
                )
                BottomNavDestination.Profile.route -> ScreenTopBar(
                    titlePrefix = "Profil",
                    titleHighlight = ""
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(BottomNavDestination.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onUploadClick = {
                    if (!showUploadModal) uploadFileViewModel.reset()
                    showUploadModal = !showUploadModal
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            PostLoginNavHost(
                navController = navController,
                dashboardState = dashboardState,
                onUploadClick = {
                    if (!showUploadModal) uploadFileViewModel.reset()
                    showUploadModal = !showUploadModal
                },
                onLogout = onLogout,
                refreshTrigger = refreshTrigger
            )

            AnimatedVisibility(
                visible = showUploadModal,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                UploadFileScreen(
                    state = uploadFileState,
                    viewModel = uploadFileViewModel,
                    onDismiss = {
                        uploadFileViewModel.reset()
                        showUploadModal = false
                    },
                    onCameraClick = uploadLaunchers.onCameraClick,
                    onGalleryClick = uploadLaunchers.onGalleryClick,
                    onFilePickerClick = uploadLaunchers.onFilePickerClick
                )
            }
        }
    }
}
