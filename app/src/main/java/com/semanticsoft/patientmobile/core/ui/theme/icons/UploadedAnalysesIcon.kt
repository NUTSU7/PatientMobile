package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Purple500

val UploadedAnalysesIcon: ImageVector
    get() {
        if (_UploadedAnalysesIcon != null) return _UploadedAnalysesIcon!!
        _UploadedAnalysesIcon = ImageVector.Builder(
            name = "UploadedAnalysesIcon",
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

            group(
                scaleX = 0.8333f,
                scaleY = 0.8333f,
                translationX = 1.5f,
                translationY = 1.5f
            ) {
                path(
                    fill = brandGradient,
                    pathFillType = PathFillType.NonZero
                ) {
                    addPathData(
                        "M4 0C2.9 0 2 0.9 2 2V16C2 17.1 2.9 18 4 18H14C15.1 18 16 17.1 16 16V6L10 0H4Z" +
                            "M10 6V1L15 6H10ZM5 10H13V12H5V10ZM5 13H10V15H5V13Z"
                    )
                }
            }
        }.build()
        return _UploadedAnalysesIcon!!
    }

private var _UploadedAnalysesIcon: ImageVector? = null
