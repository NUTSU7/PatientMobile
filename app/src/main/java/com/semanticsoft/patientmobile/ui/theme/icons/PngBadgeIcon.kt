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
val PngBadgeIcon: ImageVector
    get() {
        if (_PngBadgeIcon != null) return _PngBadgeIcon!!
        _PngBadgeIcon = ImageVector.Builder(
            name = "PngBadgeIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF10B981)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M20,2H8C6.9,2 6,2.9 6,4V16C6,17.1 6.9,18 8,18H20C21.1,18 22,17.1 22,16V4C22,2.9 21.1,2 20,2Z")
                addPathData("M4,6H2V20C2,21.1 2.9,22 4,22H18V20H4V6Z")
            }
            path(
                stroke = SolidColor(Color(0xFF10B981)),
                strokeLineWidth = 1.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11f, 10f)
                lineTo(13f, 10f)
                addPathData("C14.3,10 15,11 15,12C15,13 14.3,14 13,14")
                lineTo(11f, 14f)
                lineTo(11f, 10f)
                moveTo(7f, 10f)
                verticalLineTo(14f)
                moveTo(7f, 10f)
                lineTo(10f, 14f)
                moveTo(10f, 10f)
                verticalLineTo(14f)
                moveTo(16f, 10f)
                addPathData("C17.5,10 18.5,11 18.5,12C18.5,13 17.5,14 16.5,14")
                lineTo(18f, 14f)
            }
        }.build()
        return _PngBadgeIcon!!
    }

private var _PngBadgeIcon: ImageVector? = null
