package com.semanticsoft.patientmobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val AppShapes = Shapes(
    small = RoundedCornerShape(AppDimens.cornerRadiusSmall),
    medium = RoundedCornerShape(AppDimens.cornerRadiusMedium),
    large = RoundedCornerShape(AppDimens.cornerRadiusLarge),
    extraLarge = RoundedCornerShape(AppDimens.cornerRadiusPill)
)
