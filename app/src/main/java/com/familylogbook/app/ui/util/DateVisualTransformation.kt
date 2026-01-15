package com.familylogbook.app.ui.util

import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString

/**
 * Visual transformation for date input field
 * Formats digits (ddMMyyyy) to display format (dd.MM.yyyy) visually
 * while keeping the actual value as raw digits
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(8)
        val formatted = DateFormatter.formatDobDigits(digits)
        
        return androidx.compose.ui.text.input.TransformedText(
            AnnotatedString(formatted),
            DateOffsetMapping(digits.length, formatted.length)
        )
    }
}

/**
 * Maps cursor position between original (digits) and transformed (formatted) text
 */
private class DateOffsetMapping(
    private val originalLength: Int,
    private val transformedLength: Int
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        // Map cursor position from digits to formatted
        // "24072021" -> "24.07.2021"
        // offset 0-2: stays same (0-2)
        // offset 3-4: +1 for first dot (4-5)
        // offset 5-8: +2 for both dots (7-10)
        return when {
            offset <= 2 -> offset
            offset <= 4 -> offset + 1
            else -> offset + 2
        }.coerceAtMost(transformedLength)
    }
    
    override fun transformedToOriginal(offset: Int): Int {
        // Map cursor position from formatted to digits
        // "24.07.2021" -> "24072021"
        // offset 0-2: stays same (0-2)
        // offset 3-5: -1 for first dot (2-4)
        // offset 6-10: -2 for both dots (4-8)
        return when {
            offset <= 2 -> offset
            offset <= 5 -> offset - 1
            else -> offset - 2
        }.coerceIn(0, originalLength)
    }
}
