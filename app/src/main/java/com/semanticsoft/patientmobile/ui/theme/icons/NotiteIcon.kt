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
val NotiteIcon: ImageVector
    get() {
        if (_NotiteIcon != null) return _NotiteIcon!!
        _NotiteIcon = ImageVector.Builder(
            name = "NotiteIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Notepad body outline
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 1.5f,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 5f)
                lineTo(15f, 5f)
                lineTo(15f, 19f)
                lineTo(3f, 19f)
                close()
            }
            // Top header line
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 8f)
                lineTo(15f, 8f)
            }
            // Text line 1
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5f, 11f)
                lineTo(13f, 11f)
            }
            // Text line 2
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5f, 14f)
                lineTo(13f, 14f)
            }
            // Text line 3
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5f, 17f)
                lineTo(11f, 17f)
            }
            // Pencil
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(10f, 18f)
                lineTo(20f, 8f)
            }
        }.build()
        return _NotiteIcon!!
    }

private var _NotiteIcon: ImageVector? = null
