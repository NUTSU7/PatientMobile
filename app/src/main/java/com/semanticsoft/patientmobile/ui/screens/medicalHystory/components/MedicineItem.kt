package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicineIconType
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicineItem
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen
import com.semanticsoft.patientmobile.ui.theme.icons.IconTrash
import com.semanticsoft.patientmobile.ui.theme.icons.PillIcon

@Composable
fun MedicineItemRow(item: MedicineItem, onDeleteClick: () -> Unit, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicineBadge(iconType = item.iconType)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    color = Gray900,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.doseValue} ${item.doseUnitLabel}",
                    color = Gray500,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIV",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "\u00CEnc\u0103 ${item.daysRemaining} zile",
                        color = Gray500,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AttentionHigh.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconTrash,
                    contentDescription = "\u0218terge",
                    tint = AttentionHigh,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun MedicineBadge(@Suppress("UNUSED_PARAMETER") iconType: MedicineIconType) {
    val iconColor = Indigo600
    val iconBg = iconColor.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconBg),
        contentAlignment = Alignment.Center
    ) {
        PillIcon(
            modifier = Modifier.size(20.dp),
            color = iconColor
        )
    }
}
