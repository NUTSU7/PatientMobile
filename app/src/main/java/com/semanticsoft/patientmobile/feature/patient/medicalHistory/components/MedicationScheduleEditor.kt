package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.core.ui.theme.Gray50
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.feature.patient.medicalHistory.MealRelation
import com.semanticsoft.patientmobile.feature.patient.medicalHistory.MedicationSchedule

/**
 * Compact administration hours editor using the original Outlined Indigo design.
 * Displays hours as chips fitting 2-3 per row with an inline "Adaugă oră" button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MedicationScheduleEditor(
    schedules: List<MedicationSchedule>,
    onSchedulesChange: (List<MedicationSchedule>) -> Unit,
    onPickTime: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chipShape = RoundedCornerShape(AppDimens.cornerRadiusSmall)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(chipShape)
            .background(Gray50)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "Adaug\u0103 una sau mai multe ore zilnice \u00EEn format 24h. Este op\u021Bional.",
                color = Gray500,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                schedules.forEachIndexed { index, schedule ->
                    Row(
                        modifier = Modifier
                            .clip(chipShape)
                            .background(Color.White)
                            .border(BorderStroke(1.dp, Indigo600), chipShape)
                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = { onPickTime(index) }
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Indigo600,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = schedule.administrationTime,
                                color = Indigo600,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = {
                                        onSchedulesChange(schedules.filterIndexed { i, _ -> i != index })
                                    }
                                )
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Elimin\u0103 ora ${schedule.administrationTime}",
                                tint = AttentionHigh,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Inline "Adaugă oră" chip (single '+' icon, text without extra '+')
                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .background(Color.White)
                        .border(BorderStroke(1.dp, Indigo600), chipShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                onSchedulesChange(
                                    schedules + MedicationSchedule(
                                        administrationTime = nextFreeTime(schedules),
                                        mealRelation = MealRelation.NO_MEAL_RELATION
                                    )
                                )
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Adaug\u0103 or\u0103",
                        color = Indigo600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/** First full hour (starting at 08:00) that is not already used, so new rows never collide. */
private fun nextFreeTime(schedules: List<MedicationSchedule>): String {
    val used = schedules.map { it.administrationTime }.toSet()
    val hours = (8..23) + (0..7)
    return hours.map { "${it.toString().padStart(2, '0')}:00" }.firstOrNull { it !in used } ?: "08:00"
}
