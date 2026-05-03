package com.semanticsoft.patientmobile.ui.screens.login

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.Icon
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
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthInput
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthLabel
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthTopHeader
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit
) {
    SetStatusBar(color = Color(0xFF3B82F6), darkIcons = false)
    var passwordVisible by remember { mutableStateOf(false) }
    var hasAttemptedLogin by remember { mutableStateOf(false) }

    val missingCredentialsMessage = when {
        state.email.isBlank() && state.password.isBlank() -> "Completeaza emailul si parola"
        state.email.isBlank() -> "Completeaza emailul"
        state.password.isBlank() -> "Completeaza parola"
        else -> null
    }

    val loginWarningMessage = when {
        state.errorMessage != null -> "Email sau parola invalida"
        hasAttemptedLogin -> missingCredentialsMessage
        else -> null
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        val isCompactHeight = maxHeight < 780.dp
        val isVeryCompactHeight = maxHeight < 700.dp
        val headerHeight = when {
            isVeryCompactHeight -> 240.dp
            isCompactHeight -> 270.dp
            else -> 320.dp
        }
        val sidePadding = when {
            maxWidth >= 430.dp -> 28.dp
            maxWidth >= 375.dp -> 20.dp
            else -> 16.dp
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
        val titleFontSize = if (isVeryCompactHeight) 20.sp else 24.sp
        val titleLineHeight = if (isVeryCompactHeight) 24.sp else 28.sp
        val inputHeight = if (isVeryCompactHeight) 38.dp else 42.dp
        val buttonHeight = if (isVeryCompactHeight) 42.dp else 46.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AuthTopHeader(sidePadding = sidePadding, headerHeight = headerHeight, isCompact = isCompactHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding, vertical = contentVerticalPadding)
            ) {
                Spacer(modifier = Modifier.height(if (isVeryCompactHeight) 16.dp else if (isCompactHeight) 24.dp else 32.dp))
                Text(
                    text = "Acceseaz\u0103 contul t\u0103u",
                    color = Color(0xFF111827),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = titleLineHeight
                )
                Spacer(modifier = Modifier.height(if (isVeryCompactHeight) 20.dp else 28.dp))
                AuthLabel("Adresa de email")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.email,
                    placeholder = "nume@exemplu.com",
                    onValueChange = onEmailChange,
                    isError = state.errorMessage != null || (hasAttemptedLogin && state.email.isBlank()),
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AuthLabel("Parol\u0103")
                    Text(
                        text = "Ai uitat parola?",
                        color = Color(0xFF4F46E5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.password,
                    placeholder = "Introdu parola ta",
                    onValueChange = onPasswordChange,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    isError = state.errorMessage != null || (hasAttemptedLogin && state.password.isBlank()),
                    inputHeight = inputHeight,
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = if (passwordVisible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                            tint = if (passwordVisible) Color(0xFF4F46E5) else Color(0xFF9CA3AF),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(beforeButtonSpacing))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight)
                        .background(
                            Brush.horizontalGradient(listOf(Indigo600, Purple500)),
                            RoundedCornerShape(999.dp)
                        )
                        .clickable {
                            hasAttemptedLogin = true
                            onLoginClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isLoading) "Se autentific\u0103..." else "Acceseaz\u0103",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                if (state.isLoading) {
                    LoadingIndicator(
                        message = "Autentificare \u00EEn curs...",
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (!loginWarningMessage.isNullOrBlank()) {
                        Text(
                            text = loginWarningMessage,
                            color = Color(0xFFB91C1C),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(0.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nu ai cont?",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onGoToRegister, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text(
                            text = "\u00CEnregistreaz\u0103-te aici.",
                            color = Color(0xFF4F46E5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
