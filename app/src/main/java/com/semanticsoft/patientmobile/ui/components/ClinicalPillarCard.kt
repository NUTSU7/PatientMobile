package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.R
import com.semanticsoft.patientmobile.data.model.ClinicalPillarAlert
import com.semanticsoft.patientmobile.data.model.ClinicalPillarCardItem
import com.semanticsoft.patientmobile.data.model.ClinicalPillarType
import kotlin.math.max
import kotlin.math.min

@Composable
fun ClinicalPillarCard(
    item: ClinicalPillarCardItem,
    modifier: Modifier = Modifier
) {
    val config = clinicalPillarConfig(item.type)

    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.86f, min(maxWidth.value / 163.6f, 1.08f))
        val cardHeight = (if (config.isTall) 153.6f else 133.6f) * scale
        val titleHeight = (if (config.title.contains("\n")) 40f else 20f) * scale

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight.dp),
            shape = RoundedCornerShape((12f * scale).dp),
            colors = cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke((1f * scale).dp, Color(0xFFE5E7EB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (16f * scale).dp, vertical = (16.8f * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size((48f * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((48f * scale).dp)
                            .background(Color(0xFFEEF2FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = config.iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size((24f * scale).dp)
                        )
                    }

                    if (config.alert != ClinicalPillarAlert.NONE) {
                        val dotColor = if (config.alert == ClinicalPillarAlert.GOOD) Color(0xFF22C55E) else Color(0xFFEF4444)
                        Box(
                            modifier = Modifier
                                .size((12f * scale).dp)
                                .offset(x = (20f * scale).dp, y = (-4f * scale).dp)
                                .border((1f * scale).dp, Color.White, CircleShape)
                                .background(dotColor, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height((12f * scale).dp))

                Text(
                    text = config.title,
                    color = Color(0xFF111827),
                    fontSize = (11.9f * scale).sp,
                    lineHeight = (20f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(titleHeight.dp)
                )

                Spacer(modifier = Modifier.height((4f * scale).dp))

                Text(
                    text = "${item.reportCount} analize",
                    color = Color(0xFF6B7280),
                    fontSize = (10.2f * scale).sp,
                    lineHeight = (16f * scale).sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class ClinicalPillarConfig(
    val title: String,
    val iconResId: Int,
    val alert: ClinicalPillarAlert,
    val isTall: Boolean
)

private fun clinicalPillarConfig(type: ClinicalPillarType): ClinicalPillarConfig {
    return when (type) {
        ClinicalPillarType.BLOOD_CELLS -> ClinicalPillarConfig(
            title = "Sânge & Celule",
            iconResId = R.drawable.ic_clinical_pillar_blood_cells,
            alert = ClinicalPillarAlert.GOOD,
            isTall = true
        )

        ClinicalPillarType.ORGANS_METABOLISM -> ClinicalPillarConfig(
            title = "Organe &\nMetabolism",
            iconResId = R.drawable.ic_clinical_pillar_organs_metabolism,
            alert = ClinicalPillarAlert.ATTENTION,
            isTall = true
        )

        ClinicalPillarType.HEART_CV -> ClinicalPillarConfig(
            title = "Inimă & CV",
            iconResId = R.drawable.ic_clinical_pillar_heart_cv,
            alert = ClinicalPillarAlert.ATTENTION,
            isTall = false
        )

        ClinicalPillarType.HORMONES -> ClinicalPillarConfig(
            title = "Hormoni",
            iconResId = R.drawable.ic_clinical_pillar_hormones,
            alert = ClinicalPillarAlert.ATTENTION,
            isTall = false
        )

        ClinicalPillarType.ONCOLOGY_MARKERS -> ClinicalPillarConfig(
            title = "Markeri oncologici",
            iconResId = R.drawable.ic_clinical_pillar_oncology,
            alert = ClinicalPillarAlert.NONE,
            isTall = false
        )

        ClinicalPillarType.NUTRITION_VITAMINS -> ClinicalPillarConfig(
            title = "Nutriție & Vitamine",
            iconResId = R.drawable.ic_clinical_pillar_nutrition,
            alert = ClinicalPillarAlert.ATTENTION,
            isTall = false
        )

        ClinicalPillarType.COAGULATION -> ClinicalPillarConfig(
            title = "Coagulare",
            iconResId = R.drawable.ic_clinical_pillar_coagulation,
            alert = ClinicalPillarAlert.NONE,
            isTall = true
        )

        ClinicalPillarType.INFECTIONS_IMMUNOLOGY -> ClinicalPillarConfig(
            title = "Infecții &\nImunologie",
            iconResId = R.drawable.ic_clinical_pillar_infections,
            alert = ClinicalPillarAlert.GOOD,
            isTall = true
        )
    }
}
