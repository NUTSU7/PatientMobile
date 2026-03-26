package com.semanticsoft.patientmobile.ui.screens.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            RegistrationTopHeader(sidePadding = sidePadding)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding, vertical = 20.dp)
            ) {
                Text(
                    text = "Înregistrează-te",
                    color = Color(0xFF111827),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Completează datele pentru a crea contul tău.",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                AuthLabel("Mă înregistrez ca...")
                Spacer(modifier = Modifier.height(6.dp))
                RoleField(
                    text = if (state.role.isBlank()) "Selectează rolul..." else state.role,
                    isPlaceholder = state.role.isBlank(),
                    onRoleChange = onRoleChange
                )

                Spacer(modifier = Modifier.height(12.dp))
                AuthLabel("Nume sau Pseudonim")
                Spacer(modifier = Modifier.height(6.dp))
                AuthInput(
                    value = state.name,
                    placeholder = "Introdu nume, pseudonim sau orice identificator preferi.",
                    onValueChange = onNameChange
                )

                Spacer(modifier = Modifier.height(12.dp))
                AuthLabel("Adresa de email")
                Spacer(modifier = Modifier.height(6.dp))
                AuthInput(
                    value = state.email,
                    placeholder = "nume@exemplu.com",
                    onValueChange = onEmailChange
                )

                Spacer(modifier = Modifier.height(12.dp))
                AuthLabel("Parolă")
                Spacer(modifier = Modifier.height(6.dp))
                AuthInput(
                    value = state.password,
                    placeholder = "Min. 15 caractere",
                    onValueChange = onPasswordChange,
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = "Afișează parola",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                AuthLabel("Confirmă parola")
                Spacer(modifier = Modifier.height(6.dp))
                AuthInput(
                    value = state.confirmPassword,
                    placeholder = "Confirmă parola",
                    onValueChange = onConfirmPasswordChange,
                    trailing = {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = "Afișează parola",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text(
                        text = "Creează contul",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
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
            }
        }
    }
}

@Composable
private fun RegistrationTopHeader(sidePadding: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.85.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF9333EA))
                )
            )
    ) {
        Column(modifier = Modifier.padding(horizontal = sidePadding, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "PATIENT.MD",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Bine ai venit!",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Analizele tale, explicate pe înțelesul tău.",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun RoleField(text: String, isPlaceholder: Boolean, onRoleChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
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
                    color = if (isPlaceholder) Color(0xFF9CA3AF) else Color(0xFF111827),
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = if (isPlaceholder) "" else text,
                onValueChange = onRoleChange,
                textStyle = TextStyle(color = Color(0xFF111827), fontSize = 14.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AuthInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
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
                textStyle = TextStyle(color = Color(0xFF111827), fontSize = 14.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        trailing?.invoke()
    }
}
