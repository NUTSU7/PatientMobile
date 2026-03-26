package com.semanticsoft.patientmobile.ui.common

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

private const val FIGMA_SCREEN_WIDTH = 375.2f
private const val FIGMA_CONTENT_WIDTH = 343.2f
private const val FIGMA_SIDE_PADDING = 16f

data class DashboardSpacing(
    val scale: Float,
    val horizontalPadding: Dp,
    val listItemGap: Dp,
    val sectionGap: Dp,
    val markerHeaderGap: Dp,
    val clinicalGridGap: Dp,
    val bottomSpacer: Dp
)

fun dashboardSpacing(screenWidthDp: Float): DashboardSpacing {
    val horizontal = (screenWidthDp * (FIGMA_SIDE_PADDING / FIGMA_SCREEN_WIDTH)).dp.coerceIn(14.dp, 24.dp)
    val contentWidth = max(screenWidthDp - (horizontal.value * 2f), 280f)
    val scale = max(0.88f, min(contentWidth / FIGMA_CONTENT_WIDTH, 1.1f))

    return DashboardSpacing(
        scale = scale,
        horizontalPadding = horizontal,
        listItemGap = (12f * scale).dp,
        sectionGap = (16f * scale).dp,
        markerHeaderGap = (10f * scale).dp,
        clinicalGridGap = (16f * scale).dp,
        bottomSpacer = (8f * scale).dp
    )
}
