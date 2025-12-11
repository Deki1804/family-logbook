package com.familylogbook.app.domain.util

import java.util.Calendar

/**
 * Utility functions for calculating person age and age-based checks.
 */
object PersonAgeUtils {
    
    /**
     * Calculates age in years from date of birth.
     * @param dateOfBirth Timestamp in milliseconds
     * @return Age in years (with fractional part for months)
     */
    fun calculateAgeInYears(dateOfBirth: Long): Double {
        val now = System.currentTimeMillis()
        val birthDate = Calendar.getInstance().apply {
            timeInMillis = dateOfBirth
        }
        val currentDate = Calendar.getInstance().apply {
            timeInMillis = now
        }
        
        var years = currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        val monthDiff = currentDate.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)
        val dayDiff = currentDate.get(Calendar.DAY_OF_MONTH) - birthDate.get(Calendar.DAY_OF_MONTH)
        
        // Adjust if birthday hasn't occurred yet this year
        if (monthDiff < 0 || (monthDiff == 0 && dayDiff < 0)) {
            years--
        }
        
        // Calculate fractional part (months)
        var months = monthDiff
        if (dayDiff < 0) {
            months--
        }
        if (months < 0) {
            months += 12
        }
        
        return years + (months / 12.0)
    }
    
    /**
     * Checks if person is a baby (≤12 months old).
     * Used for baby-specific presets and features.
     * @param dateOfBirth Timestamp in milliseconds
     * @return true if person is ≤12 months old
     */
    fun isBabyAge(dateOfBirth: Long): Boolean {
        val ageInYears = calculateAgeInYears(dateOfBirth)
        return ageInYears < 1.0 // Less than 1 year old
    }
    
    /**
     * Checks if person can have feeding tracking (≤2 years old).
     * Used for feeding timer and feeding-related features.
     * @param dateOfBirth Timestamp in milliseconds
     * @return true if person is ≤2 years old
     */
    fun canHaveFeeding(dateOfBirth: Long): Boolean {
        val ageInYears = calculateAgeInYears(dateOfBirth)
        return ageInYears < 2.0 // Less than 2 years old
    }
    
    /**
     * Calculates age in days from date of birth.
     * @param dateOfBirth Timestamp in milliseconds
     * @return Age in days
     */
    fun calculateAgeInDays(dateOfBirth: Long): Long {
        val now = System.currentTimeMillis()
        return (now - dateOfBirth) / (1000L * 60 * 60 * 24)
    }
}
