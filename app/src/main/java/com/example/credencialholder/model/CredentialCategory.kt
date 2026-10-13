package com.example.credencialholder.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.credencialholder.theme.*

enum class CredentialCategory(
    val id: String,
    val title: String,
    val hexColor: String
) {
    IDENTIFICATION("id", "Identificación", "#2563EB"),
    TRANSPORT("transport", "Licencias / Vial", "#D97706"),
    WORK("work", "Laboral / Trabajo", "#0D9488"),
    EDUCATION("education", "Educación", "#7C3AED"),
    HEALTH("health", "Salud y Seguro", "#E11D48"),
    MEMBERSHIP("membership", "Membresías", "#16A34A"),
    OTHER("other", "Otros", "#475569");

    fun getIcon(): ImageVector = when (this) {
        IDENTIFICATION -> Icons.Default.Badge
        TRANSPORT -> Icons.Default.DirectionsCar
        WORK -> Icons.Default.Work
        EDUCATION -> Icons.Default.School
        HEALTH -> Icons.Default.LocalHospital
        MEMBERSHIP -> Icons.Default.CardMembership
        OTHER -> Icons.Default.CreditCard
    }

    fun getColor(): Color = when (this) {
        IDENTIFICATION -> CatIdentification
        TRANSPORT -> CatTransport
        WORK -> CatWork
        EDUCATION -> CatEducation
        HEALTH -> CatHealth
        MEMBERSHIP -> CatMembership
        OTHER -> CatOther
    }

    companion object {
        fun fromId(id: String?): CredentialCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: IDENTIFICATION
        }
    }
}
