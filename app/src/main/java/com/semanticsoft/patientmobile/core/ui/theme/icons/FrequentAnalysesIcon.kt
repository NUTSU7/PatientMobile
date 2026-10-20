package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Purple500

val FrequentAnalysesIcon: ImageVector
    get() {
        if (_FrequentAnalysesIcon != null) return _FrequentAnalysesIcon!!
        _FrequentAnalysesIcon = ImageVector.Builder(
            name = "FrequentAnalysesIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            val brandGradient = Brush.linearGradient(
                0.0f to Blue900,
                0.5f to Blue500,
                1.0f to Purple500
            )

            path(
                fill = brandGradient,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M21.3125 0H2.6875C1.1875 0 0 1.1875 0 2.6875V21.3125C0 22.8125 1.1875 24 2.6875 24H21.3125C22.8125 24 24 22.8125 24 21.3125V2.6875C24 1.1875 22.8125 0 21.3125 0ZM6.6875 18.6875C5.9375 18.6875 5.3125 18.0625 5.3125 17.3125V13.3125C5.3125 12.625 5.9375 12 6.6875 12C7.375 12 8 12.625 8 13.3125V17.3125C8 18.0625 7.375 18.6875 6.6875 18.6875ZM12 18.6875C11.25 18.6875 10.6875 18.0625 10.6875 17.3125V16C10.6875 15.25 11.25 14.6875 12 14.6875C12.75 14.6875 13.3125 15.25 13.3125 16V17.3125C13.3125 18.0625 12.75 18.6875 12 18.6875ZM12 12C11.25 12 10.6875 11.375 10.6875 10.6875C10.6875 9.9375 11.25 9.3125 12 9.3125C12.75 9.3125 13.3125 9.9375 13.3125 10.6875C13.3125 11.375 12.75 12 12 12ZM17.3125 18.6875C16.625 18.6875 16 18.0625 16 17.3125V6.6875C16 5.9375 16.625 5.3125 17.3125 5.3125C18.0625 5.3125 18.6875 5.9375 18.6875 6.6875V17.3125C18.6875 18.0625 18.0625 18.6875 17.3125 18.6875Z"
                )
            }
        }.build()
        return _FrequentAnalysesIcon!!
    }

private var _FrequentAnalysesIcon: ImageVector? = null
