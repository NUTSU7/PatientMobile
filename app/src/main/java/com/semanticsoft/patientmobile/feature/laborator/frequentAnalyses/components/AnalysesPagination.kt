package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.icons.ChevronLeftIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ChevronRightIcon
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FREQUENT_ANALYSES_PAGE_COUNT

@Composable
internal fun AnalysesPagination(
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaginationArrow(
            icon = ChevronLeftIcon,
            enabled = currentPage > 1,
            contentDescription = "Pagina anterioară",
            onClick = onPreviousPage
        )
        for (page in 1..FREQUENT_ANALYSES_PAGE_COUNT) {
            PaginationPage(
                page = page,
                selected = currentPage == page,
                onClick = { onPageSelected(page) }
            )
        }
        PaginationArrow(
            icon = ChevronRightIcon,
            enabled = currentPage < FREQUENT_ANALYSES_PAGE_COUNT,
            contentDescription = "Pagina următoare",
            onClick = onNextPage
        )
    }
}

@Composable
private fun PaginationArrow(
    icon: ImageVector,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Gray500 else Gray300,
            modifier = Modifier
                .width(4.84.dp)
                .height(7.99.dp)
                .alpha(if (enabled) 1f else 0.4f)
        )
    }
}

@Composable
private fun PaginationPage(
    page: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Indigo600 else Color.Transparent)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = page.toString(),
            color = if (selected) Color.White else Gray500,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
