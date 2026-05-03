package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.vector.PathBuilder

fun PathBuilder.addPathData(pathData: String) {
    val regex = Regex("([MLCZmlcz])([^MLCZmlcz]*)")
    regex.findAll(pathData).forEach { match ->
        val cmd = match.groupValues[1][0].uppercaseChar()
        val nums = match.groupValues[2].trim()
            .replace(",", " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.toFloat() }

        when (cmd) {
            'M' -> moveTo(nums[0], nums[1])
            'L' -> lineTo(nums[0], nums[1])
            'C' -> curveTo(nums[0], nums[1], nums[2], nums[3], nums[4], nums[5])
            'Z' -> close()
        }
    }
}
