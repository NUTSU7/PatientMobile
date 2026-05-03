package com.semanticsoft.patientmobile.ui.screens.auth.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.theme.icons.AppLogoIcon

@Composable
fun AuthTopHeader(
    sidePadding: Dp,
    headerHeight: Dp = 320.dp,
    isCompact: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF9333EA))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = sidePadding)
                .padding(
                    top = if (isCompact) 12.dp else 16.dp,
                    bottom = if (isCompact) 14.dp else 20.dp
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AppLogoIcon,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(if (isCompact) 48.dp else 56.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Patient.md",
                    color = Color.White,
                    fontSize = if (isCompact) 22.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 14.dp else 18.dp))
            Text(
                text = "Patient.md este un portal care te poate ajuta să ai grijă de sănătatea ta.",
                color = Color.White,
                fontSize = if (isCompact) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (isCompact) 22.sp else 27.sp
            )

            Spacer(modifier = Modifier.height(if (isCompact) 10.dp else 14.dp))
            Text(
                text = "Acum poți:",
                color = Color.White,
                fontSize = if (isCompact) 15.sp else 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 7.dp)) {
                BulletItem("Încarcă analizele tale medicale", isCompact)
                BulletItem("Vizualiza analizele încărcate de tine", isCompact)
                BulletItem("Păstra și monitoriza analizele", isCompact)
            }

            Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .height(if (isCompact) 30.dp else 34.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Toate datele încărcate sunt criptate",
                        color = Color.White,
                        fontSize = if (isCompact) 12.sp else 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletItem(text: String, compact: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 7.dp else 8.dp)
                .background(Color.White, CircleShape)
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = if (compact) 13.sp else 14.sp
        )
    }
}
