package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.screens.dashboard.DashboardEmptyReason

@Composable
fun ProcessingEmptyCard(
    reason: DashboardEmptyReason,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isCompact = maxWidth < 360.dp
        val title: String
        val message: String
        val isWarning: Boolean
        when (reason) {
            DashboardEmptyReason.PROCESSING -> {
                title = "Se proceseaz\u0103 analizele"
                message = "Rezultatele tale vor ap\u0103rea aici \u00EEn c\u00E2teva momente..."
                isWarning = false
            }
            DashboardEmptyReason.NO_RESULTS_EXTRACTED -> {
                title = "Nu am g\u0103sit date \u00EEn analiza \u00EEnc\u0103rcat\u0103"
                message = "Fi\u0219ierul a fost procesat, dar nu con\u021Bine rezultate de laborator extrasabile. \u00CEncarc\u0103 o alt\u0103 analiz\u0103."
                isWarning = true
            }
            DashboardEmptyReason.NON_LAB_DOCUMENT -> {
                title = "Document medical procesat"
                message = "Ai \u00EEnc\u0103rcat un document medical care nu con\u021Bine rezultate de laborator (ex: scrisoare medical\u0103, bilet de trimitere)."
                isWarning = true
            }
            else -> return@BoxWithConstraints
        }
        val bgColor = if (isWarning) Color(0xFFFFF7ED) else Color.White
        val titleColor = if (isWarning) Color(0xFF92400E) else Color(0xFF4338CA)
        val messageColor = if (isWarning) Color(0xFFA16207) else Color(0xFF6366F1)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(bgColor, RoundedCornerShape(28.dp))
                .padding(if (isCompact) 18.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (reason == DashboardEmptyReason.PROCESSING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = Color(0xFF6366F1)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = title,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 16.sp else 20.sp,
                lineHeight = if (isCompact) 24.sp else 28.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = messageColor,
                fontSize = if (isCompact) 13.sp else 15.sp,
                lineHeight = if (isCompact) 20.sp else 22.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
