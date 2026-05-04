package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val SelectIcon: ImageVector
    get() {
        if (_SelectIcon != null) return _SelectIcon!!
        _SelectIcon = ImageVector.Builder(
            name = "SelectIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M12,21A9,9 0 1,1 12,3A9,9 0 1,1 12,21Z")
                moveTo(8f, 12f)
                lineTo(11f, 15f)
                lineTo(16f, 9f)
            }
        }.build()
        return _SelectIcon!!
    }

private var _SelectIcon: ImageVector? = null
