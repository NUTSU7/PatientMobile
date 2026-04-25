package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.ui.common.SetStatusBar
import com.semanticsoft.patientmobile.ui.common.dashboardSpacing
import com.semanticsoft.patientmobile.ui.theme.AppBackground
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.AttentionModerate
import com.semanticsoft.patientmobile.ui.theme.SuccessGreen
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MedicalHystoryScreen(
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit,
    viewModel: MedicalHystoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    SetStatusBar(color = Color.White, darkIcons = true)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = dashboardSpacing(maxWidth.value)
        val horizontalPadding = spacing.horizontalPadding
        val isCompact = maxWidth < 360.dp
        val sectionGap = spacing.sectionGap

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MedicalHystoryTopBar(
                    horizontalPadding = horizontalPadding,
                    onMenuClick = onMenuClick,
                    onNotificationsClick = onNotificationsClick,
                    onInfoClick = onInfoClick
                )

                val contentScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding)
                        .verticalScroll(contentScrollState)
                        .padding(bottom = spacing.bottomSpacer),
                    verticalArrangement = Arrangement.spacedBy(sectionGap)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    FiltersCard(
                        state = state,
                        onTimelineModeChange = viewModel::onTimelineModeChange,
                        onTimelineValueSelected = viewModel::onTimelineValueSelected,
                        onPreviousYear = viewModel::selectPreviousYear,
                        onNextYear = viewModel::selectNextYear
                    )

                    SectionCard(
                        title = "Analize",
                        rightContent = {
                            AddSmallButton(label = "Adaugă", onClick = { })
                        }
                    ) {
                        when {
                            state.isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF5A52E5), strokeWidth = 2.dp)
                                }
                            }
                            state.filteredDocuments.isEmpty() -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.errorMessage?.let {
                                        Text(
                                            text = it,
                                            color = Color(0xFFB91C1C),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        text = "Nu există analize pentru filtrul selectat.",
                                        color = Color(0xFF6B7280),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            else -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    state.errorMessage?.let {
                                        Text(
                                            text = it,
                                            color = Color(0xFFB91C1C),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    state.filteredDocuments.forEach { document ->
                                        AnalysisItemRow(document = document, isCompact = isCompact)
                                    }
                                }
                            }
                        }
                    }

                    SectionCard(
                        title = "Medicamente",
                        rightContent = {
                            AddSmallButton(label = "Adaugă", onClick = { })
                        }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.medicines.forEach { medicine ->
                                MedicineItemRow(item = medicine, isCompact = isCompact)
                            }
                        }
                    }

                    SectionCard(
                        title = "Notițe personale",
                        rightContent = {
                            AddSmallButton(label = "Adaugă", onClick = { })
                        }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.notes.forEach { note ->
                                PersonalNoteCard(note = note, isCompact = isCompact)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalHystoryTopBar(
    horizontalPadding: androidx.compose.ui.unit.Dp,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val compact = horizontalPadding <= 16.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .height(if (compact) 108.dp else 116.dp)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Meniu",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Istoric medical",
            color = Color(0xFF111827),
            fontSize = if (compact) 18.sp else 20.sp,
            lineHeight = if (compact) 24.sp else 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notificări",
                tint = Color(0xFF111827)
            )
        }

        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Informații",
                tint = Color(0xFF111827)
            )
        }
    }
}

