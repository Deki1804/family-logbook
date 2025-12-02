package com.familylogbook.app.domain.model

import java.util.UUID

/**
 * Represents non-person entities in the family life (car, house, finance, school, work, etc.)
 */
data class Entity(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // e.g., "Auto", "Kuća", "Financije", "Škola", "Posao"
    val type: EntityType,
    val emoji: String = "🚗", // emoji representation
    val avatarColor: String = "#4ECDC4", // hex color
    val metadata: Map<String, String> = emptyMap() // e.g., car: "make", "model", "year"
)

enum class EntityType {
    CAR,
    HOUSE,
    FINANCE,
    SCHOOL,
    WORK,
    SHOPPING,
    OTHER
}

