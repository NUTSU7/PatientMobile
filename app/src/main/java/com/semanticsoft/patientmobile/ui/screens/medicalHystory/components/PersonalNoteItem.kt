package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.NoteSeverity
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.PersonalNoteItem
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen
import com.semanticsoft.patientmobile.ui.theme.icons.IconTrash

@Composable
fun PersonalNoteCard(note: PersonalNoteItem, onDeleteClick: () -> Unit, onClick: () -> Unit = {}) {
    val accentColor = note.accentColor ?: when (note.severity) {
        NoteSeverity.GOOD -> SuccessGreen
        NoteSeverity.OK -> Indigo600
        NoteSeverity.BAD -> AttentionHigh
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Gray50)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
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
                    text = note.title.uppercase(),
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = note.content,
                    color = Gray900,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = "\u2014 ${note.author}",
                    color = Gray500,
                    fontSize = 11.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AttentionHigh.copy(alpha = 0.08f))
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconTrash,
                        contentDescription = "\u0218terge",
                        tint = AttentionHigh,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = note.dateLabel,
                    color = Gray500,
                    fontSize = 11.sp
                )
            }
        }
    }
}
