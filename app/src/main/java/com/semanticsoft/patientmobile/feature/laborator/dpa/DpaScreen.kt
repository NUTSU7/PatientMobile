package com.semanticsoft.patientmobile.feature.laborator.dpa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.core.ui.components.auth.AuthScreenTopBar
import com.semanticsoft.patientmobile.core.ui.components.auth.GradientPillButton
import com.semanticsoft.patientmobile.core.ui.theme.AppBackground
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Gray600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.icons.DownloadIcon
import com.semanticsoft.patientmobile.feature.laborator.dpa.components.DpaConsentRow
import com.semanticsoft.patientmobile.feature.laborator.dpa.components.DpaDocumentCard
import com.semanticsoft.patientmobile.feature.laborator.dpa.components.PrinciplesCard
import com.semanticsoft.patientmobile.feature.laborator.dpa.components.SecurityFactsCards
import com.semanticsoft.patientmobile.feature.laborator.dpa.components.SecurityGdprBadge

@Composable
fun DpaScreen(
    state: DpaUiState,
    onAcceptedChange: (Boolean) -> Unit,
    onContinueClick: () -> Unit,
    onDownloadPdfClick: () -> Unit
) {
    SetStatusBar(color = AppBackground, darkIcons = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = AppDimens.paddingLarge, vertical = AppDimens.paddingDefault)
    ) {
        AuthScreenTopBar(trailing = { SecurityGdprBadge() })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = AppDimens.paddingXLarge, bottom = AppDimens.paddingDefault),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingXLarge)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppDimens.gapMedium)
                ) {
                    Text(
                        text = "Acord de procesare a datelor (DPA)",
                        color = Gray900,
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.7).sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Înainte de a accesa portalul, te rugăm să confirmi înțelegerea rolurilor privind datele pacienților.",
                        color = Gray600,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

                PrinciplesCard()

                DpaDocumentCard()

                SecurityFactsCards()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.paddingDefault)
                ) {
                    DpaConsentRow(
                        checked = state.accepted,
                        onCheckedChange = onAcceptedChange
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimens.paddingDefault)
                    ) {
                        GradientPillButton(
                            text = "Continuă către portal",
                            onClick = onContinueClick,
                            enabled = state.accepted,
                            fontSize = 13.sp
                        )
                        DownloadPdfLink(onClick = onDownloadPdfClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadPdfLink(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = DownloadIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(width = 10.66.dp, height = 10.66.dp)
        )
        Text(
            text = "Descarcă Acordul în format PDF",
            color = Indigo600,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
