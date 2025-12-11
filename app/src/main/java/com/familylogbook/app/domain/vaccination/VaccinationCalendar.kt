package com.familylogbook.app.domain.vaccination

import java.util.Calendar

/**
 * Helper class to manage vaccination schedule for children.
 * Based on Croatian vaccination calendar.
 */
object VaccinationCalendar {
    
    /**
     * Represents a vaccination type
     */
    data class VaccinationType(
        val name: String,
        val shortName: String,
        val description: String,
        val ageMonths: Int, // Age in months when this vaccination should be given
        val ageYears: Int? = null // Alternative: age in years (if null, use months)
    )
    
    /**
     * Croatian vaccination schedule
     */
    private val vaccinationSchedule = listOf(
        // 2 months (8-12 weeks)
        VaccinationType("DTP-Hib-IPV", "DTP-Hib-IPV", "Difterija, tetanus, pertusis, Haemophilus influenzae, polio", 2),
        VaccinationType("Prevenar 13", "Prevenar 13", "Pneumokokna infekcija", 2),
        
        // 3 months
        VaccinationType("DTP-Hib-IPV", "DTP-Hib-IPV", "Difterija, tetanus, pertusis, Haemophilus influenzae, polio", 3),
        VaccinationType("Prevenar 13", "Prevenar 13", "Pneumokokna infekcija", 3),
        
        // 4 months
        VaccinationType("DTP-Hib-IPV", "DTP-Hib-IPV", "Difterija, tetanus, pertusis, Haemophilus influenzae, polio", 4),
        VaccinationType("Prevenar 13", "Prevenar 13", "Pneumokokna infekcija", 4),
        
        // 12 months
        VaccinationType("MMR", "MMR", "Morbilli, mumps, rubella (trivivac)", 12),
        VaccinationType("MenC", "MenC", "Meningokok C", 12),
        
        // 18 months
        VaccinationType("DTP-Hib-IPV", "DTP-Hib-IPV", "Difterija, tetanus, pertusis, Haemophilus influenzae, polio", 18),
        
        // 6 years
        VaccinationType("DTP-IPV", "DTP-IPV", "Difterija, tetanus, pertusis, polio", 6 * 12), // 6 years = 72 months
        VaccinationType("MMR", "MMR", "Morbilli, mumps, rubella (trivivac)", 6 * 12),
        
        // 11-12 years
        VaccinationType("Tdap", "Tdap", "Tetanus, difterija, pertusis (za adolescente)", 11 * 12) // 11 years = 132 months
    )
    
    /**
     * Common vaccination names in Croatian and English (for text extraction)
     */
    private val vaccinationKeywords = mapOf(
        // DTP variants
        "dtp" to "DTP-Hib-IPV",
        "dtp-hib-ipv" to "DTP-Hib-IPV",
        "difterija" to "DTP-Hib-IPV",
        "tetanus" to "DTP-Hib-IPV",
        "pertusis" to "DTP-Hib-IPV",
        "haemophilus" to "DTP-Hib-IPV",
        "polio" to "DTP-Hib-IPV",
        "poliomijelitis" to "DTP-Hib-IPV",
        
        // Prevenar
        "prevenar" to "Prevenar 13",
        "pneumokok" to "Prevenar 13",
        "pneumokokna" to "Prevenar 13",
        
        // MMR
        "mmr" to "MMR",
        "trivivac" to "MMR",
        "morbili" to "MMR",
        "mumps" to "MMR",
        "rubella" to "MMR",
        "rubeola" to "MMR",
        "ospice" to "MMR",
        "zaušnjaci" to "MMR",
        "rubeola" to "MMR",
        
        // MenC
        "menc" to "MenC",
        "meningokok" to "MenC",
        "meningokok c" to "MenC",
        
        // Tdap
        "tdap" to "Tdap"
    )
    
    /**
     * Extracts vaccination name from text
     */
    fun extractVaccinationName(text: String): String? {
        val lowerText = text.lowercase()
        
        // Try to find vaccination keywords
        for ((keyword, vaccinationName) in vaccinationKeywords) {
            if (lowerText.contains(keyword)) {
                return vaccinationName
            }
        }
        
        return null
    }
    
    /**
     * Gets list of possible vaccinations for a child based on their age.
     * Returns vaccinations that are appropriate for the child's current age.
     */
    fun getPossibleVaccinationsForAge(dateOfBirth: Long): List<VaccinationType> {
        val now = System.currentTimeMillis()
        val ageMonths = calculateAgeInMonths(dateOfBirth, now)
        
        // Return vaccinations that child is old enough for (within reasonable range)
        return vaccinationSchedule.filter { vaccination ->
            val ageForVaccination = vaccination.ageYears?.let { it * 12 } ?: vaccination.ageMonths
            // Include vaccinations that child is old enough for, or will be soon (within 3 months)
            ageMonths >= ageForVaccination - 3
        }.distinctBy { it.shortName } // Remove duplicates (same vaccination at different ages)
    }
    
