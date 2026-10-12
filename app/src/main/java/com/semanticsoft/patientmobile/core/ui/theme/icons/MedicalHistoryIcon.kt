package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Purple500

val MedicalHistoryIcon: ImageVector
    get() {
        if (_MedicalHistoryIcon != null) return _MedicalHistoryIcon!!
        _MedicalHistoryIcon = ImageVector.Builder(
            name = "MedicalHistoryIcon",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            val brandGradient = Brush.linearGradient(
                0.0f to Blue900,
                0.5f to Blue500,
                1.0f to Purple500
            )

            group(translationY = -0.25f) {
                path(
                    stroke = brandGradient,
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    addPathData(
                        "M5.25 2.5h7.5" +
                            "M6.5 2.5v2.5M11.5 2.5v2.5" +
                            "M5.5 5H12.5A2 2 0 0 1 14.5 7V14A2 2 0 0 1 12.5 16H5.5" +
                            "A2 2 0 0 1 3.5 14V7A2 2 0 0 1 5.5 5Z" +
                            "M9 8.5v5M6.75 11h4.5"
                    )
                }
            }
        }.build()
        return _MedicalHistoryIcon!!
    }

private var _MedicalHistoryIcon: ImageVector? = null