@Composable
private fun FiltersCard(
    state: MedicalHystoryUiState,
    onTimelineModeChange: (MedicalTimelineMode) -> Unit,
    onTimelineValueSelected: (Int?) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit
) {
    var showModeDropdown by remember { mutableStateOf(false) }
    val chipHorizontal = if (state.timelineMode == MedicalTimelineMode.WEEK_DAYS) 12.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousYear, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "An anterior",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = state.selectedYear.toString(),
                    color = Color(0xFF111827),
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextYear, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = "An următor",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Box {
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(10.dp))
                        .clickable { showModeDropdown = true }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.timelineMode == MedicalTimelineMode.MONTHS) "Luni" else "Zile",
                        color = Color(0xFF111827),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showModeDropdown,
                    onDismissRequest = { showModeDropdown = false },
                    offset = DpOffset(0.dp, 2.dp),
                    containerColor = Color.Transparent,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.width(66.dp)) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Luni",
                                        color = Color(0xFF111827),
                                        fontSize = 14.sp,
                                        fontWeight = if (state.timelineMode == MedicalTimelineMode.MONTHS) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    onTimelineModeChange(MedicalTimelineMode.MONTHS)
                                    showModeDropdown = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (state.timelineMode == MedicalTimelineMode.MONTHS) Color(0xFFF5F3FF) else Color.Transparent)
                            )

                            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Zile",
                                        color = Color(0xFF111827),
                                        fontSize = 14.sp,
                                        fontWeight = if (state.timelineMode == MedicalTimelineMode.WEEK_DAYS) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    onTimelineModeChange(MedicalTimelineMode.WEEK_DAYS)
                                    showModeDropdown = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (state.timelineMode == MedicalTimelineMode.WEEK_DAYS) Color(0xFFF5F3FF) else Color.Transparent)
                            )
                        }
                    }
                }
            }
        }

        val timelineValues = state.timelineValuesForSelectedYear
        if (timelineValues.isEmpty()) {
            Text(
                text = "Nu există date pentru anul selectat.",
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timelineValues.forEach { value ->
                    val isSelected = state.selectedTimelineValue == value
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Color(0xFF5A52E5) else Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onTimelineValueSelected(value) }
                            .padding(horizontal = chipHorizontal, vertical = 8.dp)
                    ) {
                        Text(
                            text = timelineLabel(state.timelineMode, value),
                            color = if (isSelected) Color.White else Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    rightContent: (@Composable () -> Unit)?,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (title) {
                    "Analize" -> FileIcon()
                    "Medicamente" -> PillIcon(isVertical = true)
                    "Notițe personale" -> ClipboardIcon()
                }
                Text(
                    text = title,
                    color = Color(0xFF111827),
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            rightContent?.invoke()
        }
        content()
    }
}

@Composable
private fun AnalysisItemRow(
    document: PatientDocument,
    isCompact: Boolean
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ro")) }
    val dateText = document.uploadedAt
        .atZone(ZoneId.systemDefault())
        .format(dateFormatter)
    val displayName = remember(document.originalFileName) {
        val original = document.originalFileName
        val cutIndex = original.lastIndexOf('.')
        if (cutIndex > 0) original.substring(0, cutIndex) else original
    }
    val rowMinHeight = if (isCompact) 84.dp else 92.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowMinHeight)
            .shadow(3.dp, RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = displayName,
                color = Color(0xFF111827),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateText,
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Mai multe opțiuni",
                tint = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun MedicineItemRow(
    item: MedicineItem,
    isCompact: Boolean
) {
    val rowMinHeight = if (isCompact) 96.dp else 104.dp
    val iconType = resolveMedicineVisualType(item)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowMinHeight)
            .shadow(3.dp, RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MedicineTypeBadge(
            type = iconType,
            isCompact = isCompact
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                color = Color(0xFF111827),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.schedule,
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = if (isCompact) 6.dp else 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD1FAE5), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIV",
                        color = Color(0xFF047857),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "încă ${item.daysRemaining} zile",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PersonalNoteCard(
    note: PersonalNoteItem,
    isCompact: Boolean
) {
    val indicatorColor = when (note.severity) {
        NoteSeverity.GOOD -> SuccessGreen
        NoteSeverity.OK -> AttentionModerate
        NoteSeverity.BAD -> AttentionHigh
    }
    val rowMinHeight = if (isCompact) 108.dp else 118.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowMinHeight)
            .height(IntrinsicSize.Min)
            .shadow(3.dp, RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(indicatorColor)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = note.title,
                color = indicatorColor,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = note.content,
                color = Color(0xFF374151),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(vertical = if (isCompact) 8.dp else 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${note.author}",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
                Text(
                    text = note.dateLabel,
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private enum class MedicineVisualType {
    PILL,
    CAPSULE,
    SPRAY,
    SYRUP,
    INJECTION,
    DROPS
}

private fun resolveMedicineVisualType(item: MedicineItem): MedicineVisualType {
    val descriptor = "${item.name} ${item.schedule}".lowercase()
    return when {
        descriptor.contains("spray") || descriptor.contains("inhal") -> MedicineVisualType.SPRAY
        descriptor.contains("capsul") -> MedicineVisualType.CAPSULE
        descriptor.contains("sirop") -> MedicineVisualType.SYRUP
        descriptor.contains("inject") -> MedicineVisualType.INJECTION
        descriptor.contains("picăt") || descriptor.contains("picat") || descriptor.contains("drops") -> MedicineVisualType.DROPS
        else -> MedicineVisualType.PILL
    }
}

@Composable
private fun MedicineTypeBadge(
    type: MedicineVisualType,
    isCompact: Boolean
) {
    val badgeSize = if (isCompact) 34.dp else 36.dp
    val strokeWidth = if (isCompact) 1.9f else 2.1f
    val iconColor = Color(0xFF6366F1)

    Box(
        modifier = Modifier
            .size(badgeSize)
            .background(Color(0xFFDDE2FF), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 7.dp else 8.dp)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            when (type) {
                MedicineVisualType.PILL -> {
                    // Vertical pill with center line (same as the section header icon)
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

                MedicineVisualType.CAPSULE -> {
                    val radius = minOf(w, h) * 0.28f
                    drawCircle(
                        color = iconColor,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(strokeWidth)
                    )
                    drawCircle(
                        color = iconColor,
                        radius = minOf(w, h) * 0.06f,
                        center = Offset(cx + radius * 0.32f, cy - radius * 0.24f)
                    )
                }

                MedicineVisualType.SPRAY -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.18f, h * 0.34f),
                        size = Size(w * 0.36f, h * 0.44f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(strokeWidth)
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.54f, h * 0.42f),
                        end = Offset(w * 0.76f, h * 0.32f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = iconColor, radius = strokeWidth * 0.55f, center = Offset(w * 0.82f, h * 0.26f))
                    drawCircle(color = iconColor, radius = strokeWidth * 0.55f, center = Offset(w * 0.86f, h * 0.34f))
                }

                MedicineVisualType.SYRUP -> {
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.28f, h * 0.28f),
                        size = Size(w * 0.44f, h * 0.50f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(strokeWidth)
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.36f, h * 0.22f),
                        end = Offset(w * 0.64f, h * 0.22f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.40f, h * 0.50f),
                        end = Offset(w * 0.60f, h * 0.50f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                MedicineVisualType.INJECTION -> {
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.24f, h * 0.74f),
                        end = Offset(w * 0.70f, h * 0.28f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawRoundRect(
                        color = iconColor,
                        topLeft = Offset(w * 0.16f, h * 0.62f),
                        size = Size(w * 0.20f, h * 0.14f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
                        style = Stroke(strokeWidth)
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(w * 0.72f, h * 0.26f),
                        end = Offset(w * 0.86f, h * 0.12f),
                        strokeWidth = strokeWidth * 0.8f,
                        cap = StrokeCap.Round
                    )
                }

                MedicineVisualType.DROPS -> {
                    val dropPath = Path().apply {
                        moveTo(cx, h * 0.18f)
                        cubicTo(w * 0.72f, h * 0.34f, w * 0.74f, h * 0.56f, cx, h * 0.82f)
                        cubicTo(w * 0.26f, h * 0.56f, w * 0.28f, h * 0.34f, cx, h * 0.18f)
                        close()
                    }
                    drawPath(
                        path = dropPath,
                        color = iconColor,
                        style = Stroke(strokeWidth)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSmallButton(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .background(Color(0xFF5A52E5), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FileIcon() {
    val iconColor = Color(0xFF8B5CF6) // Purple
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8f

        // File outline
        drawRoundRect(
            color = iconColor,
            topLeft = Offset(w * 0.15f, h * 0.1f),
            size = Size(w * 0.7f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(strokeWidth)
        )

        // Folded corner
        val path = Path().apply {
            moveTo(w * 0.6f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.3f)
            lineTo(w * 0.6f, h * 0.3f)
            close()
        }
        drawPath(path = path, color = iconColor, style = Stroke(strokeWidth))
    }
}

@Composable
private fun PillIcon(isVertical: Boolean = false) {
    val iconColor = Color(0xFF8B5CF6) // Purple
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8f

        if (isVertical) {
            // Vertical pill with center line
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
            // Horizontal pill (original)
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

@Composable
private fun ClipboardIcon() {
    val iconColor = Color(0xFF8B5CF6) // Purple
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8f

        // Clipboard outline
        drawRoundRect(
            color = iconColor,
            topLeft = Offset(w * 0.15f, h * 0.2f),
            size = Size(w * 0.7f, h * 0.7f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
            style = Stroke(strokeWidth)
        )

        // Clip
        drawRoundRect(
            color = iconColor,
            topLeft = Offset(w * 0.3f, h * 0.08f),
            size = Size(w * 0.4f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
            style = Stroke(strokeWidth)
        )

        // Lines
        repeat(3) { i ->
            val y = h * (0.3f + i * 0.15f)
            drawLine(
                color = iconColor,
                start = Offset(w * 0.25f, y),
                end = Offset(w * 0.75f, y),
                strokeWidth = strokeWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }
    }
}
