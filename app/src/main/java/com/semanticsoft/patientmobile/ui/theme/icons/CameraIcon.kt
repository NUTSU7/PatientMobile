package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CameraIcon: ImageVector
    get() {
        if (_CameraIcon != null) return _CameraIcon!!
        _CameraIcon = ImageVector.Builder(
            name = "CameraIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 15.2f)
                addPathData("C13.7673,15.2 15.2,13.7673 15.2,12C15.2,10.2327 13.7673,8.8 12,8.8C10.2327,8.8 8.8,10.2327 8.8,12C8.8,13.7673 10.2327,15.2 12,15.2Z")
                moveTo(9f, 2f)
                lineTo(7.17f, 4f)
                horizontalLineTo(4f)
                addPathData("C2.9,4 2,4.9 2,6V18C2,19.1 2.9,20 4,20H20C21.1,20 22,19.1 22,18V6C22,4.9 21.1,4 20,4H16.83L15,2H9ZM12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17Z")
            }
        }.build()
        return _CameraIcon!!
    }

private var _CameraIcon: ImageVector? = null
