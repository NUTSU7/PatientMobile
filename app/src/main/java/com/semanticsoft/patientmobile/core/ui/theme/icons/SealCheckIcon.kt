package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SealCheckIcon: ImageVector
    get() {
        if (_SealCheckIcon != null) return _SealCheckIcon!!
        _SealCheckIcon = ImageVector.Builder(
            name = "SealCheckIcon",
            defaultWidth = 18.32.dp,
            defaultHeight = 17.5.dp,
            viewportWidth = 18.3203f,
            viewportHeight = 17.5f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF059669)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M6.32884 17.5L4.74664 14.8333L1.74876 14.1667L2.04022 11.0833L0 8.75L2.04022 6.41667" +
                        "L1.74876 3.33333L4.74664 2.66667L6.32884 0L9.16018 1.20833L11.9915 0L13.5737 2.66667" +
                        "L16.5716 3.33333L16.2802 6.41667L18.3203 8.75L16.2802 11.0833L16.5716 14.1667" +
                        "L13.5737 14.8333L11.9915 17.5L9.16018 16.2917L6.32884 17.5Z" +
                        "M7.03668 15.375L9.16018 14.4583L11.3253 15.375L12.4911 13.375L14.7812 12.8333" +
                        "L14.573 10.5L16.1136 8.75L14.573 6.95833L14.7812 4.625L12.4911 4.125L11.2837 2.125" +
                        "L9.16018 3.04167L6.99504 2.125L5.8292 4.125L3.53916 4.625L3.74734 6.95833" +
                        "L2.20677 8.75L3.74734 10.5L3.53916 12.875L5.8292 13.375L7.03668 15.375Z" +
                        "M8.2858 11.7083L12.9908 7L11.8249 5.79167L8.2858 9.33333L6.4954 7.58333" +
                        "L5.32955 8.75L8.2858 11.7083Z"
                )
            }
        }.build()
        return _SealCheckIcon!!
    }

private var _SealCheckIcon: ImageVector? = null
