package com.semanticsoft.patientmobile.feature.laborator.activateAccount.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.CheckboxBorder
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.TermsText

@Composable
internal fun TermsCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .toggleable(
                    value = checked,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        if (checked) Indigo600 else SurfaceWhite,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        if (checked) Indigo600 else CheckboxBorder,
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = buildAnnotatedString {
                append("Am citit și accept ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "terms",
                        styles = TextLinkStyles(style = SpanStyle(color = Indigo600)),
                        linkInteractionListener = { onTermsClick() }
                    )
                ) {
                    append("Termenii contractuali")
                }
                append(" și ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "privacy",
                        styles = TextLinkStyles(style = SpanStyle(color = Indigo600)),
                        linkInteractionListener = { onPrivacyClick() }
                    )
                ) {
                    append("Politica de confidențialitate")
                }
            },
            color = TermsText,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
