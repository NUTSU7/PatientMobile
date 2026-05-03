package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val UploadIcon: ImageVector
    get() {
        if (_UploadIcon != null) return _UploadIcon!!
        _UploadIcon = ImageVector.Builder(
            name = "UploadIcon",
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
                moveTo(12f, 13f)
                lineTo(12f, 21f)
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M4,14.899C3.257,14.14 2.697,13.222 2.361,12.214C2.025,11.206 1.924,10.135 2.063,9.082C2.203,8.029 2.58,7.022 3.167,6.137C3.754,5.251 4.534,4.511 5.449,3.972C6.364,3.432 7.39,3.109 8.449,3.025C9.508,2.941 10.572,3.099 11.561,3.487C12.549,3.875 13.437,4.483 14.156,5.265C14.875,6.047 15.406,6.982 15.71,8L17.5,8C18.465,7.999 19.405,8.31 20.181,8.885C20.956,9.461 21.526,10.27 21.807,11.194C22.087,12.118 22.063,13.107 21.737,14.016C21.412,14.925 20.803,15.706 20,16.242")
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M8,17L12,13L16,17")
            }
        }.build()
        return _UploadIcon!!
    }

private var _UploadIcon: ImageVector? = null
