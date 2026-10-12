package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val ArrowLeftIcon: ImageVector
    get() {
        if (_ArrowLeftIcon != null) return _ArrowLeftIcon!!
        _ArrowLeftIcon = ImageVector.Builder(
            name = "ArrowLeftIcon",
            defaultWidth = 10.dp,
            defaultHeight = 10.dp,
            viewportWidth = 9.6f,
            viewportHeight = 9.6f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF6B7280)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M2.3 5.4L5.65 8.75L4.8 9.6L0 4.8L4.8 0L5.65 0.85L2.3 4.2H9.6V5.4H2.3"
                )
            }
        }.build()
        return _ArrowLeftIcon!!
    }

private var _ArrowLeftIcon: ImageVector? = null
