package com.semanticsoft.patientmobile.feature.laborator.activateAccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.core.ui.components.auth.AuthScreenTopBar
import com.semanticsoft.patientmobile.core.ui.theme.AppBackground
import com.semanticsoft.patientmobile.feature.laborator.activateAccount.components.ActivateAccountCard

@Composable
fun ActivateAccountScreen(
    state: ActivateAccountUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onActivateClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    SetStatusBar(color = AppBackground, darkIcons = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthScreenTopBar()

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
                ActivateAccountCard(
                    password = state.password,
                    confirmPassword = state.confirmPassword,
                    criteria = state.criteria,
                    termsAccepted = state.termsAccepted,
                    canSubmit = state.canSubmit,
                    isLoading = state.isLoading,
                    onPasswordChange = onPasswordChange,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    onTermsAcceptedChange = onTermsAcceptedChange,
                    onActivateClick = onActivateClick,
                    onTermsClick = onTermsClick,
                    onPrivacyClick = onPrivacyClick
                )
            }
        }
    }
}
