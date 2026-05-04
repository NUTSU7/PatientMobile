package com.semanticsoft.patientmobile.ui.theme.icons

import androidx.compose.ui.graphics.vector.PathBuilder

@Suppress("ComplexMethod")
fun PathBuilder.addPathData(pathData: String) {
    val regex = Regex("([MLHVCSQTAZmlhvcsqtaz])([^MLHVCSQTAZmlhvcsqtaz]*)")
    regex.findAll(pathData).forEach { match ->
        val cmdChar = match.groupValues[1][0]
        val isRelative = cmdChar.isLowerCase()
        val cmd = cmdChar.uppercaseChar()
        val nums = match.groupValues[2].trim()
            .replace(",", " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.toFloat() }

        val chunkSize = when (cmd) {
            'M' -> 2
            'L' -> 2
            'T' -> 2
            'H' -> 1
            'V' -> 1
            'C' -> 6
            'S' -> 4
            'Q' -> 4
            'A' -> 7
            'Z' -> 0
            else -> 2
        }

        if (cmd == 'Z') {
            close()
            return@forEach
        }

        var i = 0
        while (i + chunkSize <= nums.size) {
            when (cmd) {
                'M' -> {
                    if (i == 0) {
                        if (isRelative) moveToRelative(nums[i], nums[i + 1])
                        else moveTo(nums[i], nums[i + 1])
                    } else {
                        if (isRelative) lineToRelative(nums[i], nums[i + 1])
                        else lineTo(nums[i], nums[i + 1])
                    }
                }

                'L' -> {
                    if (isRelative) lineToRelative(nums[i], nums[i + 1])
                    else lineTo(nums[i], nums[i + 1])
                }

                'H' -> {
                    if (isRelative) horizontalLineToRelative(nums[i])
                    else horizontalLineTo(nums[i])
                }

                'V' -> {
                    if (isRelative) verticalLineToRelative(nums[i])
                    else verticalLineTo(nums[i])
                }

                'C' -> {
                    if (isRelative) curveToRelative(
                        nums[i], nums[i + 1],
                        nums[i + 2], nums[i + 3],
                        nums[i + 4], nums[i + 5]
                    )
                    else curveTo(
                        nums[i], nums[i + 1],
                        nums[i + 2], nums[i + 3],
                        nums[i + 4], nums[i + 5]
                    )
                }

                'S' -> {
                    if (isRelative) reflectiveCurveToRelative(
                        nums[i], nums[i + 1],
                        nums[i + 2], nums[i + 3]
                    )
                    else reflectiveCurveTo(
                        nums[i], nums[i + 1],
                        nums[i + 2], nums[i + 3]
                    )
                }

                'Q' -> {
                    if (isRelative) lineToRelative(nums[i], nums[i + 1])
                    else lineTo(nums[i], nums[i + 1])
                }

                'T' -> {
                    if (isRelative) lineToRelative(nums[i], nums[i + 1])
                    else lineTo(nums[i], nums[i + 1])
                }

                'A' -> {
                    if (isRelative) arcToRelative(
                        nums[i], nums[i + 1],
                        nums[i + 2],
                        nums[i + 3] != 0f,
                        nums[i + 4] != 0f,
                        nums[i + 5], nums[i + 6]
                    )
                    else arcTo(
                        nums[i], nums[i + 1],
                        nums[i + 2],
                        nums[i + 3] != 0f,
                        nums[i + 4] != 0f,
                        nums[i + 5], nums[i + 6]
                    )
                }
            }
            i += chunkSize
        }
    }
}
