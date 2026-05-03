package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SidebarPulseIcon: ImageVector
    get() {
        if (_SidebarPulseIcon != null) return _SidebarPulseIcon!!
        _SidebarPulseIcon = ImageVector.Builder(
            name = "SidebarPulseIcon",
            defaultWidth = 36.dp,
            defaultHeight = 36.dp,
            viewportWidth = 36f,
            viewportHeight = 36f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 3f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 18f)
                lineTo(12f, 18f)
                lineTo(15f, 4f)
                lineTo(21f, 32f)
                lineTo(27f, 18f)
                lineTo(33f, 18f)
            }
        }.build()
        return _SidebarPulseIcon!!
    }

private var _SidebarPulseIcon: ImageVector? = null
