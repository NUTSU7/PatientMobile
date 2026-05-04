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
val SparkleIcon: ImageVector
    get() {
        if (_SparkleIcon != null) return _SparkleIcon!!
        _SparkleIcon = ImageVector.Builder(
            name = "SparkleIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M12,2L14.5,9.5L22,12L14.5,14.5L12,22L9.5,14.5L2,12L9.5,9.5Z")
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M19,1.5L20,2.5L21.5,4L20.5,5L19,6.5L18,5.5L16.5,4L17.5,3Z")
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineWidth = 0f,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M4,17.5L5,18.5L6.5,20L5.5,21L4,22.5L3,21.5L1.5,20L2.5,19Z")
            }
        }.build()
        return _SparkleIcon!!
    }

private var _SparkleIcon: ImageVector? = null
