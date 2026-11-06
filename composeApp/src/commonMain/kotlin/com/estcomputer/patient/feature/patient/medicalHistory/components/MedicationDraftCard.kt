package com.estcomputer.patient.feature.patient.medicalHistory.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estcomputer.patient.designsystem.theme.AppDimens
import com.estcomputer.patient.designsystem.theme.AttentionHigh
import com.estcomputer.patient.designsystem.theme.GrayLightBorder
import com.estcomputer.patient.designsystem.theme.Gray900
import com.estcomputer.patient.designsystem.theme.Indigo600
import com.estcomputer.patient.designsystem.theme.WarningNoteBg
import com.estcomputer.patient.designsystem.theme.WarningNoteBorder
import com.estcomputer.patient.designsystem.theme.WarningNoteText
import com.estcomputer.patient.designsystem.theme.icons.ChevronDownIcon
import com.estcomputer.patient.designsystem.theme.icons.TrashIcon
import com.estcomputer.patient.feature.patient.medicalHistory.DoseUnit
import com.estcomputer.patient.feature.patient.medicalHistory.MealRelation
import com.estcomputer.patient.feature.patient.medicalHistory.MedicationDraft
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private val doseUnitOptions = listOf(DialogDropdownOption(key = "", label = "Necunoscut\u0103")) +
    DoseUnit.entries.map { DialogDropdownOption(key = it.name, label = it.toRoLabel()) }

private val mealRelationOptions = listOf(
    DialogDropdownOption(key = MealRelation.NO_MEAL_RELATION.name, label = "F\u0103r\u0103 leg\u0103tur\u0103 cu masa"),
    DialogDropdownOption(key = MealRelation.BEFORE_MEAL.name, label = "\u00CEnainte de mas\u0103"),
    DialogDropdownOption(key = MealRelation.WITH_MEAL.name, label = "\u00CEn timpul mesei"),
    DialogDropdownOption(key = MealRelation.AFTER_MEAL.name, label = "Dup\u0103 mas\u0103")
)

/**
 * One medication card of the treatment editor (web `.mh-treatment-draft`).
 * Can be collapsed into a compact card or expanded to edit fields (accordion pattern).
 */
