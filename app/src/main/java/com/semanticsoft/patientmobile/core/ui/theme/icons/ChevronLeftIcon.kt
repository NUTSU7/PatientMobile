package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChevronLeftIcon: ImageVector
    get() {
        if (_ChevronLeftIcon != null) return _ChevronLeftIcon!!
        _ChevronLeftIcon = ImageVector.Builder(
            name = "ChevronLeftIcon",
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
                    "M3.99485 7.98828L0 3.99414L3.99485 0L4.84375 0.848755L1.69781 3.99414" +
                        "L4.84375 7.13953L3.99485 7.98828Z"
                )
            }
        }.build()
        return _ChevronLeftIcon!!
    }

private var _ChevronLeftIcon: ImageVector? = null
