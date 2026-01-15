package com.familylogbook.app.data.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.familylogbook.app.domain.timer.TimerManager

/**
 * Background worker for timers.
 *
 * Uses WorkManager instead of exact alarms to avoid requiring exact-alarm permissions.
 * This is sufficient for minute-level timers and is Play-policy friendly.
 */
class TimerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val timerId = inputData.getString(KEY_TIMER_ID) ?: return Result.success()
        val description = inputData.getString(KEY_DESCRIPTION)

        // Remove timer from in-memory list (if app process is alive)
        TimerManager.cancelTimer(timerId)

        showNotification(applicationContext, timerId, description)
        return Result.success()
    }

    private fun showNotification(context: Context, timerId: String, description: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Timer Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = "Notifications for timer alarms"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationText = description ?: "Timer je istekao!"

        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Timer")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(timerId.hashCode(), notification)
    }

    companion object {
        private const val TIMER_CHANNEL_ID = "timer_alarms"

        const val KEY_TIMER_ID = "timer_id"
        const val KEY_DESCRIPTION = "description"
    }
}

