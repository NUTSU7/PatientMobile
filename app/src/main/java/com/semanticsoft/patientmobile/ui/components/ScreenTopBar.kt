package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ScreenTopBarDimensions(
    val titleSize: TextUnit,
    val titleLineHeight: TextUnit,
    val menuButtonSize: Dp,
    val menuIconSize: Dp,
    val actionButtonSize: Dp,
    val actionIconSize: Dp,
    val primaryActionWidth: Dp,
    val primaryActionTextSize: TextUnit
)

@Composable
fun ScreenTopBar(
    horizontalPadding: Dp,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleContent: @Composable RowScope.(ScreenTopBarDimensions) -> Unit,
    subtitleContent: (@Composable () -> Unit)? = null,
    titleTrailingContent: (@Composable RowScope.(ScreenTopBarDimensions) -> Unit)? = null,
    actionsContent: @Composable RowScope.(ScreenTopBarDimensions) -> Unit,
    centerTitleWhenSubtitleMissing: Boolean = false,
    titleBlockAlignmentWhenSubtitleMissing: Alignment = Alignment.CenterStart
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val subtitleLineHeight = MaterialTheme.typography.bodyMedium.lineHeight
    val subtitleFontSize = MaterialTheme.typography.bodyMedium.fontSize
    val density = LocalDensity.current
    val subtitleHeight = with(density) {
        when {
            subtitleLineHeight != TextUnit.Unspecified && subtitleLineHeight.type == TextUnitType.Sp -> subtitleLineHeight.toDp()
            subtitleFontSize != TextUnit.Unspecified && subtitleFontSize.type == TextUnitType.Sp -> subtitleFontSize.toDp()
            else -> 0.dp
        }
    }
    val maxTitleLineHeight = 31.sp
    val maxActionButtonSize = 36.dp
    val maxTitleRowHeight = maxOf(with(density) { maxTitleLineHeight.toDp() }, maxActionButtonSize)
    val maxPrimaryActionTextSize = 16.sp
    val maxPrimaryActionHeight = with(density) { maxPrimaryActionTextSize.toDp() } + 24.dp
    val maxActionRowHeight = maxOf(maxActionButtonSize, maxPrimaryActionHeight)
    val topBarHeight = topInset + 8.dp + maxTitleRowHeight + subtitleHeight + 8.dp + 8.dp + maxActionRowHeight + 6.dp + 14.dp

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
            val menuButtonSize = when {
                isNarrow -> 34.dp
                isWide -> 38.dp
                else -> 36.dp
            }
            val menuIconSize = when {
                isNarrow -> 22.dp
                isWide -> 25.dp
                else -> 24.dp
            }
            val actionButtonSize = when {
                isNarrow -> 32.dp
                isWide -> 36.dp
                else -> 34.dp
            }
            val actionIconSize = when {
                isNarrow -> 17.dp
                isWide -> 19.dp
                else -> 18.dp
            }
            val primaryActionWidth = when {
                isNarrow -> maxWidth * 0.48f
                isWide -> maxWidth * 0.52f
                else -> maxWidth * 0.5f
            }
            val primaryActionTextSize = when {
                isNarrow -> 14.sp
                isWide -> 16.sp
                else -> 15.sp
            }
            val dimensions = ScreenTopBarDimensions(
                titleSize = titleSize,
                titleLineHeight = titleLineHeight,
                menuButtonSize = menuButtonSize,
                menuIconSize = menuIconSize,
                actionButtonSize = actionButtonSize,
                actionIconSize = actionIconSize,
                primaryActionWidth = primaryActionWidth,
                primaryActionTextSize = primaryActionTextSize
            )
            val titleBlockHeight = with(density) { titleLineHeight.toDp() } + subtitleHeight + 8.dp

            @Composable
            fun TitleRow() {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        titleContent(dimensions)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (titleTrailingContent != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            titleTrailingContent(dimensions)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = topInset + 8.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(dimensions.menuButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Meniu",
                            tint = Color(0xFF4B5563),
                            modifier = Modifier.size(dimensions.menuIconSize)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (subtitleContent == null && centerTitleWhenSubtitleMissing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(titleBlockHeight),
                                contentAlignment = titleBlockAlignmentWhenSubtitleMissing
                            ) {
                                TitleRow()
                            }
                        } else {
                            TitleRow()

                            if (subtitleContent != null) {
                                subtitleContent()
                            } else {
                                Spacer(modifier = Modifier.height(subtitleHeight))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            actionsContent(dimensions)
                        }
                    }
                }
            }
        }
    }
}
