package com.semanticsoft.patientmobile.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.screens.profile.components.AccountManagementCard
import com.semanticsoft.patientmobile.ui.screens.profile.components.ChangePasswordDialog
import com.semanticsoft.patientmobile.ui.screens.profile.components.ProfileDetailsCard
import com.semanticsoft.patientmobile.ui.screens.profile.components.ProfileStatsSection
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.AppDimens

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)

    val snackbarHostState = remember { SnackbarHostState() }

    Crossfade(
        targetState = state.isLoading && state.email.isEmpty(),
        animationSpec = tween(300),
        label = "ProfileTransition"
    ) { loading ->
        if (loading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(message = "Se \u00EEncarc\u0103...")
            }
        } else {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val horizontalPadding = when {
                maxWidth >= 430.dp -> AppDimens.paddingLarge
                else -> AppDimens.paddingDefault
            }
            val sectionGap = when {
                maxWidth >= 360.dp -> AppDimens.gapDefault
                else -> AppDimens.gapMedium
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = AppDimens.paddingDefault,
                        bottom = AppDimens.paddingDefault
                    ),
                verticalArrangement = Arrangement.spacedBy(sectionGap)
            ) {
                ProfileStatsSection(
                    totalAnalyses = state.totalAnalyses,
                    daysSinceLastAnalysis = state.daysSinceLastAnalysis
                )

                ProfileDetailsCard(
                    email = state.email,
                    onChangePasswordClick = { onEvent(ProfileEvent.OnChangePasswordClicked) }
                )

                AccountManagementCard(
                    onExportDataClick = { onEvent(ProfileEvent.OnExportDataClicked) },
                    onDeleteAccountClick = { onEvent(ProfileEvent.OnDeleteAccountClicked) }
                )

                OutlinedButton(
                    onClick = { onEvent(ProfileEvent.OnLogoutClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.buttonHeightDefault),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                    Text(text = "Deconectare")
                }

                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
            }
        }
    }

        AnimatedVisibility(
            visible = state.showChangePasswordDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onEvent(ProfileEvent.OnChangePasswordDismiss) }
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showChangePasswordDialog,
                    enter = scaleIn(initialScale = 0.85f, animationSpec = tween(250)) +
                        fadeIn(animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.85f, animationSpec = tween(200)) +
                        fadeOut(animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        ChangePasswordDialog(
                            onDismiss = { onEvent(ProfileEvent.OnChangePasswordDismiss) },
                            onSubmit = { old, new, confirm ->
                                onEvent(ProfileEvent.OnChangePasswordSubmit(old, new, confirm))
                            },
                            errorMessage = state.changePasswordError,
                            isLoading = state.isLoading
                        )
                    }
                }
            }
        }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: ProfileViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        vm.refresh()
        onPauseOrDispose { }
    }

    LaunchedEffect(vm) {
        vm.effects.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onLogout()
                is ProfileEffect.ShowSnackbar -> Unit
            }
        }
    }

    ProfileScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                ProfileEvent.OnUploadClicked -> onUploadClick()
                else -> vm.onEvent(event)
            }
        },
        modifier = modifier
    )
}
