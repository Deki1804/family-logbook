package com.familylogbook.app.domain.model

/**
 * Helper class for symptom tracking.
 * Wraps LogEntry with symptom-specific functionality.
 * 
 * This is NOT a separate data class - it's a helper to work with LogEntry
 * entries that have category == Category.SYMPTOM or Category.HEALTH
 * and (temperature != null || symptoms != null).
 */
object SymptomEntry {
    
    /**
     * Creates a LogEntry for symptom tracking.
     */
    fun create(
        personId: String,
        temperature: Float? = null, // °C
        symptoms: List<String> = emptyList(), // "kašalj", "curenje nosa", etc.
        timestamp: Long = System.currentTimeMillis(),
        notes: String? = null
    ): LogEntry {
        val category = if (temperature != null || symptoms.isNotEmpty()) {
            Category.SYMPTOM
        } else {
            Category.HEALTH
        }
        
        val rawText = buildString {
            if (temperature != null) {
                append("Temperatura ${temperature}°C")
            }
            if (symptoms.isNotEmpty()) {
                if (temperature != null) append(". ")
                append("Simptomi: ${symptoms.joinToString(", ")}")
            }
            if (notes != null && notes.isNotBlank()) {
                if (temperature != null || symptoms.isNotEmpty()) append(". ")
                append(notes)
            }
        }.ifEmpty { notes ?: "Simptom zapis" }
        
        return LogEntry(
            personId = personId,
            rawText = rawText,
            category = category,
            timestamp = timestamp,
            temperature = temperature,
            symptoms = if (symptoms.isNotEmpty()) symptoms else null
        )
    }
    
    /**
     * Checks if a LogEntry is a symptom entry.
     */
    fun isSymptomEntry(entry: LogEntry): Boolean {
        return (entry.temperature != null || !entry.symptoms.isNullOrEmpty()) &&
               (entry.category == Category.SYMPTOM || entry.category == Category.HEALTH)
    }
    
    /**
     * Gets the temperature from a symptom entry.
     */
    fun getTemperature(entry: LogEntry): Float? {
        return if (isSymptomEntry(entry)) {
            entry.temperature
        } else {
            null
        }
    }
    
    /**
     * Gets the symptoms list from a symptom entry.
     */
    fun getSymptoms(entry: LogEntry): List<String> {
        return if (isSymptomEntry(entry)) {
            entry.symptoms ?: emptyList()
        } else {
            emptyList()
        }
    }
}
