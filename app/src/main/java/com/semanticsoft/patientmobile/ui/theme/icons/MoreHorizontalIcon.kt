package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val MoreHorizontalIcon: ImageVector
    get() {
        if (_MoreHorizontalIcon != null) return _MoreHorizontalIcon!!
        _MoreHorizontalIcon = ImageVector.Builder(
            name = "MoreHorizontalIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF6B7280)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M7,12 a2,2,0,1,0,-4,0 a2,2,0,1,0,4,0 " +
                    "M14,12 a2,2,0,1,0,-4,0 a2,2,0,1,0,4,0 " +
                    "M21,12 a2,2,0,1,0,-4,0 a2,2,0,1,0,4,0"
                )
            }
        }.build()
        return _MoreHorizontalIcon!!
    }

private var _MoreHorizontalIcon: ImageVector? = null
