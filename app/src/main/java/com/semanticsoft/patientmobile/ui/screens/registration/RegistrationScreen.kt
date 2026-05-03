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
import androidx.compose.material.icons.Icons
import com.semanticsoft.patientmobile.ui.theme.icons.AppLogoIcon
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.components.LoadingIndicator
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthInput
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthLabel

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
            RegistrationTopHeader(sidePadding = sidePadding, headerHeight = headerHeight, compact = isCompactHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                    text = "pentru a-ți crea contul",
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
                AuthLabel("Parolă")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.password,
                    placeholder = "Min. 15 caractere",
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
                                contentDescription = if (passwordVisible) "Ascunde parola" else "Afișează parola",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                    errorMessage = state.fieldErrors["confirmPassword"],
                    inputHeight = inputHeight,
                    trailing = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ascunde parola" else "Afișează parola",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                        text = if (state.isLoading) "Se procesează..." else "Creează",
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
        Column(modifier = Modifier.padding(horizontal = sidePadding).padding(top = if (compact) 12.dp else 16.dp, bottom = if (compact) 14.dp else 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AppLogoIcon,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(if (compact) 48.dp else 56.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Patient.md",
                    color = Color.White,
                    fontSize = if (compact) 22.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 14.dp else 18.dp))
            Text(
                text = "Patient.md este un portal care te poate ajuta să ai grijă de sănătatea ta.",
                color = Color.White,
                fontSize = if (compact) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compact) 22.sp else 27.sp
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
            Text(
                text = "Acum poți:",
                color = Color.White,
                fontSize = if (compact) 15.sp else 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(if (compact) 6.dp else 8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 7.dp)) {
                BulletItem("Încărca analizele tale medicale", compact)
                BulletItem("Vizualiza analizele încărcate de tine", compact)
                BulletItem("Păstra și monitoriza analizele", compact)
            }

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                modifier = Modifier
                    .height(if (compact) 30.dp else 34.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Toate datele încărcate sunt criptate",
                    color = Color.White,
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            }
        }
    }
}

@Composable
private fun BulletItem(text: String, compact: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 7.dp else 8.dp)
                .background(Color.White, CircleShape)
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = if (compact) 13.sp else 14.sp
        )
    }
}

