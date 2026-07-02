package com.semanticsoft.patientmobile.ui.theme

import androidx.compose.ui.graphics.Color
import com.semanticsoft.patientmobile.data.model.IndicatorStatus

val AppBackground = Color(0xFFF4F8FC)
val HeaderBlue = Color(0xFF0E5DA9)
val HeaderBlueDark = Color(0xFF0A3F72)
val PrimaryTeal = Color(0xFF16A2A5)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF102A43)
val TextSecondary = Color(0xFF486581)
val AttentionHigh = Color(0xFFDC4C64)
val AttentionModerate = Color(0xFFF59E0B)
val SuccessGreen = Color(0xFF2B8A3E)

// React design palette
val Indigo200 = Color(0xFFC7D2FE)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)
val Indigo700 = Color(0xFF4338CA)
val Purple500 = Color(0xFF8B5CF6)
val Purple400 = Color(0xFFA78BFA)
val Purple600 = Color(0xFF7C3AED)
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF3F4F6)
val Gray200 = Color(0xFFE5E7EB)
val Gray400 = Color(0xFF9CA3AF)
val Gray500 = Color(0xFF6B7280)
val Gray900 = Color(0xFF111827)

val InputBackground = Color(0xFFEFF4FA)
val ErrorLightBg = Color(0xFFFEE2E2)
val ErrorText = Color(0xFFB91C1C)
val LightPurpleBg = Color(0xFFF5F3FF)
val AccentPurple = Color(0xFF6366F1)

// Light gray colors for UI cards and backgrounds
val GrayLightBg = Color(0xFFF3F4F6)
val GrayLightBorder = Color(0xFFE5E7EB)

// Medical result color-coded values
val ResultHigh = Color(0xFFDC2626)
val ResultLow = Color(0xFFEA580C)
val ResultNormal = Color(0xFF16A34A)

// Result badge backgrounds
val PastelGreenBg = Color(0xFFD1FAE5)
val PastelOrangeBg = Color(0xFFFFEDD5)
val SoftIndigoGrey = Color(0xFF374151)

// Status colors (matching web dashboard donut + chips)
val StatusNormalFill = Color(0xFF22C55E)
val StatusNormalBg = Color(0xFFDCFCE7)
val StatusNormalText = Color(0xFF16A34A)
val StatusBorderlineFill = Color(0xFFF59E0B)
val StatusBorderlineBg = Color(0xFFFEF3C7)
val StatusBorderlineText = Color(0xFFD97706)
val StatusAttentionFill = Color(0xFFEF4444)
val StatusAttentionBg = Color(0xFFFEE2E2)
val StatusAttentionText = Color(0xFFDC2626)
val StatusNoReferenceFill = Color(0xFF94A3B8)
val StatusNoReferenceBg = Color(0xFFE2E8F0)
val StatusNoReferenceText = Color(0xFF64748B)

fun statusFillColor(status: IndicatorStatus): Color = when (status) {
    IndicatorStatus.NORMAL -> StatusNormalFill
    IndicatorStatus.BORDERLINE -> StatusBorderlineFill
    IndicatorStatus.ATTENTION -> StatusAttentionFill
    IndicatorStatus.NO_REFERENCE -> StatusNoReferenceFill
}

fun statusChipBg(status: IndicatorStatus): Color = when (status) {
    IndicatorStatus.NORMAL -> StatusNormalBg
    IndicatorStatus.BORDERLINE -> StatusBorderlineBg
    IndicatorStatus.ATTENTION -> StatusAttentionBg
    IndicatorStatus.NO_REFERENCE -> StatusNoReferenceBg
}

fun statusChipText(status: IndicatorStatus): Color = when (status) {
    IndicatorStatus.NORMAL -> StatusNormalText
    IndicatorStatus.BORDERLINE -> StatusBorderlineText
    IndicatorStatus.ATTENTION -> StatusAttentionText
    IndicatorStatus.NO_REFERENCE -> StatusNoReferenceText
}
