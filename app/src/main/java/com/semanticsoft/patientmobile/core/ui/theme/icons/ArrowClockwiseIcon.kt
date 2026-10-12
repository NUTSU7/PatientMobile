package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val ArrowClockwiseIcon: ImageVector
    get() {
        if (_ArrowClockwiseIcon != null) return _ArrowClockwiseIcon!!
        _ArrowClockwiseIcon = ImageVector.Builder(
            name = "ArrowClockwiseIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M22.5 5.25 V9.75 C22.5 9.94891 22.421 10.1397 22.2803 10.2803" +
                        "C22.1397 10.421 21.9489 10.5 21.75 10.5 H17.25" +
                        "C17.0511 10.5 16.8603 10.421 16.7197 10.2803" +
                        "C16.579 10.1397 16.5 9.94891 16.5 9.75 C16.5 9.55109 16.579 9.36032 16.7197 9.21967" +
                        "C16.8603 9.07902 17.0511 9 17.25 9 H19.8188 L17.3259 6.71625 L17.3025 6.69375" +
                        "C16.2601 5.65171 14.9337 4.93993 13.4891 4.64732" +
                        "C12.0445 4.35472 10.5457 4.49427 9.1799 5.04853" +
                        "C7.81414 5.6028 6.64196 6.5472 5.80982 7.76378" +
                        "C4.97767 8.98035 4.52244 10.4152 4.501 11.8889" +
                        "C4.47955 13.3627 4.89285 14.8102 5.68925 16.0504" +
                        "C6.48565 17.2907 7.62985 18.2688 8.97891 18.8626" +
                        "C10.328 19.4564 11.8221 19.6394 13.2746 19.389" +
                        "C14.7271 19.1385 16.0736 18.4656 17.1459 17.4544" +
                        "C17.2905 17.3176 17.4835 17.2439 17.6824 17.2494" +
                        "C17.8814 17.255 18.07 17.3393 18.2067 17.4839" +
                        "C18.3435 17.6285 18.4172 17.8215 18.4116 18.0204" +
                        "C18.4061 18.2193 18.3218 18.4079 18.1772 18.5447" +
                        "C16.5098 20.126 14.298 21.0052 12 21 H11.8763" +
                        "C10.4023 20.9798 8.95578 20.5979 7.66397 19.8877" +
                        "C6.37215 19.1776 5.27455 18.161 4.46762 16.9274" +
                        "C3.66069 15.6937 3.16914 14.2807 3.03619 12.8126" +
                        "C2.90323 11.3445 3.13293 9.86615 3.7051 8.50759" +
                        "C4.27727 7.14902 5.17439 5.9518 6.31762 5.02115" +
                        "C7.46084 4.0905 8.81516 3.45491 10.2615 3.17024" +
                        "C11.7079 2.88558 13.2021 2.96055 14.6127 3.38858" +
                        "C16.0234 3.81661 17.3072 4.58458 18.3516 5.625 L21 8.04375 V5.25" +
                        "C21 5.05109 21.079 4.86032 21.2197 4.71967" +
                        "C21.3603 4.57902 21.5511 4.5 21.75 4.5 C21.9489 4.5 22.1397 4.57902 22.2803 4.71967" +
                        "C22.421 4.86032 22.5 5.05109 22.5 5.25"
                )
            }
        }.build()
        return _ArrowClockwiseIcon!!
    }

private var _ArrowClockwiseIcon: ImageVector? = null