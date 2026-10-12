package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val EnvelopeIcon: ImageVector
    get() {
        if (_EnvelopeIcon != null) return _EnvelopeIcon!!
        _EnvelopeIcon = ImageVector.Builder(
            name = "EnvelopeIcon",
            defaultWidth = 17.97.dp,
            defaultHeight = 13.93.dp,
            viewportWidth = 17.9667f,
            viewportHeight = 13.9333f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4F46E5)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M1.74167 13.9333 C1.26271 13.9333 0.852691 13.7627 0.511615 13.4216" +
                        "C0.170538 13.0804 0 12.6703 0 12.1912 V1.73842" +
                        "C0 1.25933 0.170538 0.849826 0.511615 0.509896" +
                        "C0.852691 0.169965 1.26271 0 1.74167 0 H16.225" +
                        "C16.704 0 17.114 0.170583 17.4551 0.511751" +
                        "C17.7961 0.852918 17.9667 1.26304 17.9667 1.74213 V12.1949" +
                        "C17.9667 12.674 17.7961 13.0835 17.4551 13.4234" +
                        "C17.114 13.7634 16.704 13.9333 16.225 13.9333 H1.74167 V13.9333" +
                        "M8.98333 7.975 L1.74167 3.63229 V12.1917 V12.1917 V12.1917" +
                        "H16.225 V12.1917 V12.1917 V3.63229 L8.98333 7.975" +
                        "M8.98333 6.08437 L16.225 1.74167 H1.74167 L8.98333 6.08437"
                )
            }
        }.build()
        return _EnvelopeIcon!!
    }

private var _EnvelopeIcon: ImageVector? = null