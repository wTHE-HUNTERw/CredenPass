package com.example.credencialholder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpirationBadge(
    expiryDateStr: String?,
    modifier: Modifier = Modifier
) {
    if (expiryDateStr.isNullOrEmpty()) return

    val status = getExpiryStatus(expiryDateStr)

    val (bgColor, dotColor, label) = when (status) {
        ExpiryStatus.VALID -> Triple(
            Color(0x2216A34A),
            Color(0xFF16A34A),
            "Vigente hasta $expiryDateStr"
        )
        ExpiryStatus.EXPIRING_SOON -> Triple(
            Color(0x33EA580C),
            Color(0xFFEA580C),
            "Vence pronto: $expiryDateStr"
        )
        ExpiryStatus.EXPIRED -> Triple(
            Color(0x33DC2626),
            Color(0xFFDC2626),
            "Vencida: $expiryDateStr"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = dotColor
            )
        }
    }
}

enum class ExpiryStatus {
    VALID,
    EXPIRING_SOON,
    EXPIRED
}

fun getExpiryStatus(dateStr: String): ExpiryStatus {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateStr) ?: return ExpiryStatus.VALID
        val now = Date()

        val diffDays = (date.time - now.time) / (1000 * 60 * 60 * 24)
        when {
            diffDays < 0 -> ExpiryStatus.EXPIRED
            diffDays in 0..30 -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.VALID
        }
    } catch (e: Exception) {
        ExpiryStatus.VALID
    }
}
