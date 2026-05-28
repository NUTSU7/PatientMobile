package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val IconBadge: ImageVector
    get() {
        if (_IconBadge != null) return _IconBadge!!
        _IconBadge = ImageVector.Builder(
            name = "IconBadge",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData("M12,2L20,5L20,11.5C20,17 12,22 12,22C12,22 4,17 4,11.5L4,5Z")
            }
            path(
                stroke = SolidColor(Color(0xFF9CA3AF)),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(8f, 12f)
                lineTo(11f, 15f)
                lineTo(17f, 8f)
            }
        }.build()
        return _IconBadge!!
    }

private var _IconBadge: ImageVector? = null
