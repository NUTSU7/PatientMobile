package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryUiState
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900

@Composable
fun MedicalHistoryFiltersCard(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onTimelineValueSelected: (Int?) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    val months = listOf("Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Noi", "Dec")
    val years = if (state.availableYears.isNotEmpty()) state.availableYears else listOf(state.selectedYear)
    var yearMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { yearMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray50,
                    contentColor = Gray900
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.selectedYear.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.widthIn(max = 18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = yearMenuExpanded,
                onDismissRequest = { yearMenuExpanded = false }
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            yearMenuExpanded = false
                            onYearSelected(year)
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
        ) {
            months.forEachIndexed { index, month ->
                val isSelected = state.selectedTimelineValue == index + 1
                Box(
                    modifier = Modifier
                        .widthIn(min = 34.dp)
                        .clickable { onTimelineValueSelected(index + 1) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = month,
                        color = if (isSelected) Gray900 else Gray500,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
