package com.familylogbook.app.domain.model

import java.util.UUID

/**
 * Represents a person in the family (parent, child, etc.)
 * Replaces the old Child model with more flexibility.
 */
data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PersonType = PersonType.CHILD,
    val dateOfBirth: Long? = null, // timestamp in milliseconds
    val avatarColor: String = "#FF6B6B", // hex color
    val emoji: String = "👶", // emoji representation
    val relationship: String? = null // e.g., "Mama", "Tata", "Neo", "Mia"
)

enum class PersonType {
    PARENT,
    CHILD,
    OTHER_FAMILY_MEMBER,
    PET
}

