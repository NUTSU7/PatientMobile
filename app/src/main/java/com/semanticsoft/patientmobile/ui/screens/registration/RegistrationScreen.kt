package com.semanticsoft.patientmobile.ui.screens.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.common.SetStatusBar

@Composable
fun RegistrationScreen(
    state: RegistrationUiState,
    onRoleChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoToLogin: () -> Unit
) {
    SetStatusBar(color = Color(0xFF3B82F6), darkIcons = false)
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        val sidePadding = when {
            maxWidth >= 430.dp -> 28.dp
            maxWidth >= 375.dp -> 20.dp
            else -> 16.dp
        }
        val isCompactHeight = maxHeight < 780.dp
        val isVeryCompactHeight = maxHeight < 700.dp
        val headerHeight = when {
            isVeryCompactHeight -> 132.dp
            isCompactHeight -> 144.dp
            else -> 156.85.dp
        }
        val contentVerticalPadding = when {
            isVeryCompactHeight -> 10.dp
            isCompactHeight -> 14.dp
            else -> 20.dp
        }
        val sectionSpacing = when {
            isVeryCompactHeight -> 8.dp
            isCompactHeight -> 10.dp
            else -> 12.dp
        }
        val tinySpacing = if (isVeryCompactHeight) 4.dp else 6.dp
        val afterTitleSpacing = if (isVeryCompactHeight) 6.dp else 8.dp
        val afterSubtitleSpacing = if (isVeryCompactHeight) 10.dp else 16.dp
        val beforeButtonSpacing = if (isVeryCompactHeight) 10.dp else 16.dp
        val buttonToFooterSpacing = 0.dp
        val bottomSpacing = if (isVeryCompactHeight) 8.dp else 14.dp
        val titleFontSize = if (isVeryCompactHeight) 30.sp else 34.sp
        val titleLineHeight = if (isVeryCompactHeight) 34.sp else 38.sp
        val subtitleFontSize = if (isVeryCompactHeight) 13.sp else 14.sp
        val inputHeight = if (isVeryCompactHeight) 40.dp else 44.dp
        val buttonHeight = if (isVeryCompactHeight) 40.dp else 44.dp

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            RegistrationTopHeader(sidePadding = sidePadding, headerHeight = headerHeight, compact = isCompactHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = sidePadding, vertical = contentVerticalPadding)
            ) {
                Text(
                    text = "Înregistrează-te",
                    color = Color(0xFF111827),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = titleLineHeight
                )
                Spacer(modifier = Modifier.height(afterTitleSpacing))
                Text(
                    text = "Completează datele pentru a crea contul tău.",
                    color = Color(0xFF6B7280),
                    fontSize = subtitleFontSize
                )

                Spacer(modifier = Modifier.height(afterSubtitleSpacing))
                AuthLabel("Mă înregistrez ca...")
                Spacer(modifier = Modifier.height(tinySpacing))
                RoleField(
                    text = if (state.role.isBlank()) "Selectează rolul..." else state.role,
                    isPlaceholder = state.role.isBlank(),
                    onRoleChange = onRoleChange,
                    isError = state.fieldErrors.containsKey("role"),
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Nume sau Pseudonim")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.name,
                    placeholder = "Introdu nume, pseudonim sau orice identificator preferi.",
                    onValueChange = onNameChange,
                    isError = state.fieldErrors.containsKey("name"),
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Adresa de email")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.email,
                    placeholder = "nume@exemplu.com",
                    onValueChange = onEmailChange,
                    isError = state.fieldErrors.containsKey("email"),
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Parolă")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.password,
                    placeholder = "Min. 15 caractere",
                    onValueChange = onPasswordChange,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    isError = state.fieldErrors.containsKey("password"),
                    inputHeight = inputHeight,
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = "Afișează parola",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Confirmă parola")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.confirmPassword,
                    placeholder = "Confirmă parola",
                    onValueChange = onConfirmPasswordChange,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    isError = state.fieldErrors.containsKey("confirmPassword"),
                    inputHeight = inputHeight,
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = "Afișează parola",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { confirmPasswordVisible = !confirmPasswordVisible }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(beforeButtonSpacing))
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text(
                        text = if (state.isLoading) "Se procesează..." else "Creează contul",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                if (state.isLoading) {
                    LoadingIndicator(
                        message = "Înregistrare în curs...",
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                val requiredFieldLabels = listOf(
                    "role" to "Rolul",
                    "name" to "Numele",
                    "email" to "Emailul",
                    "password" to "Parola",
                    "confirmPassword" to "Confirmarea parolei"
                ).mapNotNull { (key, label) ->
                    state.fieldErrors[key]
                        ?.takeIf { it.contains("obligatoriu", ignoreCase = true) }
                        ?.let { label }
                }
                val requiredFieldsLine = requiredFieldLabels
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(", ")
                    ?.plus(" este obligatoriu")
                val otherValidationMessages = state.fieldErrors
                    .filterNot { (key, _) ->
                        key in setOf("role", "name", "email", "password", "confirmPassword") &&
                            state.fieldErrors[key]?.contains("obligatoriu", ignoreCase = true) == true
                    }
                    .values
                val validationMessages = buildList {
                    requiredFieldsLine?.let { add(it) }
                    addAll(otherValidationMessages)
                    state.errorMessage?.let { add(it) }
                }

                // Show validation errors below button
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isVeryCompactHeight) 24.dp else 28.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        validationMessages.take(2).forEach { message ->
                            Text(
                                text = message,
                                color = Color(0xFFB91C1C),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(buttonToFooterSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ai deja cont?",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onGoToLogin, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text(
                            text = "Autentifică-te aici",
                            color = Color(0xFF4F46E5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(bottomSpacing))
            }
        }
    }
}

@Composable
private fun RegistrationTopHeader(
    sidePadding: androidx.compose.ui.unit.Dp,
    headerHeight: androidx.compose.ui.unit.Dp,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF9333EA))
                )
            )
    ) {
        Column(modifier = Modifier.padding(horizontal = sidePadding, vertical = if (compact) 14.dp else 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 30.dp else 32.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (compact) 14.dp else 16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "PATIENT.MD",
                    color = Color.White,
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))
            Text(
                text = "Bine ai venit!",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Analizele tale, explicate pe înțelesul tău.",
                color = Color.White,
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compact) 17.sp else 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(if (compact) 6.dp else 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)) {
                HeaderChip(text = "Conform HIPAA", icon = Icons.Outlined.Shield)
                HeaderChip(text = "Criptare Securizată", icon = Icons.Outlined.Lock)
            }
        }
    }
}

