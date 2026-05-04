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
val SortIcon: ImageVector
    get() {
        if (_SortIcon != null) return _SortIcon!!
        _SortIcon = ImageVector.Builder(
            name = "SortIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF6B7280)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 7f)
                lineTo(20f, 7f)
                addPathData("M6,7A2,2 0 1,1 10,7A2,2 0 1,1 6,7Z")
                moveTo(4f, 12f)
                lineTo(20f, 12f)
                addPathData("M14,12A2,2 0 1,1 18,12A2,2 0 1,1 14,12Z")
                moveTo(4f, 17f)
                lineTo(20f, 17f)
                addPathData("M10,17A2,2 0 1,1 14,17A2,2 0 1,1 10,17Z")
            }
        }.build()
        return _SortIcon!!
    }

private var _SortIcon: ImageVector? = null
