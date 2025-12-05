package com.familylogbook.app.data.notification

import com.familylogbook.app.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.familylogbook.app.MainActivity

/**
 * Manages notifications for medicine reminders and service/appointment reminders.
 */
class NotificationManager(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val CHANNEL_ID_MEDICINE = "medicine_reminders"
        private const val CHANNEL_ID_SERVICE = "service_reminders"
        private const val CHANNEL_ID_FEEDING = "feeding_reminders"
        private const val CHANNEL_NAME_MEDICINE = "Podsjetnici za lijekove"
        private const val CHANNEL_NAME_SERVICE = "Podsjetnici za servise"
        private const val CHANNEL_NAME_FEEDING = "Podsjetnici za hranjenje"
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Medicine reminders channel
            val medicineChannel = NotificationChannel(
                CHANNEL_ID_MEDICINE,
                CHANNEL_NAME_MEDICINE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Obavijesti za podsjetnike o lijekovima"
                enableVibration(true)
                enableLights(true)
            }
            
            // Service reminders channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                CHANNEL_NAME_SERVICE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Obavijesti za podsjetnike o servisima i terminima"
                enableVibration(true)
            }
            
            // Feeding reminders channel
            val feedingChannel = NotificationChannel(
                CHANNEL_ID_FEEDING,
                CHANNEL_NAME_FEEDING,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Obavijesti za podsjetnike o hranjenju (samo informativno)"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(medicineChannel)
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(feedingChannel)
        }
    }
    
    /**
     * Shows a notification for medicine reminder.
     */
    fun showMedicineReminder(
        medicineName: String,
        personName: String? = null,
        notificationId: Int
    ) {
        val title = if (personName != null) {
            "Vrijeme za uzimanje lijeka: $medicineName"
        } else {
            "Vrijeme za uzimanje lijeka: $medicineName"
        }
        
        val text = if (personName != null) {
            "$personName bi trebao/la uzeti $medicineName sada"
        } else {
            "Ne zaboravi uzeti $medicineName"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MEDICINE)
            .setSmallIcon(R.drawable.app_logo) // Use app logo as notification icon
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * Shows a notification for service/appointment reminder.
     */
    fun showServiceReminder(
        serviceName: String,
        reminderText: String,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setSmallIcon(R.drawable.app_logo) // Use app logo as notification icon
            .setContentTitle(serviceName)
            .setContentText(reminderText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * Shows a notification for feeding reminder.
     */
    fun showFeedingReminder(
        personName: String,
        hoursSinceLastFeeding: Double,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_FEEDING)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Vrijeme za hranjenje?")
            .setContentText("$personName nije hranjen već ${String.format("%.1f", hoursSinceLastFeeding)} sati. (Ovo je samo informativno)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * Cancels a notification by ID.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Cancels all notifications.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}

