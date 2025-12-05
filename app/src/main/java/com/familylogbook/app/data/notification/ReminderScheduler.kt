package com.familylogbook.app.data.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules and manages reminder workers.
 */
class ReminderScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val REMINDER_WORK_NAME = "reminder_check_work"
    }
    
    /**
     * Starts periodic reminder checking (runs every 15 minutes).
     */
    fun startPeriodicReminderCheck() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()
        
        val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }
    
    /**
     * Stops periodic reminder checking.
     */
    fun stopPeriodicReminderCheck() {
        workManager.cancelUniqueWork(REMINDER_WORK_NAME)
    }
}

