package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PdfDocumentIcon: ImageVector
    get() {
        if (_PdfDocumentIcon != null) return _PdfDocumentIcon!!
        _PdfDocumentIcon = ImageVector.Builder(
            name = "PdfDocumentIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFEF4444)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20f, 2f)
                horizontalLineTo(8f)
                addPathData("C6.9,2 6,2.9 6,4V16C6,17.1 6.9,18 8,18H20C21.1,18 22,17.1 22,16V4C22,2.9 21.1,2 20,2Z")
                moveTo(4f, 6f)
                horizontalLineTo(2f)
                verticalLineTo(20f)
                addPathData("C2,21.1 2.9,22 4,22H18V20H4V6Z")
                moveTo(10f, 11f)
                horizontalLineTo(11.5f)
                addPathData("C12.33,11 13,11.67 13,12.5V12.5C13,13.33 12.33,14 11.5,14H10V11ZM10,10V14.5")
                moveTo(14f, 10f)
                horizontalLineTo(15.5f)
                addPathData("C16.33,10 17,10.67 17,11.5V11.5C17,12.33 16.33,13 15.5,13H14V10Z")
            }
        }.build()
        return _PdfDocumentIcon!!
    }

private var _PdfDocumentIcon: ImageVector? = null
