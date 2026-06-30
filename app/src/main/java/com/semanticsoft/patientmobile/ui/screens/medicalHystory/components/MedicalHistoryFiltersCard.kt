package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing

import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryUiState
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import java.time.LocalDate

private const val CARD_RADIUS = 24

private fun monthLabel(monthValue: Int): String = when (monthValue) {
    1 -> "Ian"
    2 -> "Feb"
    3 -> "Mar"
    4 -> "Apr"
    5 -> "Mai"
    6 -> "Iun"
    7 -> "Iul"
    8 -> "Aug"
    9 -> "Sep"
    10 -> "Oct"
    11 -> "Noi"
    12 -> "Dec"
    else -> "-"
}

@Composable
fun MedicalHistoryFiltersCard(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onDateSelected: (LocalDate) -> Unit,
    onYearSelected: (Int?) -> Unit
) {
    val years = state.availableYears
    val uploadDates = state.observedDatesForSelectedYear

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(CARD_RADIUS.dp))
            .background(Color.White, RoundedCornerShape(CARD_RADIUS.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(CARD_RADIUS.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (state.selectedYear == null) {
                            years.firstOrNull()?.let { onYearSelected(it) }
                        } else {
                            val idx = years.indexOf(state.selectedYear)
                            if (idx < years.lastIndex) onYearSelected(years[idx + 1])
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "An anterior",
                        modifier = Modifier.size(18.dp),
                        tint = Gray900
                    )
                }

                Text(
                    text = state.selectedYear?.toString() ?: "To\u021Bi anii",
                    modifier = Modifier.widthIn(min = 88.dp),
                    textAlign = TextAlign.Center,
                    color = Gray900,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        if (state.selectedYear != null) {
                            val idx = years.indexOf(state.selectedYear)
                            if (idx > 0) {
                                onYearSelected(years[idx - 1])
                            } else {
                                onYearSelected(null)
                            }
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "An urm\u0103tor",
                        modifier = Modifier.size(18.dp),
                        tint = Gray900
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!state.readinessCheckInProgress && uploadDates.isNotEmpty()) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uploadDates.forEach { date ->
                                val isSelected = state.selectedDate == date
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Indigo600 else Color.Transparent)
                                        .clickable { onDateSelected(date) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${date.dayOfMonth} ${monthLabel(date.monthValue)}",
                                        color = if (isSelected) Color.White else Gray900,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    )
            }
        }
    }
}
            }
        }
    }
}

