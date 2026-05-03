package com.semanticsoft.patientmobile.ui.screens.auth.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AuthLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF111827),
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )
}
