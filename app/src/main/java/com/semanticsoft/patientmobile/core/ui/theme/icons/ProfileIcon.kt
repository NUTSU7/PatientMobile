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

val ProfileIcon: ImageVector
    get() {
        if (_ProfileIcon != null) return _ProfileIcon!!
        _ProfileIcon = ImageVector.Builder(
            name = "ProfileIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            val brandGradient = Brush.linearGradient(
                0.0f to Blue900,
                0.5f to Blue500,
                1.0f to Purple500
            )

            group(
                scaleX = 1.25f,
                scaleY = 1.25f,
                translationX = -3f,
                translationY = -3f
            ) {
                path(
                    fill = brandGradient,
                    pathFillType = PathFillType.NonZero
                ) {
                    addPathData(
                        "M12 12c2.21 0 4 -1.79 4 -4s-1.79 -4 -4 -4 -4 1.79 -4 4 1.79 4 4 4z" +
                            "M12 14c-2.67 0 -8 1.34 -8 4v2h16v-2c0 -2.66 -5.33 -4 -8 -4z"
                    )
                }
            }
        }.build()
        return _ProfileIcon!!
    }

private var _ProfileIcon: ImageVector? = null
