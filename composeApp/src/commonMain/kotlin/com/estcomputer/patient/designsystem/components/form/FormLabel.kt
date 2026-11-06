package com.estcomputer.patient.designsystem.components.form

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.estcomputer.patient.designsystem.theme.PurpleLabel

@Composable
fun FormLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = PurpleLabel,
        fontSize = 11.sp,
        lineHeight = 16.5.sp,
        letterSpacing = 0.55.sp
    )
}