@Composable
internal fun MedicationDraftCard(
    index: Int,
    draft: MedicationDraft,
    expanded: Boolean,
    canRemove: Boolean,
    showLateStartNote: Boolean,
    onToggle: () -> Unit,
    onChange: ((MedicationDraft) -> MedicationDraft) -> Unit,
    onRemove: () -> Unit,
    onPickDate: () -> Unit,
    onPickTime: (scheduleIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, GrayLightBorder), shape)
            .padding(AppDimens.paddingDefault)
    ) {
        // --- HEADER ROW (Clickable to toggle accordion) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onToggle
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val title = if (draft.name.isNotBlank()) {
                        "Medicament ${index + 1} \u00B7 ${draft.name}"
                    } else {
                        "Medicament ${index + 1}"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (draft.requiresOcrReview && !draft.reviewed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WarningNoteBg)
                                .border(1.dp, WarningNoteBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "De verificat",
                                color = WarningNoteText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Subtitle preview when collapsed
                if (!expanded) {
                    val subtitleParts = listOfNotNull(
                        draft.doseText.takeIf { it.isNotBlank() }?.let { dose ->
                            listOfNotNull(dose, draft.doseUnit?.toRoLabel()).joinToString(" ")
                        },
                        draft.frequencyText.takeIf { it.isNotBlank() },
                        draft.durationText.takeIf { it.isNotBlank() }?.let { "$it zile" },
                        draft.mealRelation.takeIf { it != MealRelation.NO_MEAL_RELATION }?.toRoLabel()
                    )
                    if (subtitleParts.isNotEmpty()) {
                        Text(
                            text = subtitleParts.joinToString(" \u00B7 "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = ChevronDownIcon,
                        contentDescription = if (expanded) "Restr\u00E2nge" else "Extinde",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(if (expanded) 180f else 0f)
                    )
                }
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = TrashIcon,
                            contentDescription = "Elimin\u0103 medicamentul din tratament",
                            tint = AttentionHigh,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- EXPANDABLE BODY CONTENT ---
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val reviewFields = draft.unconfirmedFields
                if (reviewFields.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                    WarningNote("De verificat: ${reviewFields.joinToString(", ") { medicationFieldLabel(it) }}.")
                }

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                DialogFieldLabel("Denumire")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogTextField(
                    value = draft.name,
                    onValueChange = { value -> onChange { it.copy(name = value) } },
                    placeholder = "Ex: Augmentin, Paracetamol..."
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.gapSmall)) {
                    Column(modifier = Modifier.weight(1f)) {
                        DialogFieldLabel("Doz\u0103 (op\u021Bional)")
                        Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                        DialogTextField(
                            value = draft.doseText,
                            onValueChange = { value ->
                                onChange { it.copy(doseText = value.filter { c -> c.isDigit() || c == '.' || c == ',' }) }
                            },
                            placeholder = "Ex: 1",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        DialogFieldLabel("Unitate")
                        Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                        DialogDropdownField(
                            selectedText = draft.doseUnit?.toRoLabel() ?: "Necunoscut\u0103",
                            placeholder = "Unitate",
                            options = doseUnitOptions,
                            onSelect = { option ->
                                onChange { it.copy(doseUnit = option.key.takeIf { key -> key.isNotEmpty() }?.let(DoseUnit::valueOf)) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                DialogFieldLabel("Instruc\u021Biune administrare (op\u021Bional)")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogTextField(
                    value = draft.frequencyText,
                    onValueChange = { value -> onChange { it.copy(frequencyText = value) } },
                    placeholder = "Ex: de 2 ori pe zi"
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                DialogFieldLabel("Data de \u00EEnceput")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                StartDateField(
                    date = draft.startDate,
                    onClick = onPickDate
                )
                if (showLateStartNote) {
                    Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                    Text(
                        text = "\u00CEnceput la mai mult de 14 zile dup\u0103 analizele asociate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                DialogFieldLabel("Durat\u0103 \u00EEn zile (op\u021Bional)")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogTextField(
                    value = draft.durationText,
                    onValueChange = { value -> onChange { it.copy(durationText = value.filter(Char::isDigit)) } },
                    placeholder = "F\u0103r\u0103 limit\u0103",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                DialogFieldLabel("Ore de administrare (op\u021Bional)")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                MedicationScheduleEditor(
                    schedules = draft.schedules,
                    onSchedulesChange = { schedules -> onChange { it.copy(schedules = schedules) } },
                    onPickTime = onPickTime
                )

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                DialogFieldLabel("Rela\u021Bia cu masa (pentru toate orele)")
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                DialogDropdownField(
                    selectedText = draft.mealRelation.toRoLabel(),
                    placeholder = "Alege rela\u021Bia cu masa",
                    options = mealRelationOptions,
                    onSelect = { option ->
                        onChange { it.copy(mealRelation = MealRelation.valueOf(option.key)) }
                    }
                )

                if (draft.requiresOcrReview) {
                    Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = draft.reviewed,
                            onCheckedChange = { checked -> onChange { it.copy(reviewed = checked) } },
                            colors = CheckboxDefaults.colors(checkedColor = Indigo600)
                        )
                        Text(
                            text = "Am verificat datele acestui medicament. C\u00E2mpurile goale r\u0103m\u00E2n neprecizate.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray900
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun WarningNote(text: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(WarningNoteBg)
            .border(1.dp, WarningNoteBorder, shape)
            .padding(10.dp)
    ) {
        Text(text = text, color = WarningNoteText, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun StartDateField(
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Indigo600),
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Indigo600,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = date.format(dateFormatter),
            color = Indigo600,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun medicationFieldLabel(key: String): String = when (key) {
    "name" -> "denumire"
    "doseValue" -> "doz\u0103"
    "doseUnit" -> "unitate"
    "frequencyText" -> "frecven\u021B\u0103"
    "durationDays" -> "durat\u0103"
    "schedules" -> "ore de administrare"
    "medications" -> "lista medicamentelor"
    "ocrConsensus" -> "recunoa\u0219tere"
    else -> "informa\u021Bii extrase"
}

private fun DoseUnit.toRoLabel(): String = when (this) {
    DoseUnit.TABLET -> "Comprimat"
    DoseUnit.CAPSULE -> "Capsul\u0103"
    DoseUnit.ML -> "ml"
    DoseUnit.DROPS -> "Pic\u0103turi"
}

private fun MealRelation.toRoLabel(): String = when (this) {
    MealRelation.BEFORE_MEAL -> "\u00CEnainte de mas\u0103"
    MealRelation.WITH_MEAL -> "\u00CEn timpul mesei"
    MealRelation.AFTER_MEAL -> "Dup\u0103 mas\u0103"
    MealRelation.NO_MEAL_RELATION -> "F\u0103r\u0103 leg\u0103tur\u0103 cu masa"
}
