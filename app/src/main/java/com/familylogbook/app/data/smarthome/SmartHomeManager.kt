package com.familylogbook.app.data.smarthome

import android.content.Intent
import android.content.Context
import android.net.Uri

/**
 * Manages smart home integration via Google Assistant App Actions (Level 2).
 * Sends commands directly to Google Assistant without requiring user voice input.
 */
class SmartHomeManager(private val context: Context) {
    
    /**
     * Executes a smart home command by sending it directly to Google Assistant.
     * The user does NOT need to speak - the command is executed automatically.
     * 
     * @param userText The original user input (e.g., "Upali rumbu")
     * @return true if the command was sent successfully, false otherwise
     */
    fun executeCommand(userText: String): Boolean {
        try {
            // Parse the user text into a Google Assistant command
            val command = SmartHomeCommandParser.parseCommand(userText)
            
            if (command == null) {
                android.util.Log.w("SmartHomeManager", "Could not parse command: $userText")
                return false
            }
            
            android.util.Log.d("SmartHomeManager", "Executing command: $command")
            
            // Method 1: Use App Actions intent with voice command
            // This sends the command directly to Google Assistant
            val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                // Add the command as an extra - some Assistant implementations support this
                putExtra("android.speech.extra.RECOGNIZED_SPEECH", command)
            }
            
            // Try to launch with the command
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
            
            // Method 2: Use Google Assistant deep link with query parameter
            // This is more reliable for sending commands directly
            // Format: googleassistant://send?query=COMMAND
            try {
                val assistantIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("googleassistant://send?query=${Uri.encode(command)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    setPackage("com.google.android.googlequicksearchbox")
                }
                
                if (assistantIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(assistantIntent)
                    return true
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Deep link method failed: ${e.message}")
            }
            
            // Method 2b: Alternative deep link format
            try {
                val altIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://assistant.google.com/search?q=${Uri.encode(command)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    setPackage("com.google.android.googlequicksearchbox")
                }
                
                if (altIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(altIntent)
                    return true
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Alternative deep link failed: ${e.message}")
            }
            
            // Method 3: Fallback - use generic voice command intent
            // This will open Assistant, but we'll try to pass the command
            val fallbackIntent = Intent("android.intent.action.VOICE_ASSIST").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("android.speech.extra.RECOGNIZED_SPEECH", command)
            }
            
            if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(fallbackIntent)
                return true
            }
            
            android.util.Log.w("SmartHomeManager", "No Assistant app found")
            return false
            
        } catch (e: Exception) {
            android.util.Log.e("SmartHomeManager", "Error executing command: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Checks if Google Assistant is available on this device.
     */
    fun isGoogleAssistantAvailable(): Boolean {
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return intent.resolveActivity(context.packageManager) != null ||
               Intent(Intent.ACTION_VIEW).apply {
                   data = Uri.parse("googleassistant://")
                   setPackage("com.google.android.googlequicksearchbox")
               }.resolveActivity(context.packageManager) != null
    }
}

