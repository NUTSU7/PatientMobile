package com.semanticsoft.patientmobile.core.ui.components.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray900

@Composable
fun AuthLabel(text: String) {
    Text(
        text = text,
        color = Gray900,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )
}
