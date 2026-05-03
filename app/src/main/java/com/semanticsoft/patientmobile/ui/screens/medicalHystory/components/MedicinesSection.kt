package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.components.FilePickerButton
import com.semanticsoft.patientmobile.ui.common.DashboardSpacing
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicalHystoryUiState
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.MedicineItem
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple500
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen
import com.semanticsoft.patientmobile.ui.theme.TextSecondary

@Composable
fun MedicinesSection(
    state: MedicalHystoryUiState,
    spacing: DashboardSpacing,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.markerHeaderGap)
        ) {
            PillIcon(isVertical = true)
            Text(
                text = "Medicamente",
                color = Gray900,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        FilePickerButton(
            label = "+ Adaugă medicament",
            onClick = onAddClick,
            buttonHeight = 44.dp,
            cornerRadius = 999.dp,
            textSize = 13.sp,
            backgroundBrush = Brush.horizontalGradient(listOf(Indigo600, Purple500))
        )

        if (state.medicines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gray50, RoundedCornerShape(18.dp))
                    .padding(spacing.sectionGap),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nu există medicamente adăugate.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.listItemGap)) {
                state.medicines.forEach { medicine ->
                    MedicineItemRow(item = medicine)
                }
            }
        }
    }
}

@Composable
fun MedicineItemRow(item: MedicineItem) {
    val badgeType = resolveMedicineBadgeType(item)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicineBadge(type = badgeType)

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
                    text = item.schedule,
                    color = Gray500,
                    fontSize = 14.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
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
                        text = "${item.daysRemaining} zile rămase",
                        color = Gray500,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MedicineBadge(type: MedicineBadgeType) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Indigo600.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val strokeWidth = 1.9f
            val iconColor = Indigo600
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            when (type) {
                MedicineBadgeType.PILL -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.3f, h * 0.15f),
                        size = Size(w * 0.4f, h * 0.7f),
                        cornerRadius = CornerRadius(w * 0.2f, w * 0.2f),
                        style = Stroke(strokeWidth)
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.5f, h * 0.15f),
                        end = Offset(w * 0.5f, h * 0.85f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                MedicineBadgeType.CAPSULE -> {
                    drawCircle(
                        color = iconColor,
                        radius = minOf(w, h) * 0.28f,
                        center = Offset(cx, cy),
                        style = Stroke(strokeWidth)
                    )
                }

                MedicineBadgeType.SPRAY -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.18f, h * 0.34f),
                        size = Size(w * 0.36f, h * 0.44f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(strokeWidth)
                    )
                }

                MedicineBadgeType.SYRUP -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.28f, h * 0.28f),
                        size = Size(w * 0.44f, h * 0.5f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(strokeWidth)
                    )
                }

                MedicineBadgeType.INJECTION -> {
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.24f, h * 0.74f),
                        end = Offset(w * 0.7f, h * 0.28f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                MedicineBadgeType.DROPS -> {
                    val dropPath = Path().apply {
                        moveTo(cx, h * 0.18f)
                        cubicTo(w * 0.72f, h * 0.34f, w * 0.74f, h * 0.56f, cx, h * 0.82f)
                        cubicTo(w * 0.26f, h * 0.56f, w * 0.28f, h * 0.34f, cx, h * 0.18f)
                        close()
                    }
                    drawPath(dropPath, color = iconColor, style = Stroke(strokeWidth))
                }
            }
        }
    }
}

@Composable
fun PillIcon(isVertical: Boolean = false) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 1.8f
        val iconColor = Indigo600
        val w = size.width
        val h = size.height

        if (isVertical) {
            drawRoundRect(
                color = iconColor,
                topLeft = Offset(w * 0.3f, h * 0.15f),
                size = Size(w * 0.4f, h * 0.7f),
                cornerRadius = CornerRadius(w * 0.2f, w * 0.2f),
                style = Stroke(strokeWidth)
            )
            drawLine(
                color = iconColor,
                start = Offset(w * 0.5f, h * 0.15f),
                end = Offset(w * 0.5f, h * 0.85f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        } else {
            drawRoundRect(
                color = iconColor,
                topLeft = Offset(w * 0.12f, h * 0.36f),
                size = Size(w * 0.76f, h * 0.28f),
                cornerRadius = CornerRadius(h * 0.16f, h * 0.16f),
                style = Stroke(strokeWidth)
            )
            drawLine(
                color = iconColor,
                start = Offset(w * 0.5f, h * 0.36f),
                end = Offset(w * 0.5f, h * 0.64f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

enum class MedicineBadgeType {
    PILL,
    CAPSULE,
    SPRAY,
    SYRUP,
    INJECTION,
    DROPS
}

fun resolveMedicineBadgeType(item: MedicineItem): MedicineBadgeType {
    val descriptor = "${item.name} ${item.schedule}".lowercase()
    return when {
        descriptor.contains("spray") || descriptor.contains("inhal") -> MedicineBadgeType.SPRAY
        descriptor.contains("capsul") -> MedicineBadgeType.CAPSULE
        descriptor.contains("sirop") -> MedicineBadgeType.SYRUP
        descriptor.contains("inject") -> MedicineBadgeType.INJECTION
        descriptor.contains("picăt") || descriptor.contains("picat") || descriptor.contains("drops") -> MedicineBadgeType.DROPS
        else -> MedicineBadgeType.PILL
    }
}
