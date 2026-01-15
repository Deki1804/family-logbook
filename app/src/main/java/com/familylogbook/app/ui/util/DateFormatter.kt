package com.familylogbook.app.ui.util

/**
 * Utility functions for formatting date input (ddMMyyyy -> dd.MM.yyyy)
 * Uses "digits as truth" pattern to avoid Compose input issues
 */
object DateFormatter {
    
    /**
     * Formats date digits (ddMMyyyy) to display format (dd.MM.yyyy)
     * Example: "24072021" -> "24.07.2021"
     * 
     * @param digits Raw digits string (ddMMyyyy format, max 8 digits)
     * @return Formatted string (dd.MM.yyyy) or partial format while typing
     */
    fun formatDobDigits(digits: String): String {
        // Filter only digits and limit to 8
        val d = digits.filter { it.isDigit() }.take(8)
        
        return when (d.length) {
            0 -> ""
            1, 2 -> d
            3, 4 -> {
                val day = d.take(2)
                val month = d.drop(2)
                "$day.$month"
            }
            else -> {
                val day = d.take(2)
                val month = d.drop(2).take(2)
                val year = d.drop(4)
                "$day.$month.$year"
            }
        }
    }
    
    /**
     * Converts timestamp to digits format (ddMMyyyy)
     * Used when loading existing date from ViewModel
     * 
     * @param timestamp Date timestamp in milliseconds
     * @return Digits string in ddMMyyyy format
     */
    fun timestampToDigits(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("ddMMyyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
