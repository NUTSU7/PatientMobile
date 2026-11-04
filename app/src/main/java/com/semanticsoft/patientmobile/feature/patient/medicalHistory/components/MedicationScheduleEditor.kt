package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.core.ui.theme.Gray50
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.domain.model.patient.MealRelation
import com.semanticsoft.patientmobile.domain.model.patient.MedicationSchedule

private val mealRelationOptions = listOf(
    DialogDropdownOption(MealRelation.BEFORE_MEAL.name, "\u00CEnainte de mas\u0103"),
    DialogDropdownOption(MealRelation.WITH_MEAL.name, "\u00CEn timpul mesei"),
    DialogDropdownOption(MealRelation.AFTER_MEAL.name, "Dup\u0103 mas\u0103"),
    DialogDropdownOption(MealRelation.NO_MEAL_RELATION.name, "F\u0103r\u0103 leg\u0103tur\u0103 cu masa")
)

/** Optional administration hours of one medication ("Ore de administrare" on web). */
@Composable
internal fun MedicationScheduleEditor(
    schedules: List<MedicationSchedule>,
    onSchedulesChange: (List<MedicationSchedule>) -> Unit,
    onPickTime: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.cornerRadiusSmall))
            .background(Gray50)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "Adaug\u0103 una sau mai multe ore zilnice \u00EEn format 24h \u0219i alege rela\u021Bia cu masa. Este op\u021Bional.",
                color = Gray500,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            schedules.forEachIndexed { index, schedule ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onPickTime(index) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Indigo600),
                            border = BorderStroke(1.dp, Indigo600),
                            shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Indigo600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = schedule.administrationTime,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { onSchedulesChange(schedules.filterIndexed { i, _ -> i != index }) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Elimin\u0103",
                                tint = AttentionHigh,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                    DialogCompactDropdownField(
                        selectedText = mealRelationOptions.find { it.key == schedule.mealRelation.name }?.label.orEmpty(),
                        options = mealRelationOptions,
                        onSelect = { option ->
                            val relation = MealRelation.valueOf(option.key)
                            onSchedulesChange(
                                schedules.mapIndexed { i, item ->
                                    if (i == index) item.copy(mealRelation = relation, mealRelationLabel = null) else item
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            }

            TextButton(
                onClick = {
                    onSchedulesChange(
                        schedules + MedicationSchedule(
                            administrationTime = nextFreeTime(schedules),
                            mealRelation = MealRelation.NO_MEAL_RELATION
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Indigo600,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Adaug\u0103 or\u0103",
                    color = Indigo600,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
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
