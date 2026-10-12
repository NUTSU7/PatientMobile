package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LaptopCheckIcon: ImageVector
    get() {
        if (_LaptopCheckIcon != null) return _LaptopCheckIcon!!
        _LaptopCheckIcon = ImageVector.Builder(
            name = "LaptopCheckIcon",
            defaultWidth = 16.48.dp,
            defaultHeight = 13.5.dp,
            viewportWidth = 16.4844f,
            viewportHeight = 13.4961f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF6B38D4)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M7.4367 8.2851L11.6889 4.04883L10.6212 2.98039L7.4367 6.16696L5.84446 4.57368L4.79545 5.62337L7.4367 8.2851Z" +
                        "M0 13.4961V11.9965H16.4844V13.4961H0Z" +
                        "M2.24787 11.2467C1.83576 11.2467 1.48296 11.0999 1.18949 10.8063C0.896026 10.5126 0.74929 10.1596 0.74929 9.74718" +
                        "V1.49957C0.74929 1.08719 0.896026 0.734162 1.18949 0.440498C1.48296 0.146833 1.83576 0 2.24787 0" +
                        "H14.2365C14.6486 0 15.0014 0.146833 15.2949 0.440498C15.5883 0.734162 15.7351 1.08719 15.7351 1.49957" +
                        "V9.74718C15.7351 10.1596 15.5883 10.5126 15.2949 10.8063C15.0014 11.0999 14.6486 11.2467 14.2365 11.2467" +
                        "H2.24787ZM2.24787 9.74718H14.2365V1.49957H2.24787V9.74718Z"
                )
            }
        }.build()
        return _LaptopCheckIcon!!
    }

private var _LaptopCheckIcon: ImageVector? = null
