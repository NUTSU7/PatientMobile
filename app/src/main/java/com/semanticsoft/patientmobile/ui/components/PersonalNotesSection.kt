package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryUiState
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.NoteSeverity
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.PersonalNoteItem
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen
import com.semanticsoft.patientmobile.ui.theme.TextSecondary
import androidx.compose.ui.graphics.Brush

@Composable
fun PersonalNotesSection(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onAttachFileClick: () -> Unit,
    onAddNoteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
        ) {
            FileTextIcon()
            Text(
                text = "Notițe personale",
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gray50, RoundedCornerShape(18.dp))
                    .padding(spacing.sectionGap),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nu există notițe personale adăugate.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)
            ) {
                state.notes.forEach { note ->
                    PersonalNoteCard(note = note)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
            FilePickerButton(
                label = "Atașează fișier",
                onClick = onAttachFileClick,
                buttonHeight = 48.dp,
                cornerRadius = 16.dp,
                textSize = 14.sp,
                dashed = true,
                dashedColor = Gray500,
                contentColor = Gray900,
                containerColor = Color.Transparent
            )
            FilePickerButton(
                label = "+ Adaugă notiță personală",
                onClick = onAddNoteClick,
                buttonHeight = 48.dp,
                cornerRadius = 16.dp,
                textSize = 14.sp,
                backgroundBrush = Brush.horizontalGradient(listOf(Indigo600, Purple500))
            )
        }
    }
}

@Composable
fun PersonalNoteCard(note: PersonalNoteItem) {
    val accentColor = when (note.severity) {
        NoteSeverity.GOOD -> SuccessGreen
        NoteSeverity.OK -> Indigo600
        NoteSeverity.BAD -> AttentionHigh
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = note.title,
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = note.content,
                    color = Gray900,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "— ${note.author}",
                        color = Gray500,
                        fontSize = 11.sp
                    )
                    Text(
                        text = note.dateLabel,
                        color = Gray500,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FileTextIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.8f
        val iconColor = Indigo600
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = iconColor,
            topLeft = Offset(w * 0.15f, h * 0.1f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(strokeWidth)
        )

        val fold = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.6f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.3f)
            lineTo(w * 0.6f, h * 0.3f)
            close()
        }
        drawPath(path = fold, color = iconColor, style = Stroke(strokeWidth))

        repeat(3) { index ->
            val y = h * (0.42f + index * 0.16f)
            drawLine(
                color = iconColor,
                start = Offset(w * 0.28f, y),
                end = Offset(w * 0.72f, y),
                strokeWidth = strokeWidth * 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}