    /**
     * Calculates next vaccination based on child's age and given vaccinations.
     * @param dateOfBirth Child's date of birth in milliseconds
     * @param givenVaccinations List of vaccination names that child has already received
     * @return Next vaccination recommendation, or null if no more vaccinations needed
     */
    fun getNextVaccination(
        dateOfBirth: Long,
        givenVaccinations: List<String>
    ): VaccinationRecommendation? {
        val now = System.currentTimeMillis()
        val ageMonths = calculateAgeInMonths(dateOfBirth, now)
        
        // Find next vaccination that child hasn't received yet
        for (vaccination in vaccinationSchedule) {
            val ageForVaccination = vaccination.ageYears?.let { it * 12 } ?: vaccination.ageMonths
            
            // If child is old enough for this vaccination and hasn't received it yet
            if (ageMonths >= ageForVaccination) {
                // Check if child already received this vaccination at this age
                val alreadyReceived = givenVaccinations.any { given ->
                    matchesVaccinationType(given, vaccination.shortName)
                }
                
                if (!alreadyReceived) {
                    // Calculate when this vaccination should be given
                    // If child is already past the age, recommend ASAP (next week)
                    // Otherwise, recommend 1-2 weeks before the age
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = dateOfBirth
                        add(Calendar.MONTH, ageForVaccination)
                        // Add 1 week to give time to schedule appointment
                        add(Calendar.DAY_OF_MONTH, 7)
                    }
                    
                    val recommendedDate = calendar.timeInMillis
                    val shouldBeGivenASAP = now >= recommendedDate - (7 * 24 * 60 * 60 * 1000L) // Within 1 week
                    
                    return VaccinationRecommendation(
                        vaccinationType = vaccination,
                        recommendedDate = recommendedDate,
                        shouldBeGivenASAP = shouldBeGivenASAP,
                        message = buildVaccinationMessage(vaccination, ageForVaccination, shouldBeGivenASAP)
                    )
                }
            }
        }
        
        // Check for upcoming vaccinations (within next 2 months)
        for (vaccination in vaccinationSchedule) {
            val ageForVaccination = vaccination.ageYears?.let { it * 12 } ?: vaccination.ageMonths
            val monthsUntil = ageForVaccination - ageMonths
            
            if (monthsUntil > 0 && monthsUntil <= 2) {
                val alreadyReceived = givenVaccinations.any { given ->
                    matchesVaccinationType(given, vaccination.shortName)
                }
                
                if (!alreadyReceived) {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = dateOfBirth
                        add(Calendar.MONTH, ageForVaccination)
                        // Add 1 week buffer
                        add(Calendar.DAY_OF_MONTH, 7)
                    }
                    
                    return VaccinationRecommendation(
                        vaccinationType = vaccination,
                        recommendedDate = calendar.timeInMillis,
                        shouldBeGivenASAP = false,
                        message = buildVaccinationMessage(vaccination, ageForVaccination, false)
                    )
                }
            }
        }
        
        return null
    }
    
    /**
     * Checks if a given vaccination name matches a vaccination type
     */
    private fun matchesVaccinationType(givenName: String, typeShortName: String): Boolean {
        val lowerGiven = givenName.lowercase()
        val lowerType = typeShortName.lowercase()
        
        // Direct match
        if (lowerGiven.contains(lowerType) || lowerType.contains(lowerGiven)) {
            return true
        }
        
        // Check if given name matches any keyword for this type
        val keywordsForType = vaccinationKeywords.filter { it.value == typeShortName }.keys
        return keywordsForType.any { lowerGiven.contains(it) }
    }
    
    /**
     * Builds a friendly message about the vaccination
     */
    private fun buildVaccinationMessage(
        vaccination: VaccinationType,
        ageMonths: Int,
        asap: Boolean
    ): String {
        val ageText = if (ageMonths < 12) {
            "$ageMonths mjeseci"
        } else {
            "${ageMonths / 12} ${if (ageMonths / 12 == 1) "godinu" else "godina"}"
        }
        
        return if (asap) {
            "Preporučeno: ${vaccination.shortName} (${vaccination.description}) - Trebalo bi biti primljeno do $ageText. Naruči se kod pedijatra za termin."
        } else {
            "Sljedeće cjepivo: ${vaccination.shortName} (${vaccination.description}) - Preporučeno u dobi od $ageText. Naruči se kod pedijatra za cca ${ageMonths} mjeseci."
        }
    }
    
    /**
     * Calculates age in months
     */
    private fun calculateAgeInMonths(dateOfBirth: Long, currentTime: Long): Int {
        val birthDate = Calendar.getInstance().apply {
            timeInMillis = dateOfBirth
        }
        val currentDate = Calendar.getInstance().apply {
            timeInMillis = currentTime
        }
        
        var months = (currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)) * 12
        months += currentDate.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)
        
        // Adjust if day hasn't passed yet this month
        if (currentDate.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH)) {
            months--
        }
        
        return months
    }
    
    /**
     * Recommendation for next vaccination
     */
    data class VaccinationRecommendation(
        val vaccinationType: VaccinationType,
        val recommendedDate: Long,
        val shouldBeGivenASAP: Boolean,
        val message: String
    )
}
