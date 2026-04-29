package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
    containerColor: Color = Color(0xFF5A52E5),
    contentColor: Color = Color.White,
    backgroundBrush: Brush? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 0.dp,
    dashed: Boolean = false,
    dashedColor: Color? = null,
    fillMaxWidth: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .background(
                    brush = backgroundBrush ?: Brush.linearGradient(listOf(containerColor, containerColor)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
                )
                .then(
                    when {
                        dashed -> Modifier.drawWithContent {
                            drawContent()
                            val strokeColor = dashedColor ?: borderColor ?: containerColor
                            val strokeWidthPx = if (borderWidth > Dp.Hairline) borderWidth.toPx() else 1.dp.toPx()
                            drawRoundRect(
                                color = strokeColor,
                                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                                style = Stroke(
                                    width = strokeWidthPx,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                                )
                            )
                        }
                        borderColor != null && borderWidth > Dp.Hairline -> Modifier.border(
                            width = borderWidth,
                            color = borderColor,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
                        )
                        else -> Modifier
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
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
                    color = contentColor,
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
