package com.semanticsoft.patientmobile.ui.shared.upload

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.shared.upload.components.UploadFileDialogContent
import kotlin.math.max
import kotlin.math.min

@Composable
fun UploadFileScreen(
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
            .background(Color(0x80000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
        ) {
            val maxDialogWidth = minOf(maxWidth - 32.dp, 360.dp)
            val scale = max(0.86f, min(maxDialogWidth.value / 343.2f, 1.06f))

            UploadFileDialogContent(
                state = state,
                viewModel = viewModel,
                onDismiss = onDismiss,
                onCameraClick = onCameraClick,
                onGalleryClick = onGalleryClick,
                onFilePickerClick = onFilePickerClick,
                scale = scale,
                modifier = Modifier.widthIn(max = maxDialogWidth)
            )
        }
    }
}
