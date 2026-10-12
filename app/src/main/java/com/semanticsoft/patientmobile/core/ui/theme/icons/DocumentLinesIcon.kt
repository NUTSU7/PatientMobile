package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DocumentLinesIcon: ImageVector
    get() {
        if (_DocumentLinesIcon != null) return _DocumentLinesIcon!!
        _DocumentLinesIcon = ImageVector.Builder(
            name = "DocumentLinesIcon",
            defaultWidth = 11.99.dp,
            defaultHeight = 12.66.dp,
            viewportWidth = 11.9922f,
            viewportHeight = 12.6562f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF464554)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M0 12.6562V11.3239H7.99479V12.6562H0Z" +
                        "M3.76422 9.42555L0 5.66199L1.39909 4.22984L5.19661 7.9934L3.76422 9.42555Z" +
                        "M7.99479 5.19571L4.23057 1.39884L5.66298 0L9.42719 3.76356L7.99479 5.19571Z" +
                        "M11.0595 11.9901L2.36513 3.29728L3.29785 2.36472L11.9922 11.0576L11.0595 11.9901Z"
                )
            }
        }.build()
        return _DocumentLinesIcon!!
    }

private var _DocumentLinesIcon: ImageVector? = null
