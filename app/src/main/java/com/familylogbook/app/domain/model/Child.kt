package com.familylogbook.app.domain.model

import java.util.UUID

data class Child(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateOfBirth: Long? = null, // timestamp in milliseconds
    val avatarColor: String = "#FF6B6B", // hex color
    val emoji: String = "👶" // emoji representation
)

