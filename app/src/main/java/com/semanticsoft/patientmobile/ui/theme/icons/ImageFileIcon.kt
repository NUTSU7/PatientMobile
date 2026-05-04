package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ImageFileIcon: ImageVector
    get() {
        if (_ImageFileIcon != null) return _ImageFileIcon!!
        _ImageFileIcon = ImageVector.Builder(
            name = "ImageFileIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF8B5CF6)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(19f, 5f)
                verticalLineTo(19f)
                horizontalLineTo(5f)
                verticalLineTo(5f)
                horizontalLineTo(19f)
                moveTo(19f, 3f)
                horizontalLineTo(5f)
                addPathData("C3.9,3 3,3.9 3,5V19C3,20.1 3.9,21 5,21H19C20.1,21 21,20.1 21,19V5C21,3.9 20.1,3 19,3Z")
                moveTo(14.14f, 11.86f)
                lineTo(11.14f, 15.73f)
                lineTo(9f, 13.14f)
                lineTo(6f, 17f)
                horizontalLineTo(18f)
                lineTo(14.14f, 11.86f)
                close()
            }
        }.build()
        return _ImageFileIcon!!
    }

private var _ImageFileIcon: ImageVector? = null
