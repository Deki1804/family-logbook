package com.familylogbook.app.domain.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.familylogbook.app.data.timer.TimerAlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern

/**
 * Manages timers for the app.
 * Detects timer commands from text input and sets up alarms.
 */
object TimerManager {
    
    private val _activeTimers = MutableStateFlow<List<TimerInfo>>(emptyList())
    val activeTimers: StateFlow<List<TimerInfo>> = _activeTimers.asStateFlow()
    
    data class TimerInfo(
        val id: String,
        val durationMinutes: Int,
        val endTime: Long,
        val description: String? = null
    )
    
    /**
     * Detects if text contains a timer command.
     * Examples: "upali timer 5 min", "pokreni timer 10 minuta", "timer 3 min"
     */
    fun detectTimerCommand(text: String): TimerCommand? {
        val lowerText = text.lowercase()
        
        // Check for timer keywords
        val hasTimerKeyword = lowerText.contains("timer") || 
                             lowerText.contains("tajmer") ||
                             lowerText.contains("alarm")
        
        if (!hasTimerKeyword) return null
        
        // Check for start keywords (REQUIRED for timer to start)
        val hasStartKeyword = lowerText.contains("upali") ||
                             lowerText.contains("pokreni") ||
                             lowerText.contains("start") ||
                             lowerText.contains("postavi") ||
                             lowerText.contains("stavi")
        
        // If timer keyword exists but no start keyword, don't start timer
        // (user might just be mentioning timer in text, not starting it)
        if (!hasStartKeyword) return null
        
        // Extract duration
        val duration = extractDuration(lowerText)
        
        if (duration != null && duration > 0) {
            return TimerCommand(
                durationMinutes = duration,
                description = text
            )
        }
        
        return null
    }
    
    /**
     * Extracts duration in minutes from text.
     * Examples: "5 min" -> 5, "10 minuta" -> 10, "1 sat" -> 60
     */
    private fun extractDuration(text: String): Int? {
        // Pattern for "X min" or "X minuta"
        val minPattern = Pattern.compile("(\\d+)\\s*(?:min|minuta|minute)", Pattern.CASE_INSENSITIVE)
        val minMatch = minPattern.matcher(text)
        if (minMatch.find()) {
            return minMatch.group(1)?.toIntOrNull()
        }
        
        // Pattern for "X sat" or "X sati" or "X hour"
        val hourPattern = Pattern.compile("(\\d+)\\s*(?:sat|sati|hour|hours)", Pattern.CASE_INSENSITIVE)
        val hourMatch = hourPattern.matcher(text)
        if (hourMatch.find()) {
            val hours = hourMatch.group(1)?.toIntOrNull() ?: return null
            return hours * 60
        }
        
        // Pattern for just a number (assume minutes)
        val numberPattern = Pattern.compile("\\b(\\d+)\\b")
        val numberMatch = numberPattern.matcher(text)
        if (numberMatch.find()) {
            // Check if there's context that suggests it's a timer
            val number = numberMatch.group(1)?.toIntOrNull() ?: return null
            // Only return if number is reasonable (1-120 minutes)
            if (number in 1..120) {
                return number
            }
        }
        
        return null
    }
    
    /**
     * Starts a timer and sets up alarm.
     */
    fun startTimer(context: Context, command: TimerCommand): String {
        val timerId = System.currentTimeMillis().toString()
        val endTime = System.currentTimeMillis() + (command.durationMinutes * 60 * 1000L)
        
        val timerInfo = TimerInfo(
            id = timerId,
            durationMinutes = command.durationMinutes,
            endTime = endTime,
            description = command.description
        )
        
        // Add to active timers
        _activeTimers.value = _activeTimers.value + timerInfo
        
        // Set up alarm
        setAlarm(context, timerInfo)
        
        return timerId
    }
    
    /**
     * Cancels a timer.
     */
    fun cancelTimer(timerId: String) {
        _activeTimers.value = _activeTimers.value.filter { it.id != timerId }
    }
    
    /**
     * Sets up Android alarm for timer.
     */
    private fun setAlarm(context: Context, timerInfo: TimerInfo) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            putExtra("timer_id", timerInfo.id)
            putExtra("description", timerInfo.description)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timerInfo.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = timerInfo.endTime
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    data class TimerCommand(
        val durationMinutes: Int,
        val description: String? = null
    )
}
