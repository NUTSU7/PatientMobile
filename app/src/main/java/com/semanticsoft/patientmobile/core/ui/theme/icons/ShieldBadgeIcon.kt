package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ShieldBadgeIcon: ImageVector
    get() {
        if (_ShieldBadgeIcon != null) return _ShieldBadgeIcon!!
        _ShieldBadgeIcon = ImageVector.Builder(
            name = "ShieldBadgeIcon",
            defaultWidth = 9.32.dp,
            defaultHeight = 11.66.dp,
            viewportWidth = 9.31641f,
            viewportHeight = 11.6602f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4648D4)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M4.04682 7.89974L7.33667 4.60574L6.50693 3.77496L4.04682 6.23817" +
                        "L2.82404 5.01386L1.9943 5.84464L4.04682 7.89974ZM4.65821 11.6602" +
                        "C3.30927 11.32 2.19566 10.5451 1.3174 9.33538C0.439133 8.12565 0 6.7823 0 5.30535" +
                        "V1.74902L4.65821 0L9.31641 1.74902V5.30535C9.31641 6.7823 8.87727 8.12565 7.99901 9.33538" +
                        "C7.12074 10.5451 6.00715 11.32 4.65821 11.6602Z"
                )
            }
        }.build()
        return _ShieldBadgeIcon!!
    }

private var _ShieldBadgeIcon: ImageVector? = null
