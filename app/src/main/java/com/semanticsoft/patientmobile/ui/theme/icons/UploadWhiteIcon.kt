package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val UploadWhiteIcon: ImageVector
    get() {
        if (_UploadWhiteIcon != null) return _UploadWhiteIcon!!
        _UploadWhiteIcon = ImageVector.Builder(
            name = "UploadWhiteIcon",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(10f, 10.833f)
                lineTo(10f, 17.5f)
            }
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M3.333,12.416C2.714,11.783 2.247,11.018 1.968,10.178C1.688,9.338 1.603,8.446 1.719,7.569C1.836,6.691 2.15,5.852 2.639,5.114C3.128,4.376 3.778,3.759 4.541,3.31C5.304,2.86 6.159,2.59 7.041,2.52C7.923,2.45 8.81,2.582 9.634,2.906C10.458,3.229 11.197,3.736 11.796,4.388C12.395,5.039 12.838,5.819 13.092,6.667H14.583C15.388,6.667 16.171,6.925 16.818,7.405C17.464,7.884 17.939,8.558 18.172,9.328C18.406,10.098 18.386,10.923 18.114,11.68C17.843,12.438 17.336,13.088 16.667,13.535")
            }
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M6.667,14.167L10,10.833L13.333,14.167")
            }
        }.build()
        return _UploadWhiteIcon!!
    }

private var _UploadWhiteIcon: ImageVector? = null
