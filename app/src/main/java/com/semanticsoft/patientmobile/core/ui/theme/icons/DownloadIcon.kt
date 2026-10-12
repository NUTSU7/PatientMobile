package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DownloadIcon: ImageVector
    get() {
        if (_DownloadIcon != null) return _DownloadIcon!!
        _DownloadIcon = ImageVector.Builder(
            name = "DownloadIcon",
            defaultWidth = 10.66.dp,
            defaultHeight = 10.66.dp,
            viewportWidth = 10.6641f,
            viewportHeight = 10.6641f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4648D4)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M5.33201 7.99802L1.99951 4.66552L2.9326 3.69909L4.66552 5.43199V0H5.99852V5.43199" +
                        "L7.73142 3.69909L8.66453 4.66552L5.33201 7.99802ZM1.333 10.6641" +
                        "C0.966428 10.6641 0.652617 10.5335 0.39157 10.2725C0.130524 10.0114 0 9.6976 0 9.33102" +
                        "V7.33152H1.333V9.33102H9.33102V7.33152H10.6641V9.33102C10.6641 9.6976 10.5335 10.0114 10.2725 10.2725" +
                        "C10.0114 10.5335 9.6976 10.6641 9.33102 10.6641H1.333Z"
                )
            }
        }.build()
        return _DownloadIcon!!
    }

private var _DownloadIcon: ImageVector? = null