@Composable
private fun HeaderChip(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .height(24.6.dp)
            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(12.dp)
        )
        Text(text = text, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun AuthLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF111827),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun RoleField(
    text: String,
    isPlaceholder: Boolean,
    onRoleChange: (String) -> Unit,
    isError: Boolean = false,
    inputHeight: androidx.compose.ui.unit.Dp = 44.dp
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Pacient", "Doctor")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(inputHeight)
            .padding(horizontal = 12.dp)
            .clickable { expanded = true }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        color = when {
                            isError -> Color(0xFFB91C1C)
                            isPlaceholder -> Color(0xFF9CA3AF)
                            else -> Color(0xFF111827)
                        },
                        fontSize = 14.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { expanded = true }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.Transparent,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.width(220.dp)) {
                    options.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    color = Color(0xFF111827),
                                    fontSize = 14.sp,
                                    fontWeight = if (text == option) FontWeight.SemiBold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.PersonOutline,
                                    contentDescription = null,
                                    tint = if (text == option) Color(0xFF4F46E5) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                onRoleChange(option)
                                expanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (text == option) Color(0xFFF5F3FF) else Color.Transparent
                                )
                        )

                        if (index < options.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    isError: Boolean = false,
    inputHeight: androidx.compose.ui.unit.Dp = 44.dp,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(inputHeight)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(text = placeholder, color = Color(0xFFCCCCCC), fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = if (isError) Color(0xFFB91C1C) else Color(0xFF111827), fontSize = 14.sp),
                singleLine = true,
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
