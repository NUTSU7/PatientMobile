package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryUiState
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600

private val SEASONS = listOf(
    listOf("Dec", "Ian", "Feb"),
    listOf("Mar", "Apr", "Mai"),
    listOf("Iun", "Iul", "Aug"),
    listOf("Sep", "Oct", "Noi")
)

private val MONTH_INDICES = listOf(
    listOf(12, 1, 2),
    listOf(3, 4, 5),
    listOf(6, 7, 8),
    listOf(9, 10, 11)
)

private const val CARD_RADIUS = 24

@Composable
fun MedicalHistoryFiltersCard(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onTimelineValueSelected: (Int?) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    val years = state.availableYears.ifEmpty { listOf(state.selectedYear) }
    val availableMonths = state.timelineValuesForSelectedYear.toSet()

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
                        val idx = years.indexOf(state.selectedYear)
                        if (idx < years.lastIndex) onYearSelected(years[idx + 1])
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
                    text = state.selectedYear.toString(),
                    color = Gray900,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        val idx = years.indexOf(state.selectedYear)
                        if (idx > 0) onYearSelected(years[idx - 1])
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
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MONTH_INDICES.forEachIndexed { seasonIndex, seasonMonths ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SEASONS[seasonIndex].forEachIndexed { monthIndex, monthName ->
                            val monthValue = seasonMonths[monthIndex]
                            val hasData = availableMonths.contains(monthValue)
                            val isSelected = state.selectedTimelineValue == monthValue

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSelected -> Indigo600
                                            else -> Color.Transparent
                                        }
                                    )
                                    .then(
                                        if (hasData) Modifier.clickable {
                                            onTimelineValueSelected(monthValue)
                                        } else Modifier
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthName,
                                    color = when {
                                        isSelected -> Color.White
                                        hasData -> Gray900
                                        else -> Gray500.copy(alpha = 0.5f)
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = when {
                                        isSelected -> FontWeight.SemiBold
                                        hasData -> FontWeight.Medium
                                        else -> FontWeight.Normal
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
