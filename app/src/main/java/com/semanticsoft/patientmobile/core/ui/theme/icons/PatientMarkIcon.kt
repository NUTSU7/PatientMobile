package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("unused")
val PatientMarkIcon: ImageVector
    get() {
        if (_PatientMarkIcon != null) return _PatientMarkIcon!!
        _PatientMarkIcon = ImageVector.Builder(
            name = "PatientMarkIcon",
            defaultWidth = 32.dp,
            defaultHeight = 30.36.dp,
            viewportWidth = 34f,
            viewportHeight = 33f
        ).apply {
            val tileGradient = Brush.linearGradient(
                0f to Color(0xFF1E3A8A),
                0.5f to Color(0xFF3B82F6),
                1f to Color(0xFF8B5CF6),
                start = Offset(-4f, 4.53f),
                end = Offset(18.86f, 40.67f)
            )
            val sparkleGradient = Brush.linearGradient(
                0f to Color(0xFF1E3A8A),
                0.5f to Color(0xFF3B82F6),
                1f to Color(0xFF8B5CF6),
                start = Offset(15.18f, 4.07f),
                end = Offset(27f, 22.85f)
            )

            path(
                fill = tileGradient,
                fillAlpha = 0.9f,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M12 2H20A12 12 0 0 1 32 14V20.359A12 12 0 0 1 20 32.359H12" +
                        "A12 12 0 0 1 0 20.359V14A12 12 0 0 1 12 2Z"
                )
            }

            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M20.9883 11H10.5117C9.668 11 9 11.668 9 12.5117V22.9883" +
                        "C9 23.832 9.668 24.5 10.5117 24.5H20.9883C21.832 24.5 22.5 23.832 22.5 22.9883" +
                        "V12.5117C22.5 11.668 21.832 11 20.9883 11Z" +
                        "M19.5117 19.2617H17.2617V21.5117C17.2617 21.8984 16.9102 22.25 16.4883 22.25" +
                        "H15.0117C14.5898 22.25 14.2383 21.8984 14.2383 21.5117V19.2617" +
                        "H11.9883C11.6016 19.2617 11.25 18.9102 11.25 18.4883V17.0117" +
                        "C11.25 16.5898 11.6016 16.2383 11.9883 16.2383H14.2383V13.9883" +
                        "C14.2383 13.6016 14.5898 13.25 15.0117 13.25H16.4883" +
                        "C16.9102 13.25 17.2617 13.6016 17.2617 13.9883V16.2383H19.5117" +
                        "C19.8984 16.2383 20.25 16.5898 20.25 17.0117V18.4883" +
                        "C20.25 18.9102 19.8984 19.2617 19.5117 19.2617Z"
                )
            }

            path(
                fill = SolidColor(Color.White),
                stroke = sparkleGradient,
                strokeLineWidth = 0.05f,
                strokeLineJoin = StrokeJoin.Bevel,
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M30.1463 8.95208L29.2899 6.92897L27.3143 5.96784L29.3374 5.11141" +
                        "L30.2986 3.13589L31.155 5.159L33.1305 6.12014L31.1074 6.97656L30.1463 8.95208Z" +
                        "M29.8798 19.1304L29.0233 17.1073L27.0478 16.1462L29.0709 15.2897" +
                        "L30.0321 13.3142L30.8885 15.3373L32.864 16.2985L30.8409 17.1549L29.8798 19.1304Z" +
                        "M22.6666 16.759L20.9538 12.7127L17.0027 10.7905L21.049 9.07762" +
                        "L22.9712 5.12659L24.6841 9.17281L28.6351 11.0951L24.5889 12.8079L22.6666 16.759Z"
                )
            }
        }.build()
        return _PatientMarkIcon!!
    }

private var _PatientMarkIcon: ImageVector? = null