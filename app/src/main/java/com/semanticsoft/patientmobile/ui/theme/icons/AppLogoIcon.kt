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
            // Opaque rounded background box (Replaced the transparent white)
            path(
                fill = SolidColor(Color(0xFF709DF7)), // <-- Solid blue color here!
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 4f)
                lineTo(17f, 4f)
                curveTo(18.657f, 4f, 20f, 5.343f, 20f, 7f)
                lineTo(20f, 17f)
                curveTo(20f, 18.657f, 18.657f, 20f, 17f, 20f)
                lineTo(7f, 20f)
                curveTo(5.343f, 20f, 4f, 18.657f, 4f, 17f)
                lineTo(4f, 7f)
                curveTo(4f, 5.343f, 5.343f, 4f, 7f, 4f)
                close()
            }
            
            // Plus Sign
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13f, 8f)
                lineTo(11f, 8f)
                lineTo(11f, 11f)
                lineTo(8f, 11f)
                lineTo(8f, 13f)
                lineTo(11f, 13f)
                lineTo(11f, 16f)
                lineTo(13f, 16f)
                lineTo(13f, 13f)
                lineTo(16f, 13f)
                lineTo(16f, 11f)
                lineTo(13f, 11f)
                close()
            }
            
            // Main Sparkle (Top Right)
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(18.5f, 2f)
                quadTo(18.5f, 5.5f, 22f, 5.5f)
                quadTo(18.5f, 5.5f, 18.5f, 9f)
                quadTo(18.5f, 5.5f, 15f, 5.5f)
                quadTo(18.5f, 5.5f, 18.5f, 2f)
                close()
            }
            
            // Smaller Secondary Sparkle
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(22f, 1f)
                quadTo(22f, 2.5f, 23.5f, 2.5f)
                quadTo(22f, 2.5f, 22f, 4f)
                quadTo(22f, 2.5f, 20.5f, 2.5f)
                quadTo(22f, 2.5f, 22f, 1f)
                close()
            }
        }.build()
        return _AppLogoIcon!!
    }

private var _AppLogoIcon: ImageVector? = null