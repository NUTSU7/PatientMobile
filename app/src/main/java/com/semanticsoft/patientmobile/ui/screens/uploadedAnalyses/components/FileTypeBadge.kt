package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.domain.model.PatientDocument

@Composable
fun FileTypeBadge(
    document: PatientDocument,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = fileTypeColors(document.mimeType)

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

internal fun fileTypeColors(mimeType: String): Triple<Color, Color, String> = when {
    mimeType.contains("pdf", ignoreCase = true) -> Triple(
        Color(0xFFFEE2E2),
        Color(0xFFDC2626),
        "PDF"
    )
    mimeType.contains("jpg", ignoreCase = true) || mimeType.contains("jpeg", ignoreCase = true) -> Triple(
        Color(0xFFFFF7ED),
        Color(0xFFEA580C),
        "JPG"
    )
    mimeType.contains("png", ignoreCase = true) -> Triple(
        Color(0xFFECFDF5),
        Color(0xFF059669),
        "PNG"
    )
    else -> {
        val extension = mimeType.substringAfterLast('/').uppercase().ifBlank { "FILE" }
        Triple(
            Color(0xFFF3F4F6),
            Color(0xFF6B7280),
            extension
        )
    }
}
