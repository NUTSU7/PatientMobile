package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LockIcon: ImageVector
    get() {
        if (_LockIcon != null) return _LockIcon!!
        _LockIcon = ImageVector.Builder(
            name = "LockIcon",
            defaultWidth = 13.32.dp,
            defaultHeight = 17.5.dp,
            viewportWidth = 13.3203f,
            viewportHeight = 17.5f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4648D4)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M1.66505 17.5C1.20715 17.5 0.815177 17.3368 0.489106 17.0104C0.163035 16.684 0 16.2917 0 15.8333" +
                        "V7.5C0 7.04167 0.163035 6.64931 0.489106 6.32292C0.815177 5.99653 1.20715 5.83333 1.66505 5.83333" +
                        "H2.49756V4.16667C2.49756 3.01389 2.90342 2.03125 3.71513 1.21875C4.52684 0.40625 5.50852 0 6.66018 0" +
                        "C7.81182 0 8.79351 0.40625 9.60521 1.21875C10.4169 2.03125 10.8227 3.01389 10.8227 4.16667" +
                        "V5.83333H11.6553C12.1132 5.83333 12.5052 5.99653 12.8313 6.32292C13.1573 6.64931 13.3203 7.04167 13.3203 7.5" +
                        "V15.8333C13.3203 16.2917 13.1573 16.684 12.8313 17.0104C12.5052 17.3368 12.1132 17.5 11.6553 17.5H1.66505Z" +
                        "M1.66505 15.8333H11.6553V7.5H1.66505V15.8333ZM6.66018 13.3333" +
                        "C7.11806 13.3333 7.51004 13.1701 7.83611 12.8438C8.16218 12.5174 8.32521 12.125 8.32521 11.6667" +
                        "C8.32521 11.2083 8.16218 10.816 7.83611 10.4896C7.51004 10.1632 7.11806 10 6.66018 10" +
                        "C6.20228 10 5.81031 10.1632 5.48423 10.4896C5.15816 10.816 4.99513 11.2083 4.99513 11.6667" +
                        "C4.99513 12.125 5.15816 12.5174 5.48423 12.8438C5.81031 13.1701 6.20228 13.3333 6.66018 13.3333Z" +
                        "M4.16261 5.83333H9.15774V4.16667C9.15774 3.47222 8.91492 2.88194 8.42928 2.39583" +
                        "C7.94364 1.90972 7.35394 1.66667 6.66018 1.66667C5.9664 1.66667 5.3767 1.90972 4.89106 2.39583" +
                        "C4.40542 2.88194 4.16261 3.47222 4.16261 4.16667V5.83333Z"
                )
            }
        }.build()
        return _LockIcon!!
    }

private var _LockIcon: ImageVector? = null
