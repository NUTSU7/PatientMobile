package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val IconChart: ImageVector
    get() {
        if (_IconChart != null) return _IconChart!!
        _IconChart = ImageVector.Builder(
            name = "IconChart",
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
                moveTo(4f, 20f)
                lineTo(4f, 14f)
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(10f, 20f)
                lineTo(10f, 6f)
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(16f, 20f)
                lineTo(16f, 10f)
            }
        }.build()
        return _IconChart!!
    }

private var _IconChart: ImageVector? = null
