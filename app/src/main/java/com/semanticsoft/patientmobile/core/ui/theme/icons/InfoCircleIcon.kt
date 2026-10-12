package com.semanticsoft.patientmobile.core.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val InfoCircleIcon: ImageVector
    get() {
        if (_InfoCircleIcon != null) return _InfoCircleIcon!!
        _InfoCircleIcon = ImageVector.Builder(
            name = "InfoCircleIcon",
            defaultWidth = 15.dp,
            defaultHeight = 15.dp,
            viewportWidth = 15f,
            viewportHeight = 15f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4648D4)),
                pathFillType = PathFillType.NonZero
            ) {
                addPathData(
                    "M6.75 11.25H8.25V6.75H6.75V11.25ZM7.5 5.25C7.7125 5.25 7.89063 5.17813 8.03438 5.03438" +
                        "C8.17813 4.89063 8.25 4.7125 8.25 4.5C8.25 4.2875 8.17813 4.10938 8.03438 3.96563" +
                        "C7.89063 3.82188 7.7125 3.75 7.5 3.75C7.2875 3.75 7.10937 3.82188 6.96562 3.96563" +
                        "C6.82187 4.10938 6.75 4.2875 6.75 4.5C6.75 4.7125 6.82187 4.89063 6.96562 5.03438" +
                        "C7.10937 5.17813 7.2875 5.25 7.5 5.25ZM7.5 15C6.4625 15 5.4875 14.8031 4.575 14.4094" +
                        "C3.6625 14.0156 2.86875 13.4812 2.19375 12.8062C1.51875 12.1312 0.984375 11.3375 0.590625 10.425" +
                        "C0.196875 9.5125 0 8.5375 0 7.5C0 6.4625 0.196875 5.4875 0.590625 4.575" +
                        "C0.984375 3.6625 1.51875 2.86875 2.19375 2.19375C2.86875 1.51875 3.6625 0.984375 4.575 0.590625" +
                        "C5.4875 0.196875 6.4625 0 7.5 0C8.5375 0 9.5125 0.196875 10.425 0.590625" +
                        "C11.3375 0.984375 12.1312 1.51875 12.8062 2.19375C13.4812 2.86875 14.0156 3.6625 14.4094 4.575" +
                        "C14.8031 5.4875 15 6.4625 15 7.5C15 8.5375 14.8031 9.5125 14.4094 10.425" +
                        "C14.0156 11.3375 13.4812 12.1312 12.8062 12.8062C12.1312 13.4812 11.3375 14.0156 10.425 14.4094" +
                        "C9.5125 14.8031 8.5375 15 7.5 15Z"
                )
            }
        }.build()
        return _InfoCircleIcon!!
    }

private var _InfoCircleIcon: ImageVector? = null
