package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorAnalysisSubtitle
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDivider
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorInsufficientText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSurfaceSubtle
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.TextTertiary
import com.semanticsoft.patientmobile.core.ui.theme.icons.ShieldAlertIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.SyncIcon
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FREQUENT_ANALYSES_PAGE_SIZE
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FrequentAnalysesSort
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FrequentAnalysesUiState

@Composable
internal fun FrequentAnalysesListCard(
    state: FrequentAnalysesUiState,
    onSortSelected: (FrequentAnalysesSort) -> Unit,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.03f),
                spotColor = Color.Black.copy(alpha = 0.03f)
            )
            .clip(shape)
            .background(SurfaceWhite)
            .border(1.25.dp, LaboratorDivider, shape)
    ) {
        ListHeader(totalCount = state.totalCount)
        HorizontalDivider(color = LaboratorDivider, thickness = 1.25.dp)

        SortBar(
            selectedSort = state.sort,
            onSortSelected = onSortSelected
        )
        HorizontalDivider(color = LaboratorDivider, thickness = 1.25.dp)

        if (state.analyses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nu s-au găsit analize pentru filtrele selectate.",
                    color = LaboratorAnalysisSubtitle,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            state.analyses.forEachIndexed { index, analysis ->
                FrequentAnalysisRow(analysis = analysis)
                if (index < state.analyses.lastIndex) {
                    HorizontalDivider(color = LaboratorDivider, thickness = 1.25.dp)
                }
            }
        }

        ListFooter(
            state = state,
            onPageSelected = onPageSelected,
            onPreviousPage = onPreviousPage,
            onNextPage = onNextPage
        )
    }
}

@Composable
private fun ListHeader(totalCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Toate analizele",
                    color = Color.Black,
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rezultatele și pacienții unici pentru analizele accesate în perioada selectată",
                    color = TextTertiary,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            AnalysisCountBadge(count = totalCount)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = SyncIcon,
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(8.4.dp)
            )
            Text(
                text = "Actualizat automat",
                color = Gray500,
                fontSize = 11.sp,
                lineHeight = 16.5.sp
            )
        }
    }
}

@Composable
private fun AnalysisCountBadge(count: Int) {
    Box(
        modifier = Modifier
            .background(LaboratorPanelBg, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = analysisCountLabel(count),
            color = TextTertiary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SortBar(
    selectedSort: FrequentAnalysesSort,
    onSortSelected: (FrequentAnalysesSort) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LaboratorSurfaceSubtle.copy(alpha = 0.75f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SORTEAZĂ DUPĂ",
            color = Gray900,
            fontSize = 11.sp,
            lineHeight = 16.5.sp,
            letterSpacing = 0.06.sp
        )
        AnalysisSortControl(
            selectedSort = selectedSort,
            onSortSelected = onSortSelected
        )
    }
}

@Composable
private fun ListFooter(
    state: FrequentAnalysesUiState,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LaboratorDashboardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rangeLabel(state),
                color = TextTertiary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            AnalysesPagination(
                currentPage = state.currentPage,
                onPageSelected = onPageSelected,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = LaboratorDivider, thickness = 1.25.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = ShieldAlertIcon,
                contentDescription = null,
                tint = LaboratorInsufficientText,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(10.37.dp)
                    .height(12.75.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("Notă de confidențialitate:")
                    }
                    append(
                        " Conform normelor de protecție statistică a datelor agregate, " +
                            "cohortele sub pragul de raportare sunt anonimizate automat."
                    )
                },
                color = TextTertiary,
                fontSize = 11.sp,
                lineHeight = 17.88.sp
            )
        }
    }
}

private fun rangeLabel(state: FrequentAnalysesUiState): String {
    if (state.analyses.isEmpty()) return "0 rezultate"

    val start = if (state.query.isBlank()) {
        (state.currentPage - 1) * FREQUENT_ANALYSES_PAGE_SIZE + 1
    } else {
        1
    }
    val end = start + state.analyses.size - 1
    val totalLabel = if (state.totalCount == 1) "analiză" else "analize"
    return "$start–$end din ${state.totalCount} $totalLabel"
}

private fun analysisCountLabel(count: Int): String {
    return if (count == 1) "1 analiză" else "$count analize"
}
