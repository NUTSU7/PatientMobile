package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ShieldOutlineIcon: ImageVector
    get() {
        if (_ShieldOutlineIcon != null) return _ShieldOutlineIcon!!
        _ShieldOutlineIcon = ImageVector.Builder(
            name = "ShieldOutlineIcon",
            defaultWidth = 11.97.dp,
            defaultHeight = 14.98.dp,
            viewportWidth = 11.9727f,
            viewportHeight = 14.9805f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF6B38D4)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M5.98633 14.9805C4.25279 14.5435 2.82168 13.5479 1.693 11.9938C0.564336 10.4395 0 8.71364 0 6.81611" +
                        "V2.24707L5.98633 0L11.9727 2.24707V6.81611C11.9727 8.71364 11.4083 10.4395 10.2796 11.9938" +
                        "C9.15098 13.5479 7.71987 14.5435 5.98633 14.9805ZM5.98633 13.4075" +
                        "C7.28337 12.9956 8.35592 12.1716 9.20398 10.9357C10.052 9.69985 10.4761 8.32664 10.4761 6.81611" +
                        "V3.27698L5.98633 1.59167L1.49658 3.27698V6.81611C1.49658 8.32664 1.92061 9.69985 2.76868 10.9357" +
                        "C3.61674 12.1716 4.68929 12.9956 5.98633 13.4075Z"
                )
            }
        }.build()
        return _ShieldOutlineIcon!!
    }

private var _ShieldOutlineIcon: ImageVector? = null
