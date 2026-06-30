package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.components.ErrorDialog
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components.DeleteDocumentDialog
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components.RenameDocumentDialog
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components.SortBottomSheet
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components.UploadedAnalysisItem
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.icons.CloseIcon
import com.semanticsoft.patientmobile.ui.theme.icons.IconTrash
import com.semanticsoft.patientmobile.ui.theme.icons.SortIcon

@Composable
fun UploadedAnalysesScreen(
    navigateToExplanation: (String) -> Unit = { },
    viewModel: UploadedAnalysesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSortSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LifecycleResumeEffect(Unit) {
        viewModel.refreshDocuments()
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshErrors.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    if (state.isSelectionMode) {
        BackHandler(onBack = { viewModel.exitSelectionMode() })
    }

    val showBlockingError = state.errorMessage != null && state.documents.isEmpty()

    // ── Root Box: all overlays are true siblings ──────────────────────────
    Box(modifier = modifier.fillMaxSize()) {

        // ── Main content ──────────────────────────────────────────────────
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val spacing = dashboardSpacing(maxWidth.value)
            val horizontalPadding = spacing.horizontalPadding

            Crossfade(
                targetState = state.isLoading && state.allDocuments.isEmpty(),
                animationSpec = tween(300),
                label = "AnalysesTransition"
            ) { loading ->
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(message = "Se \u00EEncarc\u0103...")
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = AppBackground
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // ── Header Row ─────────────────────────────
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = horizontalPadding,
                                            top = spacing.sectionGap,
                                            end = horizontalPadding,
                                            bottom = spacing.listItemGap
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White, RoundedCornerShape(999.dp))
                                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(999.dp))
                                            .clickable { showSortSheet = true }
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = SortIcon,
                                                contentDescription = null,
                                                tint = Gray500,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Sorteaz\u0103",
                                                color = Gray900,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    if (state.isSelectionMode) {
                                        Spacer(modifier = Modifier.weight(1f))

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    AttentionHigh.copy(alpha = 0.1f),
                                                    RoundedCornerShape(999.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    AttentionHigh.copy(alpha = 0.3f),
                                                    RoundedCornerShape(999.dp)
                                                )
                                                .clickable {
                                                    viewModel.onShowBulkDeleteDialog(
                                                        state.selectedDocumentIds
                                                    )
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = IconTrash,
                                                    contentDescription = null,
                                                    tint = AttentionHigh,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "\u0218terge",
                                                    color = AttentionHigh,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    Color.White,
                                                    RoundedCornerShape(999.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    Color(0xFFE5E7EB),
                                                    RoundedCornerShape(999.dp)
                                                )
                                                .clickable { viewModel.exitSelectionMode() }
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = CloseIcon,
                                                    contentDescription = null,
                                                    tint = Gray500,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "Anuleaz\u0103",
                                                    color = Gray900,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // ── Document list ──────────────────────────
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        start = horizontalPadding,
                                        top = 0.dp,
                                        end = horizontalPadding,
                                        bottom = spacing.bottomSpacer
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(
                                        spacing.listItemGap
                                    ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    items(state.documents, key = { it.id }) { document ->
                                        UploadedAnalysisItem(
                                            document = document,
                                            isSelectionMode = state.isSelectionMode,
                                            isSelected = state.selectedDocumentIds.contains(
                                                document.id
                                            ),
                                            onExplainClick = navigateToExplanation,
                                            onLongPress = { viewModel.enterSelectionMode(it) },
                                            onToggleSelection = {
                                                viewModel.toggleDocumentSelection(it)
                                            },
                                            onRenameClick = { viewModel.onShowRenameDialog(it) },
                                            onDeleteClick = { viewModel.onShowDeleteDialog(it) },
                                            modifier = Modifier.widthIn(max = 1152.dp)
                                        )
                                    }

                                    if (!state.isLoading && state.documents.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .widthIn(max = 1152.dp)
                                                    .height(200.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Nu exist\u0103 analize \u00EEnc\u0103rcate.",
                                                    color = Gray500,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(16.dp)
                            ) { data: SnackbarData ->
                                Snackbar(data)
                            }
                        }
                    }
                }
            }
        }

        // ── Blocking error ─────────────────────────────────────────────────
        if (showBlockingError) {
            state.errorMessage?.let { message ->
                ErrorDialog(
                    message = message,
                    title = "Analize \u00EEncarc\u0103te",
                    onDismiss = { },
                    onRetry = { viewModel.refreshDocuments() }
                )
            }
        }

        // ── Sort bottom sheet ──────────────────────────────────────────────
        if (showSortSheet) {
            SortBottomSheet(
                currentCriteria = state.sortCriteria,
                currentOrder = state.sortOrder,
                onCriteriaSelected = viewModel::onSortCriteriaSelected,
                onOrderSelected = viewModel::onSortOrderSelected,
                onDismissRequest = { showSortSheet = false }
            )
        }

        // ── Rename dialog overlay ──────────────────────────────────────────
        AnimatedVisibility(
            visible = state.showRenameDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onDismissRenameDialog()
                        }
                    )
                    .padding(AppDimens.paddingDefault),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showRenameDialog,
                    enter = scaleIn(initialScale = 0.9f, animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        (state.dialogState as? DialogState.Rename)?.let { renameRef ->
                            val doc = state.allDocuments.find { it.id == renameRef.documentId }
                            if (doc != null) {
                                RenameDocumentDialog(
                                    currentName = doc.originalFileName,
                                    errorText = renameRef.error,
                                    onDismiss = {
                                        focusManager.clearFocus()
                                        viewModel.onDismissRenameDialog()
                                    },
                                    onSave = { newName ->
                                        focusManager.clearFocus()
                                        viewModel.onRenameDocument(
                                            renameRef.documentId,
                                            newName
                                        )
                                    }
                                )
                            } else {
                                LaunchedEffect(renameRef.documentId) {
                                    viewModel.onDismissRenameDialog()
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Single delete dialog overlay ───────────────────────────────────
        AnimatedVisibility(
            visible = state.showDeleteDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onDismissDeleteDialog()
                        }
                    )
                    .padding(AppDimens.paddingDefault),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showDeleteDialog,
                    enter = scaleIn(initialScale = 0.9f, animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        (state.dialogState as? DialogState.SingleDelete)?.let { deleteRef ->
                            val doc = state.allDocuments.find { it.id == deleteRef.documentId }
                            if (doc != null) {
                                DeleteDocumentDialog(
                                    documentName = doc.originalFileName,
                                    isLoading = state.isDeleting,
                                    onDismiss = {
                                        focusManager.clearFocus()
                                        viewModel.onDismissDeleteDialog()
                                    },
                                    onConfirm = {
                                        focusManager.clearFocus()
                                        viewModel.onDeleteDocument(deleteRef.documentId)
                                    }
                                )
                            } else {
                                LaunchedEffect(deleteRef.documentId) {
                                    viewModel.onDismissDeleteDialog()
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Bulk delete dialog overlay ─────────────────────────────────────
        AnimatedVisibility(
            visible = state.showBulkDeleteDialog,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onDismissBulkDeleteDialog()
                        }
                    )
                    .padding(AppDimens.paddingDefault),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = state.showBulkDeleteDialog,
                    enter = scaleIn(initialScale = 0.9f, animationSpec = tween(250)),
                    exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        (state.dialogState as? DialogState.BulkDelete)?.let { bulkRef ->
                            DeleteDocumentDialog(
                                documentName = "",
                                bulkCount = bulkRef.documentIds.size,
                                isLoading = state.isDeleting,
                                onDismiss = {
                                    focusManager.clearFocus()
                                    viewModel.onDismissBulkDeleteDialog()
                                },
                                onConfirm = {
                                    focusManager.clearFocus()
                                    viewModel.onBulkDelete()
                                }
                            )
                        }
                    }
                }
            }
        }

    // ── End root Box ──────────────────────────────────────────────────────
    }
}
