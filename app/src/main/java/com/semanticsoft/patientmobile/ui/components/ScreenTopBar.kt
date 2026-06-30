package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ScreenTopBar(
    titlePrefix: String,
    titleHighlight: String,
    titleSuffix: String = "",
    horizontalPadding: Dp = 16.dp,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current
    val maxTitleLineHeight = 31.sp
    val maxTitleRowHeight = with(density) { maxTitleLineHeight.toDp() }
    val topBarHeight = topInset + 8.dp + maxTitleRowHeight + 8.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .background(Color.White),
        color = Color.White
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isNarrow = maxWidth < 340.dp
            val isWide = maxWidth >= 430.dp
            val titleSize = when {
                isNarrow -> 21.sp
                isWide -> 25.sp
                else -> 24.sp
            }
            val titleLineHeight = when {
                isNarrow -> 27.sp
                isWide -> 31.sp
                else -> 30.sp
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = topInset + 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titlePrefix,
                        color = Color(0xFF111827),
                        fontSize = titleSize,
                        lineHeight = titleLineHeight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = titleHighlight,
                        color = Color(0xFF4F46E5),
                        fontSize = titleSize,
                        lineHeight = titleLineHeight,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (titleSuffix.isNotEmpty()) {
                        Text(
                            text = titleSuffix,
                            color = Color(0xFF111827),
                            fontSize = titleSize,
                            lineHeight = titleLineHeight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    actions()
                }
            }
        }
    }
}
