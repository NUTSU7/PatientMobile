package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val EmailCheckIcon: ImageVector
    get() {
        if (_EmailCheckIcon != null) return _EmailCheckIcon!!
        _EmailCheckIcon = ImageVector.Builder(
            name = "EmailCheckIcon",
            defaultWidth = 28.dp,
            defaultHeight = 24.dp,
            viewportWidth = 28f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4F46E5)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M18.5444 24L12.9778 18.4333L14.7 16.7111L18.5444 20.5555L26.2778 12.8222L28 14.5444L18.5444 24" +
                        "M13.3111 9.25556L24.1333 2.44446H2.4889L13.3111 9.25556" +
                        "M13.3111 11.7L2.44446 4.84447V18.7556H9.86665L12.3111 21.2H2.44446" +
                        "C1.77779 21.2 1.20371 20.9593 0.722226 20.4778" +
                        "C0.240742 19.9963 0 19.4222 0 18.7556V2.44446" +
                        "C0 1.77779 0.240742 1.20371 0.722226 0.722225" +
                        "C1.20371 0.240742 1.77779 0 2.44446 0H24.1889" +
                        "C24.8555 0 25.4296 0.240742 25.9111 0.722225" +
                        "C26.3926 1.20371 26.6333 1.77779 26.6333 2.44446V9.02219L24.1889 11.4666V4.84447L13.3111 11.7"
                )
            }
        }.build()
        return _EmailCheckIcon!!
    }

private var _EmailCheckIcon: ImageVector? = null
