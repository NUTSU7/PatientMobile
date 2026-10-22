package com.semanticsoft.patientmobile.feature.laborator.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorBorder
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite

@Composable
internal fun LaboratorFilterCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 10.dp,
    verticalSpacing: Dp = 10.dp,
    containerColor: Color = SurfaceWhite,
    showShadow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusXLarge)
    val shadowModifier = if (showShadow) {
        Modifier.shadow(
            elevation = 1.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f)
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(shadowModifier)
            .clip(shape)
            .background(containerColor, shape)
            .border(1.25.dp, LaboratorBorder.copy(alpha = 0.3f), shape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}
