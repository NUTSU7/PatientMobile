package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChevronRightIcon: ImageVector
    get() {
        if (_ChevronRightIcon != null) return _ChevronRightIcon!!
        _ChevronRightIcon = ImageVector.Builder(
            name = "ChevronRightIcon",
            defaultWidth = 5.dp,
            defaultHeight = 8.dp,
            viewportWidth = 4.84375f,
            viewportHeight = 7.98828f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M3.14594 3.99414L0 0.848755L0.848905 0L4.84375 3.99414L0.848905 7.98828" +
                        "L0 7.13953L3.14594 3.99414Z"
                )
            }
        }.build()
        return _ChevronRightIcon!!
    }

private var _ChevronRightIcon: ImageVector? = null
