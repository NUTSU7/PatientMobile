package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.BasicIndicatorsCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.EmptyUploadCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.GeneralMarkersCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.HealthScoreCard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.ResumeAICard
import com.semanticsoft.patientmobile.ui.screens.dashboard.components.DashboardTopSection
import com.semanticsoft.patientmobile.ui.components.ErrorDialog
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.components.ScreenTopBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileEvent
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileScreen
import com.semanticsoft.patientmobile.ui.shared.upload.UploadFileViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.core.content.FileProvider
import java.io.File
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500
import com.semanticsoft.patientmobile.data.model.IndicatorStatus

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onMenuClick: () -> Unit = {},
    onUploadClick: () -> Unit = {}
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val dismissedError = remember { mutableStateOf<String?>(null) }
    var showUploadModal by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var activeStatusFilter by rememberSaveable { mutableStateOf("Toate") }
    var visibleAdditionalCount by rememberSaveable { mutableStateOf(6) }

    LaunchedEffect(activeStatusFilter) {
        visibleAdditionalCount = 6
    }

    val uploadFileViewModel: UploadFileViewModel = hiltViewModel()
    val uploadFileState by uploadFileViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraFile?.let { file ->
                if (file.exists()) {
                    uploadFileViewModel.onFilesSelected(listOf(file.absolutePath))
                }
            }
        }
        pendingCameraFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val paths = uris.mapNotNull { uri -> copyUriToCache(context, uri) }
        if (paths.isNotEmpty()) {
            uploadFileViewModel.onFilesSelected(paths)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val paths = uris.mapNotNull { uri -> copyUriToCache(context, uri) }
        if (paths.isNotEmpty()) {
            uploadFileViewModel.onFilesSelected(paths)
        }
    }

    LaunchedEffect(uploadFileViewModel) {
        uploadFileViewModel.events.collect { event ->
            when (event) {
                is UploadFileEvent.AllFilesUploaded -> {
                    showUploadModal = false
                    snackbarHostState.showSnackbar(
                        message = "Analize \u00EEnc\u0103rcate cu succes.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    val filteredGeneralMarkers = when (activeStatusFilter) {
        "Atenție" -> state.generalMarkerCards.filter { it.status == IndicatorStatus.ATTENTION }
        "La limită" -> state.generalMarkerCards.filter { it.status == IndicatorStatus.BORDERLINE }
        "Normal" -> state.generalMarkerCards.filter { it.status == IndicatorStatus.NORMAL }
        else -> state.generalMarkerCards
    }

    val hasAttention = state.generalMarkerCards.any { it.status == IndicatorStatus.ATTENTION }
    val hasBorderline = state.generalMarkerCards.any { it.status == IndicatorStatus.BORDERLINE }
    val hasNormal = state.generalMarkerCards.any { it.status == IndicatorStatus.NORMAL }
    val visibleStatusFilters = buildList {
        add("Toate")
        if (hasAttention) add("Atenție")
        if (hasBorderline) add("La limită")
        if (hasNormal) add("Normal")
    }

    LaunchedEffect(visibleStatusFilters) {
        if (activeStatusFilter !in visibleStatusFilters) {
            activeStatusFilter = "Toate"
        }
    }

    val visibleGeneralMarkers = filteredGeneralMarkers.take(visibleAdditionalCount)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val frequentCardWidth = when {
            maxWidth < 340.dp -> 218.dp
            maxWidth < 390.dp -> 232.dp
            maxWidth < 430.dp -> 240.dp
            else -> 252.dp
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            if (!state.hasUploadedDocuments) {
                Column(modifier = Modifier.fillMaxSize()) {
                    DashboardTopSection(
                        state = state,
                        horizontalPadding = horizontalPadding,
                        onMenuClick = onMenuClick,
                        onUploadClick = {
                            showUploadModal = true
                            onUploadClick()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                bottom = spacing.sectionGap + bottomInset
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyUploadCard(
                            modifier = Modifier.fillMaxWidth(),
                            onUploadClick = {
                                showUploadModal = true
                                onUploadClick()
                            }
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    DashboardTopSection(
                        state = state,
                        horizontalPadding = horizontalPadding,
                        onMenuClick = onMenuClick,
                        onUploadClick = {
                            showUploadModal = true
                            onUploadClick()
                        }
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            top = spacing.sectionGap,
                            bottom = bottomInset + spacing.bottomSpacer
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
                    ) {
                        item {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                            ) {
                                val stackCards = maxWidth < 660.dp
                                if (stackCards) {
                                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)) {
                                        HealthScoreCard(
                                            score = state.markerSummary.score,
                                            normalCount = state.markerSummary.normal,
                                            borderlineCount = state.markerSummary.borderline,
                                            attentionCount = state.markerSummary.attention,
                                            statusText = "",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        ResumeAICard(
                                            summaryText = state.aiSummary,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sectionGap)) {
                                        HealthScoreCard(
                                            score = state.markerSummary.score,
                                            normalCount = state.markerSummary.normal,
                                            borderlineCount = state.markerSummary.borderline,
                                            attentionCount = state.markerSummary.attention,
                                            statusText = "",
                                            modifier = Modifier.weight(1f)
                                        )
                                        ResumeAICard(
                                            summaryText = state.aiSummary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        if (state.basicIndicators.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Indicatori frecvenți",
                                    color = Color(0xFF111827),
                                    fontSize = 20.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = horizontalPadding)
                                )
                            }

                            item {
                                LazyRow(
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.basicIndicators) { indicator ->
                                        BasicIndicatorsCard(
                                            title = indicator.title,
                                            value = indicator.value,
                                            unit = indicator.unit,
                                            status = indicator.status,
                                            trendDirection = indicator.trendDirection,
                                            trendDelta = indicator.trendDelta,
                                            trendDescription = indicator.trendDescription,
                                            markerPosition = indicator.markerPosition,
                                            segments = indicator.segments,
                                            modifier = Modifier.width(frequentCardWidth)
                                        )
                                    }
                                }
                            }
                        }

                        if (state.generalMarkerCards.isNotEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = horizontalPadding)
                                        .background(Color.White, RoundedCornerShape(24.dp))
                                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
                                ) {
                                    Text(
                                        text = "Indicatori suplimentari",
                                        color = Color(0xFF111827),
                                        fontSize = 20.sp,
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        visibleStatusFilters.forEach { filter ->
                                            val selected = activeStatusFilter == filter
                                            Text(
                                                text = filter,
                                                textAlign = TextAlign.Center,
                                                color = if (selected) Color.White else Color(0xFF4B5563),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(if (selected) Color(0xFF6366F1) else Color.White)
                                                    .border(
                                                        width = if (selected) 0.dp else 1.dp,
                                                        color = if (selected) Color.Transparent else Color(0xFFE5E7EB),
                                                        shape = RoundedCornerShape(999.dp)
                                                    )
                                                    .clickable { activeStatusFilter = filter }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                        visibleGeneralMarkers.forEachIndexed { index, markerCard ->
                                            GeneralMarkersCard(
                                                title = markerCard.title,
                                                category = markerCard.category,
                                                value = markerCard.value,
                                                unit = markerCard.unit,
                                                status = markerCard.status,
                                                normalRange = markerCard.normalRange,
                                                borderlineRange = markerCard.borderlineRange,
                                                attentionRange = markerCard.attentionRange,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            if (index != visibleGeneralMarkers.lastIndex) {
                                                Spacer(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(Color(0xFFF3F4F6))
                                                )
                                            }
                                        }
                                    }

                                    if (filteredGeneralMarkers.size > visibleAdditionalCount) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Arată mai mult",
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(999.dp))
                                                    .background(Color(0xFF6366F1))
                                                    .clickable { visibleAdditionalCount += 6 }
                                                    .padding(horizontal = 24.dp, vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Adaugă mai multe fișiere pentru o analiză mai detaliată.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding, vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                }
            }
        }

        // Upload File Modal
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
                onCameraClick = {
                    val file = File(context.cacheDir, "camera/IMG_${System.currentTimeMillis()}.jpg").apply {
                        parentFile?.mkdirs()
                    }
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    pendingCameraFile = file
                    cameraLauncher.launch(uri)
                },
                onGalleryClick = {
                    galleryLauncher.launch("image/*")
                },
                onFilePickerClick = {
                    filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
                }
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(message = "Se actualizează datele dashboard...")
            }
        }

        state.errorMessage?.takeIf { it != dismissedError.value }?.let { message ->
            ErrorDialog(
                message = message,
                onDismiss = { dismissedError.value = message },
                onRetry = null,
                title = "Dashboard"
            )
        }

        // Snackbar overlay for upload completion
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data: SnackbarData ->
            Snackbar(data)
        }
    }
}

private fun copyUriToCache(context: android.content.Context, uri: Uri): String? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val ext = when {
            mimeType.contains("pdf") -> ".pdf"
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
            mimeType.contains("png") -> ".png"
            else -> ".tmp"
        }
        val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}$ext"
        val outFile = File(context.cacheDir, "uploads/$fileName").apply {
            parentFile?.mkdirs()
        }
        contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        outFile.absolutePath
    } catch (_: Exception) {
        null
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else null
            } else null
        }
    } catch (_: Exception) {
        null
    }
}
