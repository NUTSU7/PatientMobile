package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val ExpiryClockIcon: ImageVector
    get() {
        if (_ExpiryClockIcon != null) return _ExpiryClockIcon!!
        _ExpiryClockIcon = ImageVector.Builder(
            name = "ExpiryClockIcon",
            defaultWidth = 12.dp,
            defaultHeight = 14.dp,
            viewportWidth = 11.9f,
            viewportHeight = 14.025f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4F46E5)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M3.825 1.275V0H8.075V1.275H3.825" +
                        "M5.3125 8.925H6.5875V4.675H5.3125V8.925" +
                        "M5.95 14.025" +
                        "C5.12361 14.025 4.35082 13.8685 3.63163 13.5556" +
                        "C2.91244 13.2427 2.28384 12.8172 1.74582 12.2792" +
                        "C1.2078 11.7412 0.782324 11.1126 0.469395 10.3934" +
                        "C0.156465 9.67418 0 8.90139 0 8.075" +
                        "C0 7.24861 0.156465 6.47582 0.469395 5.75663" +
                        "C0.782324 5.03744 1.2078 4.40884 1.74582 3.87082" +
                        "C2.28384 3.3328 2.91244 2.90732 3.63163 2.59439" +
                        "C4.35082 2.28146 5.12101 2.125 5.94221 2.125" +
                        "C6.63024 2.125 7.29052 2.2401 7.92306 2.47031" +
                        "C8.5556 2.70052 9.1434 3.02222 9.68646 3.43542" +
                        "L10.6073 2.53229L11.4927 3.41771L10.5896 4.33854" +
                        "C11.0028 4.86979 11.3245 5.45303 11.5547 6.08827" +
                        "C11.7849 6.7235 11.9 7.38659 11.9 8.07755" +
                        "C11.9 8.90224 11.7435 9.67418 11.4306 10.3934" +
                        "C11.1177 11.1126 10.6922 11.7412 10.1542 12.2792" +
                        "C9.61616 12.8172 8.98756 13.2427 8.26837 13.5556" +
                        "C7.54918 13.8685 6.77639 14.025 5.95 14.025" +
                        "M5.95 12.75" +
                        "C7.24861 12.75 8.35243 12.2955 9.26146 11.3865" +
                        "C10.1705 10.4774 10.625 9.37361 10.625 8.075" +
                        "C10.625 6.77639 10.1705 5.67257 9.26146 4.76354" +
                        "C8.35243 3.85451 7.24861 3.4 5.95 3.4" +
                        "C4.65139 3.4 3.54757 3.85451 2.63854 4.76354" +
                        "C1.72951 5.67257 1.275 6.77639 1.275 8.075" +
                        "C1.275 9.37361 1.72951 10.4774 2.63854 11.3865" +
                        "C3.54757 12.2955 4.65139 12.75 5.95 12.75" +
                        "M5.95 8.075"
                )
            }
        }.build()
        return _ExpiryClockIcon!!
    }

private var _ExpiryClockIcon: ImageVector? = null
