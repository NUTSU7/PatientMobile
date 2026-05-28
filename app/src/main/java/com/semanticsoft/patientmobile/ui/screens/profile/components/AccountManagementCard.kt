package com.semanticsoft.patientmobile.ui.screens.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AppShapes
import com.semanticsoft.patientmobile.ui.theme.icons.IconExport
import com.semanticsoft.patientmobile.ui.theme.icons.IconGrid
import com.semanticsoft.patientmobile.ui.theme.icons.IconTrash

@Composable
internal fun AccountManagementCard(
    onExportDataClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.paddingDefault)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconGrid,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                Text(
                    text = "Gestionare Cont",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            Text(
                text = "Administreaz\u0103 datele tale \u0219i preferin\u021Bele de acces.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))
            OutlinedButton(
                onClick = onExportDataClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.buttonHeightDefault),
                shape = AppShapes.small,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = IconExport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                Text(text = "Export\u0103 toate datele")
            }
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))
            OutlinedButton(
                onClick = onDeleteAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.buttonHeightDefault),
                shape = AppShapes.small,
                border = BorderStroke(1.dp, Color(0xFFF87171)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFF87171)
                )
            ) {
                Icon(
                    imageVector = IconTrash,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                Text(text = "\u0218terge contul")
            }
        }
    }
}
