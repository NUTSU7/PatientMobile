package com.estcomputer.patient.feature.patient.medicalHistory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estcomputer.patient.designsystem.theme.AppDimens
import com.estcomputer.patient.designsystem.theme.AttentionHigh
import com.estcomputer.patient.designsystem.theme.Gray500
import com.estcomputer.patient.designsystem.theme.Gray900
import com.estcomputer.patient.designsystem.theme.GrayLightBorder
import com.estcomputer.patient.designsystem.theme.Purple500
import com.estcomputer.patient.designsystem.theme.icons.TrashIcon
import com.estcomputer.patient.feature.patient.medicalHistory.PersonalNoteItem
import java.time.LocalDate

private val romanianMonths = listOf(
    "ianuarie", "februarie", "martie", "aprilie", "mai", "iunie",
    "iulie", "august", "septembrie", "octombrie", "noiembrie", "decembrie"
)

internal fun formatRomanianDate(date: LocalDate): String {
    val monthName = romanianMonths.getOrElse(date.monthValue - 1) { "" }
    return "${date.dayOfMonth} $monthName ${date.year}"
}

internal fun cleanWhitespace(raw: String): String {
    return raw.trim().replace(Regex("\\s+"), " ")
}

@Composable
fun PersonalNoteCard(
    note: PersonalNoteItem,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit = {},
    analysisSummary: String = "",
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val purpleAccent = note.accentColor ?: Purple500

    val cleanTitle = remember(note.title) {
        cleanWhitespace(note.title)
    }

    val cleanContent = remember(note.content) {
        cleanWhitespace(note.content).ifBlank { "Fără observații clinice." }
    }

    val effectiveAnalysisSummary = remember(analysisSummary, note.analysisSummary) {
        cleanWhitespace(analysisSummary.ifBlank { note.analysisSummary })
    }

    val formattedDate = remember(note.noteDate, note.dateLabel) {
        runCatching {
            formatRomanianDate(note.noteDate)
        }.getOrElse {
            runCatching {
                formatRomanianDate(LocalDate.parse(note.dateLabel))
            }.getOrDefault(note.dateLabel)
        }
    }

    val authorLabel = remember(note.author) {
        val trimmed = cleanWhitespace(note.author)
        if (trimmed.isBlank() || trimmed.equals("Eu", ignoreCase = true)) {
            "Notiță personală"
        } else {
            trimmed
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.personalNoteCardHeight)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, GrayLightBorder, shape)
            .clickable(onClick = onClick)
    ) {
        // Purple accent bar on the left edge
        Box(
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(purpleAccent)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .padding(start = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Title and Associated Analyses on left, Delete button at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = cleanTitle.ifBlank { "Notiță personală" },
                        color = Gray900,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (effectiveAnalysisSummary.isNotEmpty()) {
                        Text(
                            text = "Analize asociate: $effectiveAnalysisSummary",
                            color = Gray500,
                            fontSize = 12.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AttentionHigh.copy(alpha = 0.08f))
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TrashIcon,
                        contentDescription = "Șterge notița",
                        tint = AttentionHigh,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Note Content: clearly readable with clean spacing
            Text(
                text = cleanContent,
                color = Gray900,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = if (effectiveAnalysisSummary.isNotEmpty()) 2 else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Footer: Author on left, Date in the right corner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— $authorLabel",
                    color = Gray500,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (formattedDate.isNotBlank()) {
                    Text(
                        text = formattedDate,
                        color = Gray500,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
