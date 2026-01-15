package com.familylogbook.app.domain.model

/**
 * Helper class for day/routine tracking.
 * Wraps LogEntry with day-specific functionality.
 * 
 * This is NOT a separate data class - it's a helper to work with LogEntry
 * entries that have category == Category.DAY.
 */
object DayEntry {
    
    /**
     * Creates a LogEntry for day/routine tracking.
     */
    fun create(
        personId: String,
        content: String,
        timestamp: Long = System.currentTimeMillis(),
        tags: List<String> = emptyList()
    ): LogEntry {
        return LogEntry(
            personId = personId,
            rawText = content,
            category = Category.DAY,
            timestamp = timestamp,
            tags = tags
        )
    }
    
    /**
     * Creates a checklist entry (simple text entry that can be checked off).
     */
    fun createChecklist(
        personId: String,
        item: String,
        timestamp: Long = System.currentTimeMillis()
    ): LogEntry {
        return LogEntry(
            personId = personId,
            rawText = item,
            category = Category.DAY,
            timestamp = timestamp,
            tags = listOf("checklist")
        )
    }
    
    /**
     * Creates a reminder entry.
     */
    fun createReminder(
        personId: String,
        reminderText: String,
        dueDate: Long,
        timestamp: Long = System.currentTimeMillis()
    ): LogEntry {
        return LogEntry(
            personId = personId,
            rawText = reminderText,
            category = Category.DAY,
            timestamp = timestamp,
            reminderDate = dueDate,
            tags = listOf("reminder")
        )
    }
    
    /**
     * Checks if a LogEntry is a day entry.
     */
    fun isDayEntry(entry: LogEntry): Boolean {
        return entry.category == Category.DAY
    }
    
    /**
     * Checks if a day entry is a checklist item.
     */
    fun isChecklistItem(entry: LogEntry): Boolean {
        return isDayEntry(entry) && entry.tags.contains("checklist")
    }
    
    /**
     * Checks if a day entry is a reminder.
     */
    fun isReminder(entry: LogEntry): Boolean {
        return isDayEntry(entry) && entry.reminderDate != null
    }
    
    /**
     * Checks if a reminder is due or overdue.
     */
    fun isReminderDue(entry: LogEntry): Boolean {
        if (!isReminder(entry)) return false
        val dueDate = entry.reminderDate ?: return false
        return System.currentTimeMillis() >= dueDate
    }
}
