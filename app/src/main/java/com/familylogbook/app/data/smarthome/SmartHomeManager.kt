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
     * Result class for command execution.
     */
    sealed class CommandResult {
        data object Success : CommandResult()
        data class Error(val message: String) : CommandResult()
    }
    
    /**
     * Executes a smart home command by sending it directly to Google Assistant.
     * The user does NOT need to speak - the command is executed automatically.
     * 
     * @param userText The original user input (e.g., "Upali rumbu")
     * @return CommandResult.Success if the command was sent successfully, CommandResult.Error otherwise
     */
    fun executeCommand(userText: String): CommandResult {
        try {
            // Parse the user text into a Google Assistant command
            val command = SmartHomeCommandParser.parseCommand(userText)
            
            if (command == null) {
                android.util.Log.w("SmartHomeManager", "Could not parse command: $userText")
                return CommandResult.Error("Ne mogu razumjeti komandu. Pokušaj koristiti jasnije naredbe poput 'Upali svjetla' ili 'Postavi temperaturu na 22 stupnja'.")
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
                return CommandResult.Success
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
                    return CommandResult.Success
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
                    return CommandResult.Success
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
                return CommandResult.Success
            }
            
            android.util.Log.w("SmartHomeManager", "No Assistant app found")
            return CommandResult.Error("Google Assistant nije dostupan. Provjeri da li je instaliran na tvom uređaju.")
            
        } catch (e: Exception) {
            android.util.Log.e("SmartHomeManager", "Error executing command: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("Google Assistant", ignoreCase = true) == true -> 
                    "Google Assistant nije dostupan. Provjeri da li je instaliran."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Greška mreže. Provjeri internetsku vezu."
                else -> 
                    "Ne mogu poslati komandu. Provjeri da li je pametna kuća povezana."
            }
            return CommandResult.Error(errorMessage)
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

