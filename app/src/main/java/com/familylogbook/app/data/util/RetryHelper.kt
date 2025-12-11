package com.familylogbook.app.data.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.flow

/**
 * Utility for retrying operations with exponential backoff.
 */
object RetryHelper {
    
    /**
     * Retries a suspend function with exponential backoff.
     * @param maxRetries Maximum number of retry attempts (default: 3)
     * @param initialDelayMs Initial delay in milliseconds (default: 1000)
     * @param maxDelayMs Maximum delay in milliseconds (default: 10000)
     * @param retryCondition Function that determines if an exception should trigger a retry
     */
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        retryCondition: (Throwable) -> Boolean = { true },
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Throwable? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Throwable) {
                lastException = e
                
                // Don't retry if condition is not met
                if (!retryCondition(e)) {
                    throw e
                }
                
                // Don't delay after last attempt
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    // Exponential backoff: double the delay, but cap at maxDelayMs
                    currentDelay = minOf(currentDelay * 2, maxDelayMs)
                }
            }
        }
        
        // If we get here, all retries failed
        throw lastException ?: Exception("Unknown error")
    }
    
    /**
     * Creates a Flow that retries with exponential backoff.
     */
    // Note: retryFlow is not currently used, but kept for future use
    // If needed, implement with proper Flow retry logic
}
