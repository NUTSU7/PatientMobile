package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppLogoIcon: ImageVector
    get() {
        if (_AppLogoIcon != null) {
            return _AppLogoIcon!!
        }
        _AppLogoIcon = ImageVector.Builder(
            name = "AppLogoIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF709DF7)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 4f)
                lineTo(13f, 4f)
                curveTo(14.657f, 4f, 16f, 5.343f, 16f, 7f)
                lineTo(16f, 17f)
                curveTo(16f, 18.657f, 14.657f, 20f, 13f, 20f)
                lineTo(3f, 20f)
                curveTo(1.343f, 20f, 0f, 18.657f, 0f, 17f)
                lineTo(0f, 7f)
                curveTo(0f, 5.343f, 1.343f, 4f, 3f, 4f)
                close()
            }

            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(9f, 8f)
                lineTo(7f, 8f)
                lineTo(7f, 11f)
                lineTo(4f, 11f)
                lineTo(4f, 13f)
                lineTo(7f, 13f)
                lineTo(7f, 16f)
                lineTo(9f, 16f)
                lineTo(9f, 13f)
                lineTo(12f, 13f)
                lineTo(12f, 11f)
                lineTo(9f, 11f)
                close()
            }

            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(14.5f, 2f)
                quadTo(14.5f, 5.5f, 18f, 5.5f)
                quadTo(14.5f, 5.5f, 14.5f, 9f)
                quadTo(14.5f, 5.5f, 11f, 5.5f)
                quadTo(14.5f, 5.5f, 14.5f, 2f)
                close()
            }

            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(18f, 1f)
                quadTo(18f, 2.5f, 19.5f, 2.5f)
                quadTo(18f, 2.5f, 18f, 4f)
                quadTo(18f, 2.5f, 16.5f, 2.5f)
                quadTo(18f, 2.5f, 18f, 1f)
                close()
            }
        }.build()
        return _AppLogoIcon!!
    }

private var _AppLogoIcon: ImageVector? = null
