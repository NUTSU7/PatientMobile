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
val PaperclipIcon: ImageVector
    get() {
        if (_PaperclipIcon != null) return _PaperclipIcon!!
        _PaperclipIcon = ImageVector.Builder(
            name = "PaperclipIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF4F46E5)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(14.1f, 4.2f)
                arcTo(4f, 4f, 0f, false, true, 19.8f, 9.9f)
                quadTo(15f, 15f, 9.9f, 19.8f)
                arcTo(4f, 4f, 0f, false, true, 4.2f, 14.1f)
                quadTo(9.5f, 9f, 14.1f, 4.2f)
                close()
                moveTo(7f, 17f)
                lineTo(17f, 7f)
            }
        }.build()
        return _PaperclipIcon!!
    }

private var _PaperclipIcon: ImageVector? = null
