package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyUploadCard(
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isCompact = maxWidth < 360.dp
        val isVeryCompact = maxWidth < 330.dp

        val cardPadding = when {
            isVeryCompact -> 16.dp
            isCompact -> 18.dp
            else -> 20.dp
        }
        val titleFontSize = when {
            isVeryCompact -> 16.sp
            isCompact -> 18.sp
            else -> 20.sp
        }
        val titleLineHeight = when {
            isVeryCompact -> 26.sp
            isCompact -> 28.sp
            else -> 30.sp
        }
        val bodyFontSize = if (isCompact) 14.sp else 15.sp
        val bodyLineHeight = if (isCompact) 20.sp else 22.sp
        val buttonHeight = if (isCompact) 46.dp else 48.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                .padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 40.dp else 48.dp)
                    .border(2.dp, Color(0xFF8B5CF6), RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(if (isCompact) 24.dp else 28.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 20.dp))
            Text(
                text = "Începe prin a-ți încărca prima analiză\nde laborator",
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.Bold,
                fontSize = titleFontSize,
                lineHeight = titleLineHeight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 20.dp))
            Text(
                text = "Încarcă un fișier PDF, JPG sau PNG și AI-ul nostru va extrage automat datele pentru a-ți oferi o imagine clară asupra sănătății tale.",
                color = Color(0xFF6B7280),
                fontSize = bodyFontSize,
                lineHeight = bodyLineHeight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isCompact) 20.dp else 24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(buttonHeight)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onUploadClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Încarcă analize",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (isCompact) 15.sp else 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 20.dp else 24.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🔒 Date securizate", color = Color(0xFF6B7280), fontSize = if (isCompact) 13.sp else 14.sp)
                Text(text = "🤖 Extracție prin AI", color = Color(0xFF6B7280), fontSize = if (isCompact) 13.sp else 14.sp)
                Text(text = "📊 Istoric vizual", color = Color(0xFF6B7280), fontSize = if (isCompact) 13.sp else 14.sp)
            }

            Spacer(modifier = Modifier.height(if (isCompact) 18.dp else 22.dp))
            Text(
                text = "Nu ai documente la îndemână? Utilizează aplicația când ești gata.",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
