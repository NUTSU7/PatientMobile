package com.semanticsoft.patientmobile.ui.screens.login.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.screens.login.LoginRole
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.GrayLightBorder
import com.semanticsoft.patientmobile.ui.theme.InputBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRoleSelector(
    selectedRole: LoginRole,
    onRoleSelected: (LoginRole) -> Unit,
    modifier: Modifier = Modifier,
    inputHeight: Dp = 42.dp
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(inputHeight)
                .background(InputBackground, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedRole.label,
                color = Gray900,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, GrayLightBorder)
        ) {
            LoginRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.label, fontSize = 14.sp) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}