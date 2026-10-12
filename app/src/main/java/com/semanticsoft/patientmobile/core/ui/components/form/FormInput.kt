package com.semanticsoft.patientmobile.core.ui.components.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.ErrorLightBg
import com.semanticsoft.patientmobile.core.ui.theme.ErrorText
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.InputHaloShadow
import com.semanticsoft.patientmobile.core.ui.theme.InputShadow
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite

@Composable
fun FormInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    inputHeight: Dp = 42.dp,
    fontSize: TextUnit = 13.sp,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(inputHeight)
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
            .background(if (isError) ErrorLightBg else SurfaceWhite, shape)
            .border(1.dp, if (isError) ErrorText else Gray300, shape)
            .padding(horizontal = 21.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()
        if (leading != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = if (isError && !errorMessage.isNullOrBlank()) errorMessage else placeholder,
                    color = if (isError) ErrorText else Gray500,
                    fontSize = fontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = if (isError) ErrorText else Gray900, fontSize = fontSize),
                singleLine = true,
                keyboardOptions = keyboardOptions,
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        trailing?.invoke()
    }
}
