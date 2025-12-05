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
            // Get repository - get userId from shared preferences
            val sharedPrefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val userId = sharedPrefs.getString("user_id", null)
            
            val repository: LogbookRepository = if (userId != null) {
                try {
                    FirestoreLogbookRepository(userId = userId)
                } catch (e: Exception) {
                    InMemoryLogbookRepository()
                }
            } else {
                InMemoryLogbookRepository()
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
                val ageInDays = (now - dateOfBirth) / (1000.0 * 60 * 60 * 24)
                return ageInDays < 730 // Less than 2 years
            }
            
            // Group feeding entries by person
            val feedingEntriesByPerson = entries
                .filter { it.category == com.familylogbook.app.domain.model.Category.FEEDING }
                .groupBy { it.personId ?: it.childId }
            
            // Check feeding reminders (if last feeding > 3 hours for babies)
            feedingEntriesByPerson.forEach { (personId, personFeedings) ->
                if (personId != null && personFeedings.isNotEmpty()) {
                    val lastFeeding = personFeedings.maxByOrNull { it.timestamp }
                    lastFeeding?.let { feeding ->
                        val hoursSinceLastFeeding = (now - feeding.timestamp) / (1000.0 * 60 * 60)
                        
                        // Find person to check age
                        val person = persons.find { it.id == personId }
                        val isBaby = person?.dateOfBirth?.let { isBabyAge(it) } ?: false
                        
                        // Show reminder for babies if last feeding was:
                        // - 3-6 hours ago (normal reminder window)
                        // - 6+ hours ago (urgent reminder)
                        if (isBaby) {
                            if (hoursSinceLastFeeding >= 3.0 && hoursSinceLastFeeding <= 6.0) {
                                val personName = person?.name ?: "Beba"
                                notificationManager.showFeedingReminder(
                                    personName = personName,
                                    hoursSinceLastFeeding = hoursSinceLastFeeding,
                                    notificationId = ("feeding_$personId").hashCode()
                                )
                            } else if (hoursSinceLastFeeding > 6.0 && hoursSinceLastFeeding <= 8.0) {
                                // Urgent reminder - haven't fed in 6+ hours
                                val personName = person?.name ?: "Beba"
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
                        val personName = entry.personId?.let { 
                            // Try to get person name - for now just use entry text
                            null // TODO: Fetch person name from repository
                        }
                        notificationManager.showMedicineReminder(
                            medicineName = entry.medicineGiven ?: "Medicine",
                            personName = personName,
                            notificationId = entry.id.hashCode()
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

