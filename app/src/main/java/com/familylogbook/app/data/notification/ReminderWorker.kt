package com.familylogbook.app.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.familylogbook.app.data.repository.FirestoreLogbookRepository
import com.familylogbook.app.data.repository.InMemoryLogbookRepository
import com.familylogbook.app.domain.repository.LogbookRepository
import kotlinx.coroutines.flow.first

/**
 * Background worker that checks for medicine and service reminders and shows notifications.
 * This worker runs periodically to check for upcoming reminders.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val notificationManager = NotificationManager(applicationContext)
    
    override suspend fun doWork(): Result {
        return try {
            // Get repository - it uses FirebaseAuth dynamically
            // If user is not authenticated, it will throw exception and we'll skip notifications
            val repository: LogbookRepository = try {
                FirestoreLogbookRepository()
            } catch (e: Exception) {
                // User not authenticated or Firebase not available - skip notifications
                android.util.Log.d("ReminderWorker", "User not authenticated, skipping reminders")
                return Result.success()
            }
            
            val now = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val oneHourInMillis = 60 * 60 * 1000L
            
            // Get all entries
            val entries = repository.getAllEntries().first()
            
            // Get all persons to find baby names
            val persons = repository.getAllPersons().first()
            
            // Helper function to check if person is a baby (less than 2 years old)
            fun isBabyAge(dateOfBirth: Long?): Boolean {
                if (dateOfBirth == null) return false
                return com.familylogbook.app.domain.util.PersonAgeUtils.canHaveFeeding(dateOfBirth)
            }
            
            // Group feeding entries by person - ONLY if person exists and is a CHILD type
            val feedingEntriesByPerson = entries
                .filter { entry ->
                    entry.category == com.familylogbook.app.domain.model.Category.FEEDING &&
                    entry.personId != null && // Must have personId
                    entry.feedingType != null // Must have explicit feeding type (not just detected from text)
                }
                .mapNotNull { entry ->
                    val personId = entry.personId ?: entry.childId
                    val person = personId?.let { persons.find { it.id == personId } }
                    // Only include if person exists, is a CHILD, and has dateOfBirth
                    if (person != null && 
                        person.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                        person.dateOfBirth != null) {
                        personId to entry
                    } else {
                        null
                    }
                }
                .groupBy({ it.first }, { it.second })
            
            // Check feeding reminders (if last feeding > 3 hours for babies)
            feedingEntriesByPerson.forEach { (personId, personFeedings) ->
                if (personId != null && personFeedings.isNotEmpty()) {
                    val lastFeeding = personFeedings.maxByOrNull { it.timestamp }
                    lastFeeding?.let { feeding ->
                        val hoursSinceLastFeeding = (now - feeding.timestamp) / (1000.0 * 60 * 60)
                        
                        // Find person to check age (should exist since we filtered above)
                        val person = persons.find { it.id == personId }
                        val isBaby = person?.dateOfBirth?.let { isBabyAge(it) } ?: false
                        
                        // Show reminder ONLY for babies (< 2 years old)
                        // - 3-6 hours ago (normal reminder window)
                        // - 6+ hours ago (urgent reminder)
                        if (isBaby && person != null) {
                            if (hoursSinceLastFeeding >= 3.0 && hoursSinceLastFeeding <= 6.0) {
                                val personName = person.name
                                notificationManager.showFeedingReminder(
                                    personName = personName,
                                    hoursSinceLastFeeding = hoursSinceLastFeeding,
                                    notificationId = ("feeding_$personId").hashCode()
                                )
                            } else if (hoursSinceLastFeeding > 6.0 && hoursSinceLastFeeding <= 8.0) {
                                // Urgent reminder - haven't fed in 6+ hours
                                val personName = person.name
                                notificationManager.showFeedingReminder(
                                    personName = personName,
                                    hoursSinceLastFeeding = hoursSinceLastFeeding,
                                    notificationId = ("feeding_urgent_$personId").hashCode()
                                )
                            }
                        }
                    }
                }
            }
            
            entries.forEach { entry ->
                // Check medicine reminders
                entry.nextMedicineTime?.let { nextTime ->
                    val timeUntilNext = nextTime - now
                    // Show notification if it's time (within 5 minutes) or overdue
                    if (timeUntilNext <= 5 * 60 * 1000L && timeUntilNext >= -30 * 60 * 1000L) {
                        val personName = entry.personId?.let { personId ->
                            persons.find { it.id == personId }?.name
                        } ?: entry.childId?.let { childId ->
                            // Legacy child support - try to find as person with same ID
                            persons.find { it.id == childId }?.name ?: "Osoba"
                        } ?: null
                        notificationManager.showMedicineReminder(
                            medicineName = entry.medicineGiven ?: "Lijek",
                            personName = personName,
                            notificationId = entry.id.hashCode()
                        )
                    }
                }
                
                // Check vaccination reminders
                entry.nextVaccinationDate?.let { vaccinationDate ->
                    val timeUntilVaccination = vaccinationDate - now
                    val personName = entry.personId?.let { personId ->
                        persons.find { it.id == personId }?.name
                    } ?: entry.childId?.let { childId ->
                        persons.find { it.id == childId }?.name ?: "Dijete"
                    } ?: "Dijete"
                    
                    // Show notification 7 days before
                    if (timeUntilVaccination > 0 && timeUntilVaccination <= 7 * oneDayInMillis && 
                        timeUntilVaccination > 6 * oneDayInMillis) {
                        notificationManager.showServiceReminder(
                            serviceName = "Cjepivo",
                            reminderText = entry.nextVaccinationMessage ?: "Vrijeme je za sljedeće cjepivo za $personName. Naruči se kod pedijatra.",
                            notificationId = (entry.id + "_vaccination_7days").hashCode()
                        )
                    }
                    
                    // Show notification 1 day before
                    if (timeUntilVaccination > 0 && timeUntilVaccination <= oneDayInMillis && 
                        timeUntilVaccination > oneDayInMillis - oneHourInMillis) {
                        notificationManager.showServiceReminder(
                            serviceName = "Cjepivo",
                            reminderText = entry.nextVaccinationMessage ?: "Sutra je termin za cjepivo za $personName.",
                            notificationId = (entry.id + "_vaccination_1day").hashCode()
                        )
                    }
                    
                    // Show notification on the day or if overdue (within 2 hours window)
                    if (timeUntilVaccination <= 2 * oneHourInMillis && timeUntilVaccination >= -2 * oneHourInMillis) {
                        notificationManager.showServiceReminder(
                            serviceName = "Cjepivo",
                            reminderText = entry.nextVaccinationMessage ?: "Danas je termin za cjepivo za $personName.",
                            notificationId = (entry.id + "_vaccination_today").hashCode()
                        )
                    }
                }
                
                // Check service/appointment reminders
                entry.reminderDate?.let { reminderDate ->
                    val timeUntilReminder = reminderDate - now
                    
                    // Show notification 1 day before
                    if (timeUntilReminder > 0 && timeUntilReminder <= oneDayInMillis && 
                        timeUntilReminder > oneDayInMillis - oneHourInMillis) {
                        notificationManager.showServiceReminder(
                            serviceName = entry.serviceType ?: "Service",
                            reminderText = "Reminder: ${entry.rawText} is scheduled for tomorrow",
                            notificationId = (entry.id + "_1day").hashCode()
                        )
                    }
                    
                    // Show notification on the day (at 8 AM or if overdue)
                    if (timeUntilReminder <= oneHourInMillis && timeUntilReminder >= -oneHourInMillis) {
                        notificationManager.showServiceReminder(
                            serviceName = entry.serviceType ?: "Service",
                            reminderText = "Today: ${entry.rawText}",
                            notificationId = (entry.id + "_today").hashCode()
                        )
                    }
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

