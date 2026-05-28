package com.semanticsoft.patientmobile.ui.screens.auth.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.theme.Gray900

@Composable
fun AuthLabel(text: String) {
    Text(
        text = text,
        color = Gray900,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )
}
