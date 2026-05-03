@file:Suppress("unused")

package com.semanticsoft.patientmobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ProgressIndicator(
    message: String,
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color(0xFF5A52E5)
) {
    LoadingIndicator(
        message = message,
        modifier = modifier,
        indicatorColor = indicatorColor
    )
}
