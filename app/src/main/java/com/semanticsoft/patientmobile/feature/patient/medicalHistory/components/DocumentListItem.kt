package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import com.semanticsoft.patientmobile.feature.patient.medicalHistory.DocumentReadiness
import com.semanticsoft.patientmobile.core.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600

@Composable
fun DocumentListItem(
    fileName: String,
    uploadStatus: String,
    resultsCount: Int,
    readiness: DocumentReadiness?,
    onClick: () -> Unit,
    onReviewClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = fileName,
                    color = Gray900,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = uploadStatus,
                    color = Gray500,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (readiness == DocumentReadiness.Failed) {
                    Text(
                        text = "Procesare e\u0219uat\u0103",
                        color = AttentionHigh,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            when (readiness) {
                DocumentReadiness.Checking, DocumentReadiness.Pending -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Indigo600
                    )
                }
                DocumentReadiness.Ready -> {
                    if (onReviewClick != null) {
                        IconButton(
                            onClick = onReviewClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Editeaz\u0103 analiza",
                                modifier = Modifier.size(18.dp),
                                tint = Indigo600
                            )
                        }
                    }
                    if (resultsCount > 0) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Analizat",
                            modifier = Modifier.size(20.dp),
                            tint = Indigo600
                        )
                    }
                }
                DocumentReadiness.Failed -> {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = "Procesare e\u0219uat\u0103",
                        modifier = Modifier.size(20.dp),
                        tint = AttentionHigh
                    )
                }
                DocumentReadiness.Unavailable -> {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Indisponibil",
                        modifier = Modifier.size(20.dp),
                        tint = Gray500
                    )
                }
                null -> {}
            }
        }
    }
}
