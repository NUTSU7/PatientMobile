package com.semanticsoft.patientmobile.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.semanticsoft.patientmobile.domain.model.patient.IndicatorStatus

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
val Gray300 = Color(0xFFD1D5DB)
val Gray400 = Color(0xFF9CA3AF)
val Gray500 = Color(0xFF6B7280)
val Gray600 = Color(0xFF4B5563)
val Gray900 = Color(0xFF111827)

// Password reset flow palette
val AmberLightBg = Color(0x40FFFBEB)
val Blue500 = Color(0xFF3B82F6)
val Blue900 = Color(0xFF1E3A8A)
val Emerald600 = Color(0xFF059669)
val PurpleLabel = Color(0xFF280557)
val InfoCardBackground = Color(0xFFFAFAFC)

val InputBackground = Color(0xFFEFF4FA)
val InputShadow = Color(0x334A40E0)
val InputHaloShadow = Color(0x33D7D7DB)
val ErrorLightBg = Color(0xFFFEE2E2)
val ErrorText = Color(0xFFB91C1C)
val LightPurpleBg = Color(0xFFF5F3FF)
val AccentPurple = Color(0xFF6366F1)

// Light gray colors for UI cards and backgrounds
val GrayLightBg = Color(0xFFF3F4F6)
val GrayLightBorder = Color(0xFFE5E7EB)

// Laborator onboarding palette (Figma: ActivateAccount / DPA)
val TextTertiary = Color(0xFF586273)
val TermsText = Color(0xFF474554)
val CheckboxBorder = Color(0xFFC8C4D7)
val HelperTextMuted = Color(0xFF787586)
val LaboratorCardTint = Color(0xFFF1F3FF)
val LaboratorBadgeBg = Color(0xFFE9EDFF)
val LaboratorStepIconBg = Color(0xFFDCE2F7)
val LaboratorDocHeaderBg = Color(0xFFE1E8FD)

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

// Donut-specific colors (matching web dashboard palette)
val DonutNormalFill = Color(0xFF62D292)
val DonutBorderlineFill = Color(0xFFFFD275)
val DonutAttentionFill = Color(0xFFFF9F9F)

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
