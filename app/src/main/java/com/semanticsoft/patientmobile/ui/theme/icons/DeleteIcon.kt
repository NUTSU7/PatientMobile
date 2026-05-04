package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DeleteIcon: ImageVector
    get() {
        if (_DeleteIcon != null) return _DeleteIcon!!
        _DeleteIcon = ImageVector.Builder(
            name = "DeleteIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFDC2626)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6f, 19f)
                addPathData("C6,20.1 6.9,21 8,21H16C17.1,21 18,20.1 18,19V7H6V19Z")
                moveTo(19f, 4f)
                horizontalLineTo(15.5f)
                lineTo(14.5f, 3f)
                horizontalLineTo(9.5f)
                lineTo(8.5f, 4f)
                horizontalLineTo(5f)
                verticalLineTo(6f)
                horizontalLineTo(19f)
                verticalLineTo(4f)
                close()
            }
        }.build()
        return _DeleteIcon!!
    }

private var _DeleteIcon: ImageVector? = null
