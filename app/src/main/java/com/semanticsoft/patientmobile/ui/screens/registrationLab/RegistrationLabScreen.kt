package com.semanticsoft.patientmobile.ui.screens.registrationLab

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthInput
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthLabel
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthTopHeader
import com.semanticsoft.patientmobile.ui.screens.registrationLab.components.LabMessageInput
import com.semanticsoft.patientmobile.ui.theme.Gray200
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500

@Composable
fun RegistrationLabScreen(
    state: RegistrationLabUiState,
    onLabNameChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onCuiChange: (String) -> Unit,
    onContactNameChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
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
        val isCompactHeight = maxHeight < 780.dp
        val isVeryCompactHeight = maxHeight < 700.dp
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
            AuthTopHeader(
                sidePadding = sidePadding,
                isCompact = isCompactHeight,
                wrapContentHeight = true,
                subtitleText = "Conectează-ți laboratorul la ecosistemul digital al pacienților.",
                showSubtitleLabel = false,
                bullets = listOf(
                    "Audiență digitală suplimentară",
                    "Statistici agregate privind implicarea pacienților",
                    "Continuitate pentru pacienții cu multiple laboratoare"
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding, vertical = contentVerticalPadding)
            ) {
                Spacer(modifier = Modifier.height(if (isVeryCompactHeight) 8.dp else 12.dp))
                Text(
                    text = "Devino partener Patient",
                    color = Color(0xFF111827),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = titleLineHeight
                )
                Spacer(modifier = Modifier.height(if (isVeryCompactHeight) 4.dp else 6.dp))
                Text(
                    text = "Completează detaliile laboratorului tău",
                    color = Gray500,
                    fontSize = if (isVeryCompactHeight) 13.sp else 14.sp,
                    lineHeight = if (isVeryCompactHeight) 18.sp else 20.sp
                )
                Text(
                    text = "Conexiunea va fi stabilită în maxim 5 zile lucrătoare.",
                    color = Gray500,
                    fontSize = if (isVeryCompactHeight) 13.sp else 14.sp,
                    lineHeight = if (isVeryCompactHeight) 18.sp else 20.sp
                )

                Spacer(modifier = Modifier.height(if (isVeryCompactHeight) 20.dp else 28.dp))
                AuthLabel("Denumire laborator")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.labName,
                    placeholder = "Laboratorul din care faci parte",
                    onValueChange = onLabNameChange,
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            AuthLabel("Țară")
                            Spacer(modifier = Modifier.height(tinySpacing))
                            AuthInput(
                                value = state.country,
                                placeholder = "Țara",
                                onValueChange = onCountryChange,
                                inputHeight = inputHeight
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            AuthLabel("CUI")
                            Spacer(modifier = Modifier.height(tinySpacing))
                            AuthInput(
                                value = state.cui,
                                placeholder = "CUI laborator",
                                onValueChange = onCuiChange,
                                inputHeight = inputHeight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing / 2))
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = Gray200
                )
                Spacer(modifier = Modifier.height(sectionSpacing / 2))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding)
            ) {
                AuthLabel("Numele Prenumele")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.contactName,
                    placeholder = "Introdu numele și prenumele",
                    onValueChange = onContactNameChange,
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Funcție")
                Spacer(modifier = Modifier.height(tinySpacing))
                AuthInput(
                    value = state.role,
                    placeholder = "Completează cu funcția pe care o deții în cadrul laborator",
                    onValueChange = onRoleChange,
                    inputHeight = inputHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            AuthLabel("E-mail instituțional")
                            Spacer(modifier = Modifier.height(tinySpacing))
                            AuthInput(
                                value = state.email,
                                placeholder = "contact@laborator.md",
                                onValueChange = onEmailChange,
                                inputHeight = inputHeight
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            AuthLabel("Telefon")
                            Spacer(modifier = Modifier.height(tinySpacing))
                            AuthInput(
                                value = state.phone,
                                placeholder = "Număr de telefon",
                                onValueChange = onPhoneChange,
                                inputHeight = inputHeight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))
                AuthLabel("Mesaj (opțional)")
                Spacer(modifier = Modifier.height(tinySpacing))
                LabMessageInput(
                    value = state.message,
                    placeholder = "Alte detalii pe care doriți să le comunicați...",
                    onValueChange = onMessageChange,
                    modifier = Modifier.height(100.dp)
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
                        .clickable(onClick = onRegisterClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Devino partener",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
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