package com.semanticsoft.patientmobile.ui.screens.analysisExplanation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600

@Composable
fun ExplanationBottomSheetContent(
    explanationText: String,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Explica\u021Bie ",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Gray900,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Analize",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Indigo600,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Text(
            text = "Am tradus rezultatele tale clinice \u00EEntr-un limbaj simplu pentru a te ajuta s\u0103 \u00EEn\u021Belegi starea de s\u0103n\u0103tate.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Gray500),
            fontSize = 14.sp
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)

        Text(
            text = when {
                isLoading && explanationText.isBlank() -> "Se \u00EEncarc\u0103 explica\u021Bia..."
                !isLoading && explanationText.isBlank() -> "Explica\u021Bie indisponibil\u0103 pentru acest document."
                else -> explanationText
            },
            style = MaterialTheme.typography.bodyMedium.copy(color = Gray900),
            fontSize = 14.sp
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)

        Text(
            text = "Explica\u021Bia este generat\u0103 pe baza valorilor extrase din acest document \u0219i poate fi recitit\u0103 ulterior f\u0103r\u0103 regenerare.",
            style = MaterialTheme.typography.labelSmall.copy(
                fontStyle = FontStyle.Italic,
                color = Gray500,
                fontSize = 12.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
