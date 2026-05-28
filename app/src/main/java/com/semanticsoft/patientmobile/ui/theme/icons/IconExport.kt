package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val IconExport: ImageVector
    get() {
        if (_IconExport != null) return _IconExport!!
        _IconExport = ImageVector.Builder(
            name = "IconExport",
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
                addPathData("M4,15L4,19A2,2 0 0,0 6,21H18A2,2 0 0,0 20,19L20,15")
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 16f)
                lineTo(12f, 3f)
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(8f, 7f)
                lineTo(12f, 3f)
                lineTo(16f, 7f)
            }
        }.build()
        return _IconExport!!
    }

private var _IconExport: ImageVector? = null
