package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HospitalIcon: ImageVector
    get() {
        if (_HospitalIcon != null) return _HospitalIcon!!
        _HospitalIcon = ImageVector.Builder(
            name = "HospitalIcon",
            defaultWidth = 14.98.dp,
            defaultHeight = 13.5.dp,
            viewportWidth = 14.9805f,
            viewportHeight = 13.4961f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF6B38D4)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M0 13.4961V0H7.49023V2.99913H14.9805V13.4961H0Z" +
                        "M1.49805 11.9965H2.99609V10.497H1.49805V11.9965Z" +
                        "M1.49805 8.9974H2.99609V7.49783H1.49805V8.9974Z" +
                        "M1.49805 5.99826H2.99609V4.4987H1.49805V5.99826Z" +
                        "M1.49805 2.99913H2.99609V1.49957H1.49805V2.99913Z" +
                        "M4.49414 11.9965H5.99219V10.497H4.49414V11.9965Z" +
                        "M4.49414 8.9974H5.99219V7.49783H4.49414V8.9974Z" +
                        "M4.49414 5.99826H5.99219V4.4987H4.49414V5.99826Z" +
                        "M4.49414 2.99913H5.99219V1.49957H4.49414V2.99913Z" +
                        "M7.49023 11.9965H13.4824V4.4987H7.49023V5.99826H8.98828V7.49783H7.49023V8.9974" +
                        "H8.98828V10.497H7.49023V11.9965ZM10.4863 7.49783V5.99826H11.9844V7.49783H10.4863Z" +
                        "M10.4863 10.497V8.9974H11.9844V10.497H10.4863Z"
                )
            }
        }.build()
        return _HospitalIcon!!
    }

private var _HospitalIcon: ImageVector? = null
