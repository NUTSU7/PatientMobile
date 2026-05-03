package com.semanticsoft.patientmobile.ui.screens.uploadFile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.R
import com.semanticsoft.patientmobile.ui.components.FilePickerButton
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.screens.uploadFile.UploadFileUiState
import com.semanticsoft.patientmobile.ui.screens.uploadFile.UploadFileViewModel
import com.semanticsoft.patientmobile.ui.screens.uploadFile.UploadSource
import com.semanticsoft.patientmobile.ui.theme.icons.CloseIcon
import com.semanticsoft.patientmobile.ui.theme.icons.GoogleDriveIcon
import com.semanticsoft.patientmobile.ui.theme.icons.UploadWhiteIcon
import kotlin.math.max
import kotlin.math.min

@Composable
fun UploadFileDialogContent(
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((36f * scale).dp)
                    .padding(bottom = (12f * scale).dp)
            ) {
                Text(
                    text = "\u00CEncarc\u0103 analize",
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

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size((36f * scale).dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Close",
                        modifier = Modifier.size((20f * scale).dp),
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            Text(
                text = "Adaug\u0103 rezultatele tale de laborator",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (13.6f * scale).sp,
                    lineHeight = (24f * scale).sp
                ),
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = (20f * scale).dp),
                textAlign = TextAlign.Center
            )

            DragDropArea(
                scale = scale,
                onClick = { viewModel.uploadFile(UploadSource.FILE_PICKER) }
            )

            Spacer(modifier = Modifier.height((16f * scale).dp))

            FilePickerButton(
                label = "\u00CEnc\u0103rcare fi\u0219ier",
                validationMessage = state.errorMessage,
                leadingIcon = UploadWhiteIcon,
                buttonHeight = (47.2f * scale).dp,
                cornerRadius = (8f * scale).dp,
                iconSize = (16f * scale).dp,
                textSize = (11.9f * scale).sp,
                textLineHeight = (20f * scale).sp,
                onClick = { viewModel.uploadFile(UploadSource.FILE_PICKER) }
            )

            Spacer(modifier = Modifier.height((12f * scale).dp))

            ExternalSourceButton(
                label = "Google Drive",
                icon = GoogleDriveIcon,
                scale = scale,
                onClick = { viewModel.uploadFile(UploadSource.GOOGLE_DRIVE) }
            )

            Spacer(modifier = Modifier.height((12f * scale).dp))

            DropboxButton(
                scale = scale,
                onClick = { viewModel.uploadFile(UploadSource.DROPBOX) }
            )

            if (state.selectedFileName != null) {
                Spacer(modifier = Modifier.height((12f * scale).dp))
                Text(
                    text = "Fi\u0219ier selectat: ${state.selectedFileName}",
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
                LoadingIndicator(message = "Upload \u00EEn progres: ${(state.uploadProgress * 100).toInt()}%")
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

            Text(
                text = "Po\u021Bi \u00EEnc\u0103rca: PDF, JPG, JPEG, PNG.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (10.2f * scale).sp,
                    lineHeight = (16f * scale).sp
                ),
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height((4f * scale).dp))

            Text(
                text = "Max 10 MB per fi\u0219ier.",
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
