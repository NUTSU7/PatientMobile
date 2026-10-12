package com.semanticsoft.patientmobile.core.ui.components.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.PatientMobileTheme
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite

@Preview(showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun FormComponentsPreview() {
    PatientMobileTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FormLabel("Denumire laborator")
                FormInput(
                    value = "",
                    placeholder = "Laboratorul din care faci parte",
                    onValueChange = {}
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FormLabel("Adresa de email")
                FormInput(
                    value = "contact@laborator.md",
                    placeholder = "contact@laborator.md",
                    onValueChange = {}
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FormLabel("Parolă")
                FormInput(
                    value = "",
                    placeholder = "Introdu parola ta",
                    onValueChange = {},
                    isPassword = true
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FormLabel("E-mail instituțional")
                FormInput(
                    value = "",
                    placeholder = "Introdu o adresă validă",
                    onValueChange = {},
                    isError = true,
                    errorMessage = "Introdu o adresă validă"
                )
            }
        }
    }
}
