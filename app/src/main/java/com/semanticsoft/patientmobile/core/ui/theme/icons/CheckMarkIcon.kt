package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CheckMarkIcon: ImageVector
    get() {
        if (_CheckMarkIcon != null) return _CheckMarkIcon!!
        _CheckMarkIcon = ImageVector.Builder(
            name = "CheckMarkIcon",
            defaultWidth = 6.78.dp,
            defaultHeight = 5.dp,
            viewportWidth = 6.77734f,
            viewportHeight = 5f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M2.36999 5L0 2.62994L0.592498 2.03742L2.36999 3.81497L6.18485 0L6.77734 0.592515L2.36999 5Z"
                )
            }
        }.build()
        return _CheckMarkIcon!!
    }

private var _CheckMarkIcon: ImageVector? = null
