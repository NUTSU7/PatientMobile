package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val ShieldCheckIcon: ImageVector
    get() {
        if (_ShieldCheckIcon != null) return _ShieldCheckIcon!!
        _ShieldCheckIcon = ImageVector.Builder(
            name = "ShieldCheckIcon",
            defaultWidth = 11.7.dp,
            defaultHeight = 14.4.dp,
            viewportWidth = 11.7f,
            viewportHeight = 14.4f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF059669)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M5.0625 9.84375 L9.1875 5.7 L8.23125 4.74375 L5.0625 7.93125" +
                        "L3.46875 6.3375 L2.5125 7.29375 L5.0625 9.84375" +
                        "M5.85 14.4 C4.1625 13.9875 2.76562 13.0322 1.65937 11.5342" +
                        "C0.553125 10.0362 0 8.37268 0 6.54375 V2.25 L5.85 0 L11.7 2.25 V6.54375" +
                        "C11.7 8.37268 11.1469 10.0362 10.0406 11.5342" +
                        "C8.93437 13.0322 7.5375 13.9875 5.85 14.4" +
                        "M5.85 12.9937 C7.15 12.5906 8.225 11.7844 9.075 10.575" +
                        "C9.925 9.36562 10.35 8.02187 10.35 6.54375 V3.16875 L5.85 1.44375" +
                        "L1.35 3.16875 V6.54375 C1.35 8.02187 1.775 9.36562 2.625 10.575" +
                        "C3.475 11.7844 4.55 12.5906 5.85 12.9937"
                )
            }
        }.build()
        return _ShieldCheckIcon!!
    }

private var _ShieldCheckIcon: ImageVector? = null