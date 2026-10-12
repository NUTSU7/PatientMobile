package com.semanticsoft.patientmobile.ui.navigation.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileScreen
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileViewModel
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileEvent
import com.semanticsoft.patientmobile.ui.shared.upload.rememberUploadFileLaunchers
import com.semanticsoft.patientmobile.ui.navigation.patient.components.BottomNavBar
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import kotlinx.coroutines.flow.StateFlow
import com.semanticsoft.patientmobile.ui.screens.patient.dashboard.DashboardUiState
import kotlinx.coroutines.flow.collect
import androidx.compose.material3.Scaffold

@Composable
fun PatientNavScreen(
    dashboardState: StateFlow<DashboardUiState>,
    onLogout: () -> Unit,
    navigateToExplanation: (String) -> Unit = { },
    navigateToReportResults: (String) -> Unit = { },
    modifier: Modifier = Modifier
) {
    var showUploadModal by rememberSaveable { mutableStateOf(false) }
    val uploadFileViewModel: UploadFileViewModel = hiltViewModel()
    val uploadFileState by uploadFileViewModel.state.collectAsStateWithLifecycle()
    val processingStatus by uploadFileViewModel.processingStatus.collectAsStateWithLifecycle()
    val uploadLaunchers = rememberUploadFileLaunchers(uploadFileViewModel)
    val profileState by dashboardState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        uploadFileViewModel.resumeProcessingIfPending()
        onPauseOrDispose { }
    }

    LaunchedEffect(uploadFileViewModel) {
        uploadFileViewModel.events.collect { event ->
            when (event) {
                is UploadFileEvent.AllFilesUploaded -> {
                    showUploadModal = false
                    uploadFileViewModel.resetUploadComplete()
                }
                is UploadFileEvent.ProcessingComplete -> {
                    showUploadModal = false
                    uploadFileViewModel.resetUploadComplete()
                }
                is UploadFileEvent.ExtractionStarted -> { /* extraction handled via polling in ViewModel */ }
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
                PatientTab.Home.route -> ScreenTopBar(
                    titlePrefix = "Salut ",
                    titleHighlight = profileState.greetingName,
                    titleSuffix = ","
                )
                PatientTab.History.route -> ScreenTopBar(
                    titlePrefix = "Istoric ",
                    titleHighlight = "medical"
                )
                PatientTab.Analyses.route -> ScreenTopBar(
                    titlePrefix = "Analize ",
                    titleHighlight = "încărcate"
                )
                PatientTab.Profile.route -> ScreenTopBar(
                    titlePrefix = "Profilul meu",
                    titleHighlight = ""
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(PatientTab.Home.route) {
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
            PatientPostLoginNavHost(
                navController = navController,
                dashboardState = dashboardState,
                processingStatus = processingStatus,
                onUploadClick = {
                    if (!showUploadModal) uploadFileViewModel.reset()
                    showUploadModal = !showUploadModal
                },
                onLogout = onLogout,
                navigateToExplanation = navigateToExplanation,
                navigateToReportResults = navigateToReportResults
            )

            AnimatedVisibility(
                visible = showUploadModal,
                enter = fadeIn(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                UploadFileScreen(
                    visible = showUploadModal,
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
