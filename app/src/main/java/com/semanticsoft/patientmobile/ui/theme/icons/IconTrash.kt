package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val IconTrash: ImageVector
    get() {
        if (_IconTrash != null) return _IconTrash!!
        _IconTrash = ImageVector.Builder(
            name = "IconTrash",
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
                addPathData("M3,6L21,6")
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M8,6L8,4A1,1 0 0,1 9,3H15A1,1 0 0,1 16,4L16,6")
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M19,6L19,20A2,2 0 0,1 17,22H7A2,2 0 0,1 5,20L5,6")
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(10f, 11f)
                lineTo(10f, 17f)
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(14f, 11f)
                lineTo(14f, 17f)
            }
        }.build()
        return _IconTrash!!
    }

private var _IconTrash: ImageVector? = null
