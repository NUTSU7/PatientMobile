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

/**
 * Main dialog content matching Figma design 1-2226
 */
@Composable
private fun UploadFileDialogContent(
    state: UploadFileUiState,
    viewModel: UploadFileViewModel,
    onDismiss: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape((16f * scale).dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding((24f * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with centered title and close button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((36f * scale).dp)
                    .padding(bottom = (12f * scale).dp)
            ) {
                // Centered title
                Text(
                    text = "Încarcă analize",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = (20.4f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (32f * scale).sp
                    ),
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )

                // Close button on the right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size((36f * scale).dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        modifier = Modifier.size((20f * scale).dp),
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            // Subtitle
            Text(
                text = "Adaugă rezultatele tale de laborator",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (13.6f * scale).sp,
                    lineHeight = (24f * scale).sp
                ),
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = (20f * scale).dp),
                textAlign = TextAlign.Center
            )

            // Main drag-and-drop area
            DragDropArea(
                scale = scale,
                onClick = { viewModel.uploadFile(UploadSource.FILE_PICKER) }
            )

            Spacer(modifier = Modifier.height((16f * scale).dp))

            // "Încărcare fișier" button
            FilePickerButton(
                label = "Încărcare fișier",
                validationMessage = state.errorMessage,
                leadingIconRes = R.drawable.ic_upload_white,
                buttonHeight = (47.2f * scale).dp,
                cornerRadius = (8f * scale).dp,
                iconSize = (16f * scale).dp,
                textSize = (11.9f * scale).sp,
                textLineHeight = (20f * scale).sp,
                onClick = { viewModel.uploadFile(UploadSource.FILE_PICKER) }
            )

            Spacer(modifier = Modifier.height((12f * scale).dp))

            // "Google Drive" button
            ExternalSourceButton(
                label = "Google Drive",
                icon = R.drawable.ic_google_drive,
                scale = scale,
                onClick = { viewModel.uploadFile(UploadSource.GOOGLE_DRIVE) }
            )

            Spacer(modifier = Modifier.height((12f * scale).dp))

            // "Dropbox" button (text only, no icon)
            DropboxButton(
                scale = scale,
                onClick = { viewModel.uploadFile(UploadSource.DROPBOX) }
            )

            if (state.selectedFileName != null) {
                Spacer(modifier = Modifier.height((12f * scale).dp))
                Text(
                    text = "Fișier selectat: ${state.selectedFileName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (11.9f * scale).sp,
                        lineHeight = (20f * scale).sp
                    ),
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center
                )
            }

            if (state.isUploading) {
                Spacer(modifier = Modifier.height((8f * scale).dp))
                LoadingIndicator(message = "Upload în progres: ${(state.uploadProgress * 100).toInt()}%")
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height((8f * scale).dp))
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (11.9f * scale).sp,
                        lineHeight = (20f * scale).sp
                    ),
                    color = Color(0xFFB91C1C),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height((16f * scale).dp))

            // Footer info
            Text(
                text = "Poți încărca: PDF, JPG, JPEG, PNG.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (10.2f * scale).sp,
                    lineHeight = (16f * scale).sp
                ),
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height((4f * scale).dp))

            Text(
                text = "Max 10 MB per fișier.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (10.2f * scale).sp,
                    lineHeight = (16f * scale).sp
                ),
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Drag and drop area - main upload interaction zone
 */
@Composable
private fun DragDropArea(
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((179.2f * scale).dp)
            .border(
                width = (1f * scale).dp,
                color = Color(0xFFD1D5DB),
                shape = RoundedCornerShape((12f * scale).dp)
            )
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape((12f * scale).dp)
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Upload icon in rounded background
            Box(
                modifier = Modifier
                    .size((48f * scale).dp)
                    .background(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape((9999f * scale).dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cloud_download),
                    contentDescription = "Upload",
                    modifier = Modifier.size((24f * scale).dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height((12f * scale).dp))

            // Main instruction text
            Text(
                text = "Trage aici PDF-uri/imagini",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (13.6f * scale).sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = (24f * scale).sp
                ),
                color = Color(0xFF374151),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height((4f * scale).dp))

            // Secondary instruction text
            Text(
                text = "sau click pentru a naviga",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (11.9f * scale).sp,
                    lineHeight = (20f * scale).sp
                ),
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Primary upload button (blue/indigo) with icon
 */
@Composable
private fun UploadButton(
    label: String,
    icon: Int,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height((48f * scale).dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5A52E5),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape((8f * scale).dp),
        contentPadding = androidx.compose.material3.ButtonDefaults.ContentPadding
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size((20f * scale).dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.size((8f * scale).dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = (13.6f * scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/**
 * External source button (Google Drive)
 */
@Composable
private fun ExternalSourceButton(
    label: String,
    icon: Int,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height((41.6f * scale).dp)
            .border(
                width = (1f * scale).dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape((8f * scale).dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape((8f * scale).dp)
            )
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size((16f * scale).dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.size((8f * scale).dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (11.9f * scale).sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (20f * scale).sp
            ),
            color = Color(0xFF374151)
        )
    }
}

/**
 * Dropbox button (text only, no icon in Figma)
 */
@Composable
private fun DropboxButton(
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height((41.6f * scale).dp)
            .border(
                width = (1f * scale).dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape((8f * scale).dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape((8f * scale).dp)
            )
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Dropbox",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (11.9f * scale).sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (20f * scale).sp
            ),
            color = Color(0xFF374151)
        )
    }
}


