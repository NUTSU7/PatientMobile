package com.semanticsoft.patientmobile.ui.shared.upload

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.shared.upload.components.UploadFileDialogContent

@Composable
fun UploadFileScreen(
    visible: Boolean,
    state: UploadFileUiState,
    viewModel: UploadFileViewModel,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onFilePickerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.9f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(220)),
            exit = scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(220)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
            ) {
                BoxWithConstraints {
                    val maxDialogWidth = minOf(maxWidth - 32.dp, 360.dp)
                    UploadFileDialogContent(
                        state = state,
                        viewModel = viewModel,
                        onDismiss = onDismiss,
                        onCameraClick = onCameraClick,
                        onGalleryClick = onGalleryClick,
                        onFilePickerClick = onFilePickerClick,
                        modifier = Modifier.widthIn(max = maxDialogWidth)
                    )
                }
            }
        }
    }
}