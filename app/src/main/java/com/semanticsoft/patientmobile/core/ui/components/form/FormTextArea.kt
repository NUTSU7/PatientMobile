package com.semanticsoft.patientmobile.core.ui.components.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.InputHaloShadow
import com.semanticsoft.patientmobile.core.ui.theme.InputShadow
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite

@Composable
fun FormTextArea(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = shape,
                ambientColor = InputHaloShadow,
                spotColor = InputHaloShadow
            )
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = InputShadow,
                spotColor = InputShadow
            )
            .background(SurfaceWhite, shape)
            .border(1.dp, Gray300, shape)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = Gray500,
                fontSize = 13.sp
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Gray900, fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
