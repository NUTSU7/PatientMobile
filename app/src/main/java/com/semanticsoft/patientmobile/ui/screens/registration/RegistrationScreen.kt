package com.semanticsoft.patientmobile.ui.screens.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.components.PasswordCriteriaRow
import com.semanticsoft.patientmobile.ui.components.PasswordMatchIndicator
import com.semanticsoft.patientmobile.ui.components.computePasswordCriteria
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthInput
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthLabel
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthTopHeader
import com.semanticsoft.patientmobile.ui.theme.Gray100
import com.semanticsoft.patientmobile.ui.theme.Gray200
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500

@Composable
fun RegistrationScreen(
    state: RegistrationUiState,
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
            isVeryCompactHeight -> 240.dp
            isCompactHeight -> 270.dp
            else -> 320.dp
        }
        val contentVerticalPadding = when {
            isVeryCompactHeight -> 6.dp
            isCompactHeight -> 8.dp
            else -> 10.dp
        }
        val sectionSpacing = when {
            isVeryCompactHeight -> 10.dp
            isCompactHeight -> 12.dp
            else -> 16.dp
        }
        val tinySpacing = if (isVeryCompactHeight) 3.dp else 4.dp
        val beforeButtonSpacing = if (isVeryCompactHeight) 10.dp else 14.dp
        val buttonToFooterSpacing = 2.dp
        val bottomSpacing = 0.dp
        val titleFontSize = if (isVeryCompactHeight) 20.sp else 24.sp
        val titleLineHeight = if (isVeryCompactHeight) 24.sp else 28.sp
        val inputHeight = if (isVeryCompactHeight) 38.dp else 42.dp
        val buttonHeight = if (isVeryCompactHeight) 42.dp else 46.dp

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AuthTopHeader(sidePadding = sidePadding, headerHeight = headerHeight, isCompact = isCompactHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = sidePadding, vertical = contentVerticalPadding)
            ) {
                Spacer(modifier = Modifier.height(if (isVeryCompactHeight) 8.dp else 12.dp))
                Text(
                    text = "Introdu datele tale mai jos",
                    color = Color(0xFF111827),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = titleLineHeight
                )
                Text(
                    text = "pentru a-\u021Bi crea contul",
                    color = Color(0xFF111827),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = titleLineHeight
                )

                Spacer(modifier = Modifier.height(if (isVeryCompactHeight) 20.dp else 28.dp))
                AuthLabel("Scrie un nume sau un pseudonim")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.name,
                    placeholder = "Introdu nume",
                    onValueChange = onNameChange,
                    isError = state.fieldErrors.containsKey("name"),
                    errorMessage = state.fieldErrors["name"],
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
                    errorMessage = state.fieldErrors["email"],
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Parol\u0103")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.password,
                    placeholder = "Min. 8 caractere",
                    onValueChange = onPasswordChange,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    isError = state.fieldErrors.containsKey("password"),
                    errorMessage = state.fieldErrors["password"],
                    inputHeight = inputHeight,
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )

                if (state.password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val criteria = computePasswordCriteria(state.password, state.email)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Gray100, RoundedCornerShape(8.dp))
                            .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PasswordCriteriaRow(met = criteria.hasLength, label = "Minim 8 caractere")
                        PasswordCriteriaRow(met = criteria.hasUppercase, label = "Minim o liter\u0103 majuscul\u0103")
                        PasswordCriteriaRow(met = criteria.hasSpecial, label = "Minim un caracter special (ex: !, @, #, $, etc.)")
                        PasswordCriteriaRow(met = criteria.hasDigit, label = "Minim o cifr\u0103")
                        PasswordCriteriaRow(met = criteria.notCommon, label = "Parola nu este prea comun\u0103")
                        PasswordCriteriaRow(met = criteria.notPersonal, label = "Parola nu este bazat\u0103 pe date personale")
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Confirm\u0103 parola")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.confirmPassword,
                    placeholder = "Confirm\u0103 parola",
                    onValueChange = onConfirmPasswordChange,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    isError = state.fieldErrors.containsKey("confirmPassword"),
                    errorMessage = state.fieldErrors["confirmPassword"],
                    inputHeight = inputHeight,
                    trailing = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )

                if (state.confirmPassword.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    PasswordMatchIndicator(passwordsMatch = state.password == state.confirmPassword)
                }

                Spacer(modifier = Modifier.height(beforeButtonSpacing))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight)
                        .background(
                            Brush.horizontalGradient(listOf(Indigo600, Purple500)),
                            RoundedCornerShape(999.dp)
                        )
                        .clickable(onClick = onRegisterClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isLoading) "Se proceseaz\u0103..." else "Creeaz\u0103",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                if (state.isLoading) {
                    LoadingIndicator(
                        message = "\u00CEnregistrare \u00EEn curs...",
                        modifier = Modifier.padding(top = 10.dp)
                    )
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
                            text = "Autentific\u0103-te aici",
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
