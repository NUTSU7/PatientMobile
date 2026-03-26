package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.data.model.MarkerCategoryItem
import kotlin.math.max
import kotlin.math.min

@Composable
fun MarkerOverviewSection(
    categories: List<MarkerCategoryItem>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.88f, min(maxWidth.value / 343.2f, 1.12f))

        LazyRow(horizontalArrangement = Arrangement.spacedBy((8f * scale).dp)) {
            itemsIndexed(categories) { index, category ->
                MarkerCategoryChip(
                    name = category.name,
                    count = category.count,
                    selected = index == selectedIndex,
                    onClick = { onCategorySelected(index) },
                    scale = scale
                )
            }
        }
    }
}

@Composable
private fun MarkerCategoryChip(
    name: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    scale: Float
) {
    val chipBg = if (selected) Color(0xFF5A52E5) else Color.White
    val textColor = if (selected) Color.White else Color(0xFF4B5563)
    val badgeBg = if (selected) Color.White.copy(alpha = 0.2f) else Color(0xFFF3F4F6)

    Button(
        onClick = onClick,
        shape = RoundedCornerShape((8f * scale).dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = chipBg,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .height((37.6f * scale).dp)
            .widthIn(min = (100f * scale).dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = (16f * scale).dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((8f * scale).dp)
        ) {
            Text(
                text = name,
                color = textColor,
                fontSize = 11.9.sp * scale,
                lineHeight = 20.sp * scale,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape((6f * scale).dp))
                    .background(badgeBg)
                    .padding(horizontal = (7f * scale).dp, vertical = (2f * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = count.toString(),
                    color = textColor,
                    fontSize = 10.2.sp * scale,
                    lineHeight = 16.sp * scale,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
