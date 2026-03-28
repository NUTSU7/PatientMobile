package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FilePickerButton(
    label: String,
    onClick: () -> Unit,
    validationMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A52E5))
        ) {
            Text(text = label, color = Color.White)
        }

        if (!validationMessage.isNullOrBlank()) {
            Text(
                text = validationMessage,
                color = Color(0xFFB91C1C),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
