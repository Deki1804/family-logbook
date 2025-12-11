package com.familylogbook.app.data.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.familylogbook.app.domain.timer.TimerManager

/**
 * Broadcast receiver for timer alarms.
 * Shows a notification when timer expires.
 */
class TimerAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra("timer_id") ?: return
        val description = intent.getStringExtra("description")
        
        // Remove timer from active timers list
        TimerManager.cancelTimer(timerId)
        
        // Show notification
        showNotification(context, timerId, description)
    }
    
    private fun showNotification(context: Context, timerId: String, description: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Timer Alarms",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for timer alarms"
            channel.enableVibration(true)
            channel.enableLights(true)
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
    }
}
