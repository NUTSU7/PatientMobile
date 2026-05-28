package com.semanticsoft.patientmobile.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AppShapes
import com.semanticsoft.patientmobile.ui.theme.AccentPurple
import com.semanticsoft.patientmobile.ui.theme.Gray400
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.InputBackground
import com.semanticsoft.patientmobile.ui.theme.LightPurpleBg

@Composable
internal fun ProfileDetailsCard(
    email: String,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(AccentPurple)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppDimens.paddingDefault)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                    Text(
                        text = "Detalii Profil",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                Button(
                    onClick = onChangePasswordClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.buttonHeightDefault),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightPurpleBg,
                        contentColor = AccentPurple
                    ),
                    shape = AppShapes.small
                ) {
                    Text(text = "Schimb\u0103 parola")
                }

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                Text(
                    text = "EMAIL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.inputHeightDefault)
                        .background(InputBackground, RoundedCornerShape(AppDimens.cornerRadiusSmall))
                        .padding(horizontal = AppDimens.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                Text(
                    text = "PAROL\u0102",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.inputHeightDefault)
                        .background(InputBackground, RoundedCornerShape(AppDimens.cornerRadiusSmall))
                        .padding(horizontal = AppDimens.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                    Text(
                        text = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
            }
        }
    }
}
