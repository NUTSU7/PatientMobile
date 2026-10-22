package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ArrowDownIcon: ImageVector
    get() {
        if (_ArrowDownIcon != null) return _ArrowDownIcon!!
        _ArrowDownIcon = ImageVector.Builder(
            name = "ArrowDownIcon",
            defaultWidth = 9.dp,
            defaultHeight = 9.dp,
            viewportWidth = 9.375f,
            viewportHeight = 9.375f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M3.82861 0V6.07046L1.22283 3.46467L0 4.68749L4.68749 9.375L9.375 4.68749" +
                        "L8.15217 3.46467L5.54639 6.07046V0H3.82861Z"
                )
            }
        }.build()
        return _ArrowDownIcon!!
    }

private var _ArrowDownIcon: ImageVector? = null
