package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.ErrorLightBg
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray400
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.InputHaloShadow
import com.semanticsoft.patientmobile.core.ui.theme.InputShadow

@Composable
fun DialogFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Gray500,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.inputHeightDefault)
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
            .background(if (isError) ErrorLightBg else Color.White, shape)
            .border(BorderStroke(1.dp, Gray300), shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Gray500,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = if (enabled) Gray900 else Gray400, fontSize = 13.sp),
                singleLine = true,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(Gray900),
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun DialogMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    minLines: Int = 4
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
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
            .background(if (isError) ErrorLightBg else Color.White, shape)
            .border(BorderStroke(1.dp, Gray300), shape)
            .padding(12.dp)
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = Gray500, fontSize = 13.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Gray900, fontSize = 13.sp),
            cursorBrush = SolidColor(Gray900),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogDropdownField(
    selectedText: String,
    placeholder: String,
    options: List<DialogDropdownOption>,
    onSelect: (DialogDropdownOption) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(AppDimens.inputHeightDefault)
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
                .background(if (isError) ErrorLightBg else Color.White, shape)
                .border(BorderStroke(1.dp, Gray300), shape)
                .padding(horizontal = 12.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedText.ifEmpty { placeholder },
                    color = if (selectedText.isEmpty()) Gray500 else Gray900,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = shape,
            containerColor = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, Gray300)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, fontSize = 13.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogCompactDropdownField(
    selectedText: String,
    options: List<DialogDropdownOption>,
    onSelect: (DialogDropdownOption) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Row(
            modifier = modifier
                .height(AppDimens.inputHeightSmall)
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
                .background(if (isError) ErrorLightBg else Color.White, shape)
                .border(BorderStroke(1.dp, Gray300), shape)
                .padding(horizontal = 8.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedText,
                color = Gray900,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = shape,
            containerColor = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, Gray300)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, fontSize = 13.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DialogHorizontalStepperField(
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double,
    min: Double,
    formatValue: (Double) -> String = { v ->
        if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
    },
    placeholder: String? = null,
    trailingText: String? = null,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val hasPlaceholder = !placeholder.isNullOrEmpty()
    var displayText by remember {
        mutableStateOf(if (value == min && hasPlaceholder) "" else formatValue(value))
    }
    val shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)

    Row(
        modifier = modifier
            .height(AppDimens.inputHeightDefault)
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
            .background(if (isError) ErrorLightBg else Color.White, shape)
            .border(BorderStroke(1.dp, Gray300), shape)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val new = (value - step).coerceAtLeast(min)
                onValueChange(new)
                displayText = if (new == min && hasPlaceholder) "" else formatValue(new)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Text(
                text = "\u2212",
                fontSize = 18.sp,
                color = Gray900,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .widthIn(min = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (value == min && hasPlaceholder && displayText.isEmpty()) {
                Text(
                    text = placeholder!!,
                    color = Gray500,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = displayText,
                onValueChange = { newText ->
                    displayText = newText
                    val parsed = newText.toDoubleOrNull()
                    if (parsed != null) {
                        val coerced = parsed.coerceAtLeast(min)
                        onValueChange(coerced)
                    } else if (newText.isEmpty()) {
                        onValueChange(min)
                        displayText = ""
                    }
                },
                textStyle = TextStyle(
                    color = Gray900,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(Gray900),
                modifier = Modifier.fillMaxWidth()
            )
        }

        IconButton(
            onClick = {
                val new = value + step
                onValueChange(new)
                displayText = if (new == min && hasPlaceholder) "" else formatValue(new)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Text(
                text = "+",
                fontSize = 18.sp,
                color = Gray900,
                fontWeight = FontWeight.Medium
            )
        }

        trailingText?.let {
            Text(
                text = it,
                color = Gray500,
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

data class DialogDropdownOption(
    val key: String,
    val label: String
)
