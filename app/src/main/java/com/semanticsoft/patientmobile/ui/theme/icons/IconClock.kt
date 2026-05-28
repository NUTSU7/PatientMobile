package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val IconClock: ImageVector
    get() {
        if (_IconClock != null) return _IconClock!!
        _IconClock = ImageVector.Builder(
            name = "IconClock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M12,2A10,10 0 1,0 12,22A10,10 0 1,0 12,2")
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 6f)
                lineTo(12f, 12f)
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 12f)
                lineTo(16f, 12f)
            }
        }.build()
        return _IconClock!!
    }

private var _IconClock: ImageVector? = null
