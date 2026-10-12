package com.semanticsoft.patientmobile.feature.auth.forgotPassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.core.ui.theme.AppBackground
import com.semanticsoft.patientmobile.core.ui.theme.Blue500
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Gray400
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Purple500
import com.semanticsoft.patientmobile.core.ui.theme.icons.PatientMarkIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ShieldCheckIcon
import com.semanticsoft.patientmobile.feature.auth.forgotPassword.components.ConfirmCodeCard
import com.semanticsoft.patientmobile.feature.auth.forgotPassword.components.ErrorCodeCard
import com.semanticsoft.patientmobile.feature.auth.forgotPassword.components.NewPasswordCard
import com.semanticsoft.patientmobile.feature.auth.forgotPassword.components.RequestEmailCard

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onRequestSubmit: () -> Unit,
    onCodeChange: (String) -> Unit,
    onResendClick: () -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitNewPassword: () -> Unit,
    onRequestNewCode: () -> Unit,
    onBackToLogin: () -> Unit
) {
    SetStatusBar(color = AppBackground, darkIcons = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ForgotPasswordTopBar()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val targetIsError = targetState == ForgotPasswordStep.ERROR
                    val currentIsError = initialState == ForgotPasswordStep.ERROR
                    if (targetIsError || currentIsError) {
                        (fadeIn(animationSpec = tween(durationMillis = 220)) + scaleIn(
                            initialScale = 0.98f,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                        )).togetherWith(fadeOut(animationSpec = tween(durationMillis = 120)))
                    } else {
                        val forward = targetState.ordinal > initialState.ordinal
                        (slideInHorizontally(
                            initialOffsetX = { if (forward) it / 10 else -it / 10 },
                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(durationMillis = 240)))
                            .togetherWith(fadeOut(animationSpec = tween(durationMillis = 140)))
                    }
                }
            ) { step ->
                when (step) {
                    ForgotPasswordStep.REQUEST -> RequestEmailCard(
                        email = state.email,
                        onEmailChange = onEmailChange,
                        onSubmit = onRequestSubmit,
                        onBackToLogin = onBackToLogin,
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage
                    )

                    ForgotPasswordStep.CONFIRM -> ConfirmCodeCard(
                        code = state.code,
                        onCodeChange = onCodeChange,
                        onResendClick = onResendClick,
                        onBackToLogin = onBackToLogin,
                        resendCooldownSeconds = state.resendCooldownSeconds,
                        errorMessage = state.errorMessage
                    )

                    ForgotPasswordStep.SET_NEW -> NewPasswordCard(
                        newPassword = state.newPassword,
                        confirmPassword = state.confirmPassword,
                        email = state.email,
                        onNewPasswordChange = onNewPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onSubmit = onSubmitNewPassword,
                        onBackToLogin = onBackToLogin,
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage
                    )

                    ForgotPasswordStep.ERROR -> ErrorCodeCard(
                        onRequestNewCode = onRequestNewCode,
                        onBackToLogin = onBackToLogin,
                        isLoading = state.isLoading,
                        resendCooldownSeconds = state.resendCooldownSeconds,
                        errorMessage = state.errorMessage
                    )
                }
            }
            }

        }

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 220))
                    .togetherWith(fadeOut(animationSpec = tween(durationMillis = 120)))
            }
        ) { step ->
            Text(
                text = when (step) {
                    ForgotPasswordStep.SET_NEW -> "© 2026 Patient • Standarde de securitate ISO 27001"
                    ForgotPasswordStep.ERROR -> "© 2026 Patient.md • Securitate Garantată"
                    else -> "© 2026 Patient • Toate datele medicale sunt strict protejate și criptate conform GDPR"
                },
                color = Gray400,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ForgotPasswordTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = PatientMarkIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 32.dp, height = 30.36.dp)
            )
            Text(
                text = "Patient",
                color = Color.Unspecified,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.6).sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(
                        0.0f to Blue900,
                        0.5f to Blue500,
                        1.0f to Purple500
                    )
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = ShieldCheckIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 12.dp, height = 14.dp)
            )
            Text(
                text = "Conexiune securizată SSL",
                color = Gray500,
                fontSize = 12.sp
            )
        }
    }
}