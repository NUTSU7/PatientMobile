package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ShieldAlertIcon: ImageVector
    get() {
        if (_ShieldAlertIcon != null) return _ShieldAlertIcon!!
        _ShieldAlertIcon = ImageVector.Builder(
            name = "ShieldAlertIcon",
            defaultWidth = 10.dp,
            defaultHeight = 13.dp,
            viewportWidth = 10.3711f,
            viewportHeight = 12.7539f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M4.58721 9.56543H5.78388V5.57983H4.58721V9.56543ZM5.18205 4.38416" +
                        "C5.35059 4.38416 5.49302 4.3272 5.60937 4.21328C5.72571 4.09936 5.78388 3.95821 " +
                        "5.78388 3.78981C5.78388 3.62141 5.72688 3.4791 5.61287 3.36285" +
                        "C5.49885 3.2466 5.35758 3.18848 5.18905 3.18848C5.02051 3.18848 " +
                        "4.87807 3.24543 4.76173 3.35935C4.64538 3.47327 4.58721 3.61442 4.58721 3.78282" +
                        "C4.58721 3.95122 4.64422 4.09354 4.75823 4.20979C4.87224 4.32604 5.01352 4.38416 " +
                        "5.18205 4.38416ZM5.18555 12.7539C3.68972 12.3885 2.4515 11.5425 1.4709 10.2157" +
                        "C0.4903 8.8889 0 7.41558 0 5.79572V1.9928L5.18555 0L10.3711 1.9928V5.79572" +
                        "C10.3711 7.41558 9.88079 8.8889 8.90019 10.2157C7.9196 11.5425 6.68138 12.3885 " +
                        "5.18555 12.7539ZM5.18555 11.5084C6.33789 11.1514 7.29079 10.4373 8.04425 9.36615" +
                        "C8.7977 8.29502 9.17443 7.10488 9.17443 5.79572V2.80653L5.18555 1.27871" +
                        "L1.19666 2.80653V5.79572C1.19666 7.10488 1.57339 8.29502 2.32684 9.36615" +
                        "C3.0803 10.4373 4.0332 11.1514 5.18555 11.5084Z"
                )
            }
        }.build()
        return _ShieldAlertIcon!!
    }

private var _ShieldAlertIcon: ImageVector? = null
