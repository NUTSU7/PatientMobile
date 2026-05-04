package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val RefreshIcon: ImageVector
    get() {
        if (_RefreshIcon != null) return _RefreshIcon!!
        _RefreshIcon = ImageVector.Builder(
            name = "RefreshIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M17.65 6.35C16.2 4.9 14.21 4 12 4c -4.42 0 -7.99 3.58 -7.99 8s3.57 8 7.99 8c3.73 0 6.84 -2.55 7.73 -6h -2.08c -0.82 2.33 -3.04 4 -5.65 4c -3.31 0 -6 -2.69 -6 -6s2.69 -6 6 -6c1.66 0 3.14 0.69 4.22 1.78L13 11h7V4l -2.35 2.35z"
                )
            }
        }.build()
        return _RefreshIcon!!
    }

private var _RefreshIcon: ImageVector? = null
