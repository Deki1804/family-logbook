package com.familylogbook.app.domain.model

/**
 * Template for parenting advice that can be shown to users.
 * These are general tips, NOT medical instructions.
 */
data class AdviceTemplate(
    val id: String,
    val title: String,
    val shortDescription: String,
    val tips: List<String>, // Bullet points with suggestions
    val whenToCallDoctor: String? = null, // Optional disclaimer/warning
    val relatedKeywords: List<String> = emptyList() // Keywords that trigger this advice
)

