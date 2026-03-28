package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilePickerButton(
    label: String,
    onClick: () -> Unit,
    validationMessage: String? = null,
    leadingIconRes: Int? = null,
    buttonHeight: Dp = 48.dp,
    cornerRadius: Dp = 8.dp,
    iconSize: Dp = 20.dp,
    textSize: TextUnit = 14.sp,
    textLineHeight: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A52E5)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIconRes?.let { iconRes ->
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = textSize,
                    lineHeight = textLineHeight,
                    fontWeight = FontWeight.Medium
                )
            }
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
