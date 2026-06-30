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
val NoteIcon: ImageVector
    get() {
        if (_NoteIcon != null) return _NoteIcon!!
        _NoteIcon = ImageVector.Builder(
            name = "NoteIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Document body with folded corner
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 2f)
                lineTo(15f, 2f)
                lineTo(20f, 7f)
                lineTo(20f, 22f)
                lineTo(4f, 22f)
                close()
            }
            // Fold crease
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(15f, 2f)
                lineTo(15f, 7f)
                lineTo(20f, 7f)
            }
            // Text lines
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 11f)
                lineTo(17f, 11f)
            }
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 15f)
                lineTo(17f, 15f)
            }
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 19f)
                lineTo(13f, 19f)
            }
        }.build()
        return _NoteIcon!!
    }

private var _NoteIcon: ImageVector? = null
