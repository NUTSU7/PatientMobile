package com.semanticsoft.patientmobile.ui.shared.upload.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.shared.upload.ProcessingPollState

@Composable
fun ProcessingBanner(
    state: ProcessingPollState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isActive,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        val bgColor = when {
            state.isTimedOut -> Color(0xFFFEF2F2)
            state.isSlowWarning -> Color(0xFFFFF7ED)
            else -> Color(0xFFEEF2FF)
        }
        val textColor = when {
            state.isTimedOut -> Color(0xFFB91C1C)
            state.isSlowWarning -> Color(0xFFEA580C)
            else -> Color(0xFF4338CA)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = textColor
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = state.message,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
