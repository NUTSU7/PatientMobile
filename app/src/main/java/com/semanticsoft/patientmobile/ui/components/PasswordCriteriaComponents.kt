package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.theme.Gray100
import com.semanticsoft.patientmobile.ui.theme.Gray200

private val ColorGreen = Color(0xFF22C55E)
private val ColorRed = Color(0xFFEF4444)

internal val commonPasswordBlocklist = setOf(
    "password", "password123", "qwerty", "qwerty123", "123456789",
    "letmein", "welcome", "iloveyou", "admin", "changeme"
)

internal data class PasswordCriteria(
    val hasLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasSpecial: Boolean = false,
    val hasDigit: Boolean = false,
    val notCommon: Boolean = false,
    val notPersonal: Boolean = false
)

internal fun computePasswordCriteria(password: String, email: String = ""): PasswordCriteria {
    val lower = password.lowercase()
    val emailLower = email.lowercase()
    val emailLocal = emailLower.substringBefore("@")

    return PasswordCriteria(
        hasLength = password.length >= 8,
        hasUppercase = password.any { it.isUpperCase() },
        hasSpecial = password.any { it in "!@#\$%^&*()_+-=[]{};':\"\\|,.<>/?~`" },
        hasDigit = password.any { it.isDigit() },
        notCommon = lower !in commonPasswordBlocklist,
        notPersonal = if (email.isBlank()) true else {
            !lower.contains(emailLower) && (emailLocal.length < 3 || !lower.contains(emailLocal))
        }
    )
}

@Composable
internal fun PasswordCriteriaRow(met: Boolean, label: String) {
    val color = if (met) ColorGreen else ColorRed
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (met) Icons.Outlined.Check else Icons.Outlined.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
internal fun PasswordMatchIndicator(passwordsMatch: Boolean) {
    val color = if (passwordsMatch) ColorGreen else ColorRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray100, RoundedCornerShape(8.dp))
            .border(1.dp, Gray200, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (passwordsMatch) Icons.Outlined.Check else Icons.Outlined.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Parolele coincid",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
