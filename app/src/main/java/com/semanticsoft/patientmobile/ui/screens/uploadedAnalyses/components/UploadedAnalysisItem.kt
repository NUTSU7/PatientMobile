package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.ui.theme.Gray100
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UploadedAnalysisItem(
    document: PatientDocument,
    onExplainClick: (String) -> Unit,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: (String) -> Unit = {},
    onToggleSelection: (String) -> Unit = {},
    onRenameClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelection(document.id)
                    else onExplainClick(document.id)
                },
                onLongClick = {
                    if (!isSelectionMode) onLongPress(document.id)
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Gray100)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FileTypeBadge(document = document)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayFileName(document),
                        color = Gray900,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatDate(document)} · ${formatSize(document.fileSizeBytes)}",
                        color = Gray500,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Crossfade(
                    targetState = isSelectionMode,
                    animationSpec = tween(200)
                ) { inSelection ->
                    if (inSelection) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection(document.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    } else {
                        DocumentItemMenu(
                            onRenameClick = { onRenameClick(document.id) },
                            onDeleteClick = { onDeleteClick(document.id) }
                        )
                    }
                }
            }

        }
    }
}

private fun displayFileName(document: PatientDocument): String {
    val name = document.originalFileName
    val dotIndex = name.lastIndexOf('.')
    return if (dotIndex > 0) name.substring(0, dotIndex) else name
}

private fun formatDate(document: PatientDocument): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return document.uploadedAt.atZone(ZoneId.systemDefault()).format(formatter)
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}
