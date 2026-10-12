package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Purple500

val DashboardIcon: ImageVector
    get() {
        if (_DashboardIcon != null) return _DashboardIcon!!
        _DashboardIcon = ImageVector.Builder(
            name = "DashboardIcon",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            val brandGradient = Brush.linearGradient(
                0.0f to Blue900,
                0.5f to Blue500,
                1.0f to Purple500
            )

            group(
                scaleX = 0.8772f,
                scaleY = 0.8772f,
                translationX = 1.5f,
                translationY = 1.5f
            ) {
                path(
                    fill = brandGradient,
                    pathFillType = PathFillType.NonZero
                ) {
                    addPathData(
                        "M9.5 5.7V0H17.1V5.7H9.5ZM0 9.5V0H7.6V9.5H0ZM9.5 17.1V7.6H17.1V17.1H9.5Z" +
                            "M0 17.1V11.4H7.6V17.1H0Z"
                    )
                }
            }
        }.build()
        return _DashboardIcon!!
    }

private var _DashboardIcon: ImageVector? = null
