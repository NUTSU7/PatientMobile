package com.estcomputer.patient.designsystem.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.estcomputer.patient.designsystem.theme.LaboratorAnalysisSubtitle

@Suppress("unused")
val ChevronDownIcon: ImageVector
    get() {
        if (_ChevronDownIcon != null) return _ChevronDownIcon!!
        _ChevronDownIcon = ImageVector.Builder(
            name = "ChevronDownIcon",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(LaboratorAnalysisSubtitle),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 9.5f)
                lineTo(12f, 15.5f)
                lineTo(18f, 9.5f)
            }
        }.build()
        return _ChevronDownIcon!!
    }

private var _ChevronDownIcon: ImageVector? = null
