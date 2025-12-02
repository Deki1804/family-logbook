package com.familylogbook.app.data.smarthome

import android.content.Intent
import android.content.Context

/**
 * Manages smart home integration via Google Assistant.
 * This is a basic Level 1/2 integration that triggers Google Assistant intents.
 */
class SmartHomeManager(private val context: Context) {
    
    /**
     * Triggers Google Assistant to handle a smart home command.
     * Opens the assistant so the user can confirm or speak the command.
     */
    fun triggerGoogleAssistant(command: String? = null) {
        try {
            // Option 1: Open Google Assistant directly
            val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // Try to launch Google Assistant
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback: Try alternative intent
                val fallbackIntent = Intent("android.intent.action.VOICE_ASSIST").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(fallbackIntent)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SmartHomeManager", "Error launching Google Assistant: ${e.message}")
        }
    }
    
    /**
     * Checks if Google Assistant is available on this device.
     */
    fun isGoogleAssistantAvailable(): Boolean {
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return intent.resolveActivity(context.packageManager) != null
    }
}

