package com.semanticsoft.patientmobile.feature.laborator.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.core.ui.components.form.FormInput
import com.semanticsoft.patientmobile.core.ui.components.form.FormLabel
import com.semanticsoft.patientmobile.core.ui.components.auth.AuthTopHeader
import com.semanticsoft.patientmobile.core.ui.components.GradientPillButton
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.CheckboxBorder
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.components.form.FormTextArea

private val FieldGroupGap = 10.dp
private val LabelInputGap = 4.dp

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
            .background(SurfaceWhite)
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
            AuthTopHeader(
                sidePadding = sidePadding,
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
                    .padding(horizontal = sidePadding)
                    .padding(top = 24.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "Devino partener Patient",
                    color = Gray900,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Completează detaliile laboratorului tău. Conexiunea va fi stabilită în maxim 5 zile lucrătoare.",
                    color = Gray500,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column(verticalArrangement = Arrangement.spacedBy(FieldGroupGap)) {
                    Column {
                        FormLabel("Denumire laborator")
                        Spacer(modifier = Modifier.height(LabelInputGap))
                        FormInput(
                            value = state.labName,
                            placeholder = "Laboratorul din care faci parte",
                            onValueChange = onLabNameChange,
                            inputHeight = AppDimens.inputHeightDefault
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(FieldGroupGap)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Țară")
                            Spacer(modifier = Modifier.height(LabelInputGap))
                            FormInput(
                                value = state.country,
                                placeholder = "Țara",
                                onValueChange = onCountryChange,
                                inputHeight = AppDimens.inputHeightDefault
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("CUI")
                            Spacer(modifier = Modifier.height(LabelInputGap))
                            FormInput(
                                value = state.cui,
                                placeholder = "CUI laborator",
                                onValueChange = onCuiChange,
                                inputHeight = AppDimens.inputHeightDefault
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = CheckboxBorder.copy(alpha = 0.5f)
                    )

                    Column {
                        FormLabel("Numele Prenumele")
                        Spacer(modifier = Modifier.height(LabelInputGap))
                        FormInput(
                            value = state.contactName,
                            placeholder = "Introdu numele și prenumele",
                            onValueChange = onContactNameChange,
                            inputHeight = AppDimens.inputHeightDefault
                        )
                    }

                    Column {
                        FormLabel("Funcție")
                        Spacer(modifier = Modifier.height(LabelInputGap))
                        FormInput(
                            value = state.role,
                            placeholder = "Funcția pe care o deții în cadrul laboratorului",
                            onValueChange = onRoleChange,
                            inputHeight = AppDimens.inputHeightDefault
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(FieldGroupGap)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("E-mail instituțional")
                            Spacer(modifier = Modifier.height(LabelInputGap))
                            FormInput(
                                value = state.email,
                                placeholder = "contact@laborator.md",
                                onValueChange = onEmailChange,
                                inputHeight = AppDimens.inputHeightDefault
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Telefon")
                            Spacer(modifier = Modifier.height(LabelInputGap))
                            FormInput(
                                value = state.phone,
                                placeholder = "Număr de telefon",
                                onValueChange = onPhoneChange,
                                inputHeight = AppDimens.inputHeightDefault
                            )
                        }
                    }

                    Column {
                        FormLabel("Mesaj (opțional)")
                        Spacer(modifier = Modifier.height(LabelInputGap))
                        FormTextArea(
                            value = state.message,
                            placeholder = "Alte detalii pe care doriți să le comunicați...",
                            onValueChange = onMessageChange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))

                GradientPillButton(
                    text = "Devino partener",
                    onClick = onRegisterClick,
                    icon = null,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ai deja cont?",
                        color = Gray500,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = onGoToLogin,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Autentifică-te aici",
                            color = Indigo600,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
