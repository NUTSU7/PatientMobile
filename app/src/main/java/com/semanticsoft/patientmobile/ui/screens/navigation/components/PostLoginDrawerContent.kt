package com.semanticsoft.patientmobile.ui.screens.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.R
import com.semanticsoft.patientmobile.ui.screens.navigation.PostLoginTab
import com.semanticsoft.patientmobile.ui.theme.icons.SidebarPulseIcon

@Composable
internal fun PostLoginDrawerContent(
    fullName: String,
    role: String,
    profilePhotoResId: Int?,
    selectedTab: PostLoginTab,
    scale: Float,
    onClose: () -> Unit,
    onSelectDashboard: () -> Unit,
    onSelectHistory: () -> Unit,
    onSelectUploadedAnalyses: () -> Unit,
    onLogout: () -> Unit
) {
    val initials = remember(fullName) {
        fullName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "AP" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = (16f * scale).dp, vertical = (18f * scale).dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size((36f * scale).dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape((10f * scale).dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SidebarPulseIcon,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size((20f * scale).dp)
                    )
                }

                Spacer(modifier = Modifier.width((12f * scale).dp))

                Text(
                    text = "Patient.md",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (24f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.2).sp
                    )
                )
            }

            IconButton(onClick = onClose, modifier = Modifier.size((24f * scale).dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "\u00CEnchide meniu",
                    tint = Color.White,
                    modifier = Modifier.size((24f * scale).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height((26f * scale).dp))

        DrawerMenuItem(
            label = "Panou principal",
            selected = selectedTab == PostLoginTab.Dashboard,
            scale = scale,
            activeIcon = Icons.Outlined.GridView,
            onClick = onSelectDashboard
        )

        Spacer(modifier = Modifier.height((8f * scale).dp))

        DrawerMenuItem(
            label = "Analize \u00EEnc\u0103rcate",
            selected = selectedTab == PostLoginTab.UploadedAnalyses,
            scale = scale,
            activeIcon = Icons.Outlined.Description,
            onClick = onSelectUploadedAnalyses
        )

        Spacer(modifier = Modifier.height((8f * scale).dp))

        DrawerMenuItem(
            label = "Istoric medical",
            selected = selectedTab == PostLoginTab.MedicalHystory,
            scale = scale,
            activeIcon = Icons.Outlined.Description,
            onClick = onSelectHistory
        )

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height((76f * scale).dp),
            color = Color.White.copy(alpha = 0.12f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape((14f * scale).dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = (14f * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size((40f * scale).dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(0xFF5A52E5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoResId != null) {
                        Image(
                            painter = painterResource(id = profilePhotoResId),
                            contentDescription = "Fotografie profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = (11.9f * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width((12f * scale).dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName.split(" ").firstOrNull().orEmpty().ifBlank { "Diana" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (11.9f * scale).sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = role,
                        color = Color(0xFFA5B4FC),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (10.2f * scale).sp
                        )
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size((32f * scale).dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Mai multe op\u021Biuni",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size((20f * scale).dp)
                    )
                }
            }
        }
    }
}
