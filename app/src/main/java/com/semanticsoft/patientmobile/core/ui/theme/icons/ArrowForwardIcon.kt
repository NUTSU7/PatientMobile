package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ArrowForwardIcon: ImageVector
    get() {
        if (_ArrowForwardIcon != null) return _ArrowForwardIcon!!
        _ArrowForwardIcon = ImageVector.Builder(
            name = "ArrowForwardIcon",
            defaultWidth = 11.99.dp,
            defaultHeight = 11.99.dp,
            viewportWidth = 11.9922f,
            viewportHeight = 11.9922f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M9.12531 6.74561H0V5.24658H9.12531L4.92804 1.04932L5.99609 0L11.9922 5.99609L5.99609 11.9922" +
                        "L4.92804 10.9429L9.12531 6.74561Z"
                )
            }
        }.build()
        return _ArrowForwardIcon!!
    }

private var _ArrowForwardIcon: ImageVector? = null
