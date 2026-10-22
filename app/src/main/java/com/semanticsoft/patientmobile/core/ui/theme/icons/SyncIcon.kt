package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SyncIcon: ImageVector
    get() {
        if (_SyncIcon != null) return _SyncIcon!!
        _SyncIcon = ImageVector.Builder(
            name = "SyncIcon",
            defaultWidth = 8.dp,
            defaultHeight = 8.dp,
            viewportWidth = 8.39844f,
            viewportHeight = 8.39844f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M0.349935 8.39844V7.34863H1.4289C0.991483 6.95982 0.643977 6.49566 " +
                        "0.386386 5.95618C0.128795 5.4167 0 4.83104 0 4.19922C0 3.21746 " +
                        "0.296473 2.35477 0.889418 1.61116C1.48236 0.867547 2.23569 0.379096 " +
                        "3.14941 0.145806V1.23935C2.53703 1.46292 2.03399 1.83959 1.64031 2.36935 " +
                        "C1.24665 2.89911 1.0498 3.50907 1.0498 4.19922C1.0498 4.6658 1.14458 5.09836 " +
                        "1.33413 5.4969C1.52368 5.89543 1.77884 6.23565 2.09961 6.51754V5.59896H3.14941" +
                        "V8.39844H0.349935ZM5.24902 8.25264V7.15909C5.86141 6.93552 6.36444 6.55885 " +
                        "6.75812 6.02909C7.1518 5.49933 7.34863 4.88937 7.34863 4.19922C7.34863 3.73264 " +
                        "7.25386 3.30008 7.06432 2.90154C6.87476 2.503 6.6196 2.16279 6.29883 1.8809" +
                        "V2.79948H5.24902V0H8.0485V1.0498H6.96953C7.40695 1.43862 7.75446 1.90277 " +
                        "8.01205 2.44226C8.26964 2.98174 8.39844 3.5674 8.39844 4.19922C8.39844 5.18098 " +
                        "8.10196 6.04367 7.50902 6.78728C6.91607 7.53089 6.16274 8.01934 5.24902 8.25264Z"
                )
            }
        }.build()
        return _SyncIcon!!
    }

private var _SyncIcon: ImageVector? = null
