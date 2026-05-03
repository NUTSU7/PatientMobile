package com.semanticsoft.patientmobile.ui.screens.uploadFile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.semanticsoft.patientmobile.R
import com.semanticsoft.patientmobile.ui.components.FilePickerButton
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.screens.uploadFile.components.DragDropArea
import com.semanticsoft.patientmobile.ui.screens.uploadFile.components.DropboxButton
import com.semanticsoft.patientmobile.ui.screens.uploadFile.components.ExternalSourceButton
import com.semanticsoft.patientmobile.ui.screens.uploadFile.components.UploadFileDialogContent
import kotlin.math.max
import kotlin.math.min

/**
 * Upload File Modal Screen - appears as a popup over the dashboard
 * Matches Figma design node 1-2226 exactly
 */
@Composable
fun UploadFileScreen(
    state: UploadFileUiState,
    viewModel: UploadFileViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Maintain dark status bar when dialog is open
    SetStatusBar(color = Color.White, darkIcons = true)
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0x80000000))  // Semi-transparent overlay
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val maxDialogWidth = minOf(maxWidth - 32.dp, 360.dp)
            val scale = max(0.86f, min(maxDialogWidth.value / 343.2f, 1.06f))

            // Smooth enter/exit animation
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(initialScale = 0.85f) + fadeIn(),
                exit = scaleOut(targetScale = 0.85f) + fadeOut()
            ) {
                UploadFileDialogContent(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = onDismiss,
                    scale = scale,
                    modifier = Modifier.widthIn(max = maxDialogWidth)
                )
            }
        }
    }
}

