package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val WarningIcon: ImageVector
    get() {
        if (_WarningIcon != null) return _WarningIcon!!
        _WarningIcon = ImageVector.Builder(
            name = "WarningIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFB91C1C)),
                strokeLineWidth = 1.5f,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Miter,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2f)
                lineTo(22f, 21f)
                lineTo(2f, 21f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFFB91C1C)),
                strokeLineWidth = 1.5f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 8f)
                lineTo(12f, 13f)
            }
            path(
                fill = SolidColor(Color(0xFFB91C1C)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 17f)
                curveTo(12.55f, 17f, 13f, 17.45f, 13f, 18f)
                curveTo(13f, 18.55f, 12.55f, 19f, 12f, 19f)
                curveTo(11.45f, 19f, 11f, 18.55f, 11f, 18f)
                curveTo(11f, 17.45f, 11.45f, 17f, 12f, 17f)
                close()
            }
        }.build()
        return _WarningIcon!!
    }

private var _WarningIcon: ImageVector? = null
