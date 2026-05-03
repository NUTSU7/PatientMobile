package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val GoogleDriveIcon: ImageVector
    get() {
        if (_GoogleDriveIcon != null) return _GoogleDriveIcon!!
        _GoogleDriveIcon = ImageVector.Builder(
            name = "GoogleDriveIcon",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF0F9D58)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(14.667f, 8f)
                lineTo(1.333f, 8f)
            }
            path(
                stroke = SolidColor(Color(0xFF0F9D58)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M3.633,3.407L1.333,8L1.333,12C1.333,12.354 1.474,12.693 1.724,12.943C1.974,13.193 2.313,13.334 2.667,13.334L13.333,13.334C13.687,13.334 14.026,13.193 14.276,12.943C14.526,12.693 14.667,12.354 14.667,12L14.667,8L12.367,3.407C12.256,3.185 12.086,2.998 11.875,2.867C11.665,2.736 11.421,2.667 11.173,2.667L4.827,2.667C4.579,2.667 4.336,2.736 4.125,2.867C3.914,2.998 3.744,3.185 3.633,3.407Z")
            }
            path(
                stroke = SolidColor(Color(0xFF0F9D58)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 10.667f)
                lineTo(4.007f, 10.667f)
            }
            path(
                stroke = SolidColor(Color(0xFF0F9D58)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6.667f, 10.667f)
                lineTo(6.673f, 10.667f)
            }
        }.build()
        return _GoogleDriveIcon!!
    }

private var _GoogleDriveIcon: ImageVector? = null
