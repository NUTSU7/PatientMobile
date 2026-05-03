package com.semanticsoft.patientmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = HeaderBlue,
    secondary = PrimaryTeal,
    background = AppBackground,
    surface = SurfaceWhite,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = PrimaryTeal,
    secondary = HeaderBlue,
    background = TextPrimary,
    surface = ColorTokens.DarkSurface,
    onPrimary = TextPrimary,
    onSecondary = SurfaceWhite,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite
)

private object ColorTokens {
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF1E293B)
}

@Composable
fun PatientMobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
