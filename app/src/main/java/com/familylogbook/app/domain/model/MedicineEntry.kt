package com.familylogbook.app.domain.model

/**
 * Helper class for medicine tracking.
 * Wraps LogEntry with medicine-specific functionality.
 * 
 * This is NOT a separate data class - it's a helper to work with LogEntry
 * entries that have category == Category.MEDICINE or Category.HEALTH
 * and medicineGiven != null.
 */
object MedicineEntry {
    
    /**
     * Creates a LogEntry for medicine tracking.
     */
    fun create(
        personId: String,
        medicineName: String,
        dosage: String, // "5ml", "1 tableta", "10mg"
        givenAt: Long = System.currentTimeMillis(),
        intervalHours: Int = 6, // default 6 hours
        notes: String? = null
    ): LogEntry {
        val nextDoseAt = givenAt + (intervalHours * 60 * 60 * 1000L)
        
        return LogEntry(
            personId = personId,
            rawText = notes ?: "Dao/la ${medicineName} ${dosage}",
            category = Category.MEDICINE,
            timestamp = givenAt,
            medicineGiven = medicineName,
            medicineDosage = dosage,
            medicineTimestamp = givenAt,
            nextMedicineTime = nextDoseAt,
            medicineIntervalHours = intervalHours
        )
    }
    
    /**
     * Checks if a LogEntry is a medicine entry.
     */
    fun isMedicineEntry(entry: LogEntry): Boolean {
        return entry.medicineGiven != null && 
               (entry.category == Category.MEDICINE || entry.category == Category.HEALTH)
    }
    
    /**
     * Gets the next dose time for a medicine entry.
     * Returns null if no next dose is scheduled.
     */
    fun getNextDoseTime(entry: LogEntry): Long? {
        return if (isMedicineEntry(entry)) {
            entry.nextMedicineTime
        } else {
            null
        }
    }
    
    /**
     * Checks if a medicine dose is due (next dose time has passed).
     */
    fun isDoseDue(entry: LogEntry): Boolean {
        val nextDose = getNextDoseTime(entry) ?: return false
        return System.currentTimeMillis() >= nextDose
    }
    
    /**
     * Calculates the next dose time based on last dose and interval.
     */
    fun calculateNextDose(lastDoseTime: Long, intervalHours: Int): Long {
        return lastDoseTime + (intervalHours * 60 * 60 * 1000L)
    }
}
