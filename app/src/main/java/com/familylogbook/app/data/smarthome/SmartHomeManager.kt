package com.familylogbook.app.data.smarthome

import android.content.Intent
import android.content.Context
import android.net.Uri

/**
 * Manages smart home integration via Gemini/Google Assistant.
 * Attempts to send commands to Gemini first, then falls back to Google Assistant/Google app.
 * 
 * IMPORTANT LIMITATION:
 * Due to Android security restrictions, it's not possible to execute smart home commands
 * directly from other apps without user interaction. Android blocks apps from:
 * - Controlling other apps programmatically
 * - Executing commands without explicit user confirmation
 * - Accessing Google Home devices directly without proper OAuth
 * 
 * CURRENT WORKAROUND:
 * Opens Gemini/Google Home app where user can execute the command manually.
 * This is the best available solution without implementing Google Home APIs SDK.
 * 
 * FUTURE SOLUTION:
 * To enable direct command execution, implement Google Home APIs SDK:
 * 1. Add dependency: implementation 'com.google.android.gms:play-services-home:16.0.0-beta1'
 * 2. Set up OAuth 2.0 in Google Cloud Console
 * 3. Register app and add SHA-1 fingerprint
 * 4. Implement GoogleHomeApiManager for direct device control
 * 
 * See: https://developers.home.google.com/apis/android
 */
class SmartHomeManager(private val context: Context) {
    
    // Package names for different Google apps
    private val GEMINI_PACKAGE = "com.google.android.apps.bard"
    private val GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox"
    private val GOOGLE_HOME_PACKAGE = "com.google.android.apps.chromecast.app"
    
    // Alternative package names for Google Home (in case the main one changes)
    private val GOOGLE_HOME_PACKAGE_ALT = listOf(
        "com.google.android.apps.chromecast.app",
        "com.google.android.apps.nest.hub",
        "com.google.android.apps.home"
    )
    
    /**
     * Result class for command execution.
     */
    sealed class CommandResult {
        data object Success : CommandResult()
        data class Error(val message: String) : CommandResult()
    }
    
    /**
     * Executes a smart home command by sending it to Google Home/Assistant.
     * Tries multiple methods to execute the command directly.
     * 
     * @param userText The original user input (e.g., "Upali svjetla")
     * @return CommandResult.Success if the command was sent successfully, CommandResult.Error otherwise
     */
    fun executeCommand(userText: String): CommandResult {
        // Parse the user text into a command (outside try block so it's accessible in catch)
        val command = SmartHomeCommandParser.parseCommand(userText)
        
        if (command == null) {
            android.util.Log.w("SmartHomeManager", "Could not parse command: $userText")
            return CommandResult.Error("Ne mogu razumjeti komandu. Pokušaj koristiti jasnije naredbe poput 'Upali svjetla' ili 'Postavi temperaturu na 22 stupnja'.")
        }
        
        try {
            android.util.Log.d("SmartHomeManager", "Executing command: $command")
            
            // Method 1: PRIORITETNO - Try Google Home app (direktna kontrola bez glasovnih komandi)
            // Ovo je najbolje rješenje jer korisnik može direktno kontrolirati uređaje u app-u
            try {
                val homeAppIntent = context.packageManager.getLaunchIntentForPackage(GOOGLE_HOME_PACKAGE)
                if (homeAppIntent != null) {
                    homeAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(homeAppIntent)
                        android.util.Log.i("SmartHomeManager", "Opened Google Home app. User can control devices directly.")
                        return CommandResult.Success
                    } catch (e: Exception) {
                        android.util.Log.d("SmartHomeManager", "Google Home app launch failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Google Home app check failed: ${e.message}")
            }
            
            // Method 2: Try Google Assistant deep link with query (ako korisnik preferira)
            try {
                val assistantIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("googleassistant://send?query=${Uri.encode(command)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setPackage(GOOGLE_APP_PACKAGE)
                }
                
                if (assistantIntent.resolveActivity(context.packageManager) != null) {
                    try {
                        context.startActivity(assistantIntent)
                        android.util.Log.i("SmartHomeManager", "Opened Google Assistant with command: $command")
                        return CommandResult.Success
                    } catch (e: SecurityException) {
                        android.util.Log.d("SmartHomeManager", "Security exception with deep link: ${e.message}")
                    } catch (e: Exception) {
                        android.util.Log.d("SmartHomeManager", "Assistant deep link failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Assistant deep link method failed: ${e.message}")
            }
            
            // Method 3: Try Gemini app (fallback)
            try {
                // Try to launch Gemini directly with voice input
                val geminiVoiceIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    setPackage(GEMINI_PACKAGE)
                }
                
                if (geminiVoiceIntent.resolveActivity(context.packageManager) != null) {
                    try {
                        context.startActivity(geminiVoiceIntent)
                        android.util.Log.i("SmartHomeManager", "Opened Gemini voice. User should say: $command")
                        return CommandResult.Success
                    } catch (e: Exception) {
                        android.util.Log.d("SmartHomeManager", "Gemini voice intent failed: ${e.message}")
                    }
                }
                
                // Fallback: Try to launch Gemini app directly
                val geminiIntent = context.packageManager.getLaunchIntentForPackage(GEMINI_PACKAGE)
                if (geminiIntent != null) {
                    geminiIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(geminiIntent)
                        android.util.Log.i("SmartHomeManager", "Opened Gemini. User should say: $command")
                        return CommandResult.Success
                    } catch (e: Exception) {
                        android.util.Log.d("SmartHomeManager", "Gemini launch failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Gemini check failed: ${e.message}")
            }
            
            // Method 4: Try Google Home app (opens app, user can control devices directly)
            try {
                val homeAppIntent = context.packageManager.getLaunchIntentForPackage(GOOGLE_HOME_PACKAGE)
                if (homeAppIntent != null) {
                    homeAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    try {
                        context.startActivity(homeAppIntent)
                        android.util.Log.i("SmartHomeManager", "Opened Google Home app. User can control devices directly.")
                        return CommandResult.Success
                    } catch (e: Exception) {
                        android.util.Log.d("SmartHomeManager", "Google Home app launch failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Google Home app check failed: ${e.message}")
            }
            
            // Method 4: Open voice command (works with both Gemini and Assistant)
            try {
                val voiceIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                if (voiceIntent.resolveActivity(context.packageManager) != null) {
                    try {
                        context.startActivity(voiceIntent)
                        android.util.Log.i("SmartHomeManager", "Opened voice command. User should say: $command")
                        return CommandResult.Success
                    } catch (e: SecurityException) {
                        android.util.Log.w("SmartHomeManager", "Security exception with voice command: ${e.message}")
                    }
                }
            } catch (e: SecurityException) {
                android.util.Log.w("SmartHomeManager", "Security exception: ${e.message}")
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Voice command intent failed: ${e.message}")
            }
            
            // Method 5: Open Google app (which includes Assistant/Gemini)
            try {
                val googleAppIntent = context.packageManager.getLaunchIntentForPackage(GOOGLE_APP_PACKAGE)
                if (googleAppIntent != null) {
                    googleAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(googleAppIntent)
                    android.util.Log.i("SmartHomeManager", "Opened Google app. User should activate Gemini/Assistant and say: $command")
                    return CommandResult.Success
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Google app launch failed: ${e.message}")
            }
            
            // Method 6: Try HTTPS URL (opens in browser, redirects to Assistant/Gemini if available)
            try {
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://assistant.google.com/search?q=${Uri.encode(command)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                if (webIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(webIntent)
                    return CommandResult.Success
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Web intent failed: ${e.message}")
            }
            
            android.util.Log.w("SmartHomeManager", "No Gemini or Assistant app found")
            return CommandResult.Error(
                "Gemini ili Google Home app nije dostupan. Provjeri da li je instaliran na tvom uređaju.\n\n" +
                "Napomena: Zbog sigurnosnih ograničenja Android-a, komanda se ne može izvršiti automatski " +
                "bez otvaranja aplikacije. Ovo je ograničenje Android sustava, ne naše aplikacije."
            )
            
        } catch (e: SecurityException) {
            android.util.Log.e("SmartHomeManager", "Security exception: ${e.message}", e)
            return CommandResult.Error(
                "Android ne dopušta direktne komande iz drugih aplikacija (sigurnosno ograničenje).\n\n" +
                "Ovo je ograničenje Android-a koje sprječava aplikacije da kontroliraju druge aplikacije " +
                "bez korisničke interakcije. Google Home APIs SDK bi omogućio direktnu kontrolu, ali zahtijeva " +
                "kompleksnu OAuth integraciju i Google Cloud Console setup.\n\n" +
                "Otvorit ću Google Home app gdje možeš direktno kontrolirati uređaje."
            )
        } catch (e: Exception) {
            android.util.Log.e("SmartHomeManager", "Error executing command: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("permission", ignoreCase = true) == true ||
                e.message?.contains("dozvola", ignoreCase = true) == true -> 
                    "Gemini ne dopušta direktne komande (sigurnosno ograničenje). Otvori Gemini ručno i reci: \"$command\""
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Greška mreže. Provjeri internetsku vezu."
                else -> 
                    "Ne mogu otvoriti Gemini. Pokušaj ručno: otvori Gemini i reci \"$command\""
            }
            return CommandResult.Error(errorMessage)
        }
    }
    
    /**
     * Opens Google Home app directly.
     * Returns true if successful, false if app is not installed.
     * Uses explicit Intent with MAIN/LAUNCHER activity to avoid Play Store redirect.
     */
    fun openGoogleHomeApp(): Boolean {
        return try {
            val packageManager = context.packageManager
            
            // Try all possible package names
            for (packageName in GOOGLE_HOME_PACKAGE_ALT) {
                try {
                    // Step 1: Verify package is installed
                    val packageInfo = try {
                        packageManager.getPackageInfo(
                            packageName, 
                            android.content.pm.PackageManager.GET_ACTIVITIES
                        )
                    } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                        android.util.Log.d("SmartHomeManager", "Package not installed: $packageName")
                        continue
                    }
                    
                    android.util.Log.d("SmartHomeManager", "Package found: $packageName, trying to launch...")
                    
                    // Step 2: Find MAIN/LAUNCHER activity directly (skip getLaunchIntentForPackage)
                    val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        setPackage(packageName)
                    }
                    
                    val activities = packageManager.queryIntentActivities(
                        mainIntent, 
                        android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                    )
                    
                    if (activities.isEmpty()) {
                        android.util.Log.w("SmartHomeManager", "No MAIN/LAUNCHER activities found for: $packageName")
                        continue
                    }
                    
                    // Step 3: Filter out Play Store activities and other unwanted activities
                    val validActivities = activities.filter { resolveInfo ->
                        val activityPackage = resolveInfo.activityInfo.packageName
                        val activityName = resolveInfo.activityInfo.name.lowercase()
                        
                        // Must match package name exactly
                        activityPackage == packageName && 
                        // Exclude Play Store
                        !activityPackage.contains("com.android.vending") &&
                        !activityPackage.contains("play.google") &&
                        // Exclude activities that look like Play Store redirects
                        !activityName.contains("play") &&
                        !activityName.contains("store") &&
                        !activityName.contains("install") &&
                        // Exclude activities that are not main launcher activities
                        resolveInfo.activityInfo.exported
                    }
                    
                    if (validActivities.isEmpty()) {
                        android.util.Log.w("SmartHomeManager", "No valid activities found (all are Play Store): $packageName")
                        continue
                    }
                    
                    // Step 4: Create explicit Intent with first valid activity
                    val activityInfo = validActivities[0].activityInfo
                    val explicitIntent = Intent().apply {
                        setClassName(packageName, activityInfo.name)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    
                    // Step 5: Final verification before launching
                    val resolveInfo = packageManager.resolveActivity(
                        explicitIntent, 
                        android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                    )
                    
                    if (resolveInfo != null && resolveInfo.activityInfo.packageName == packageName) {
                        try {
                            context.startActivity(explicitIntent)
                            android.util.Log.i("SmartHomeManager", "✅ Successfully opened Google Home: $packageName/${activityInfo.name}")
                            return true
                        } catch (e: android.content.ActivityNotFoundException) {
                            android.util.Log.e("SmartHomeManager", "ActivityNotFoundException: ${e.message}")
                            continue
                        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                            android.util.Log.e("SmartHomeManager", "NameNotFoundException: ${e.message}")
                            continue
                        }
                    } else {
                        android.util.Log.w("SmartHomeManager", "Resolve check failed: ${resolveInfo?.activityInfo?.packageName} vs $packageName")
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("SmartHomeManager", "Error processing package $packageName: ${e.message}", e)
                }
            }
            
            android.util.Log.w("SmartHomeManager", "❌ Google Home app not found. Checked: $GOOGLE_HOME_PACKAGE_ALT")
            false
        } catch (e: Exception) {
            android.util.Log.e("SmartHomeManager", "Failed to open Google Home app: ${e.message}", e)
            false
        }
    }
    
    /**
     * Checks if Google Home app is installed.
     * Uses multiple methods to verify installation.
     */
    fun isGoogleHomeAppInstalled(): Boolean {
        return try {
            android.util.Log.d("SmartHomeManager", "Checking Google Home app installation...")
            
            // Check all possible package names
            for (packageName in GOOGLE_HOME_PACKAGE_ALT) {
                try {
                    android.util.Log.d("SmartHomeManager", "Checking package: $packageName")
                    
                    // Method 1: Try to get package info directly (most reliable)
                    try {
                        val packageInfo = context.packageManager.getPackageInfo(
                            packageName, 
                            android.content.pm.PackageManager.GET_ACTIVITIES
                        )
                        android.util.Log.i("SmartHomeManager", "✅ Google Home app FOUND: $packageName (via package info)")
                        return true
                    } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                        android.util.Log.d("SmartHomeManager", "Package not found: $packageName (NameNotFoundException)")
                        // Package not found, continue to next method
                    }
                    
                    // Method 2: Try to get launch intent and verify it's not Play Store
                    val homeAppIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                    if (homeAppIntent != null) {
                        val component = homeAppIntent.component
                        // Verify that intent is for the actual app, not Play Store
                        if (component != null && 
                            component.packageName == packageName &&
                            !component.packageName.contains("com.android.vending") &&
                            !component.packageName.contains("play.google")) {
                            android.util.Log.i("SmartHomeManager", "✅ Google Home app FOUND: $packageName (via launch intent)")
                            return true
                        } else {
                            android.util.Log.d("SmartHomeManager", "Launch intent is for Play Store or wrong package: ${component?.packageName}")
                        }
                    } else {
                        android.util.Log.d("SmartHomeManager", "Launch intent is null for: $packageName")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("SmartHomeManager", "Error checking package $packageName: ${e.message}")
                }
            }
            
            // Method 3: Try to find by app name (in case package name is different)
            // This searches through installed apps to find Google Home by name
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveList = context.packageManager.queryIntentActivities(intent, 0)
                
                for (resolveInfo in resolveList) {
                    val appName = resolveInfo.loadLabel(context.packageManager).toString().lowercase()
                    val packageName = resolveInfo.activityInfo.packageName
                    
                    // Check if app name contains "google home" or "home" and is from Google
                    if (packageName.startsWith("com.google") && 
                        (appName.contains("google home") || 
                         (appName.contains("home") && appName.contains("google")))) {
                        android.util.Log.i("SmartHomeManager", "✅ Google Home app FOUND by name: $packageName ($appName)")
                        return true
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("SmartHomeManager", "Error searching by app name: ${e.message}")
            }
            
            android.util.Log.w("SmartHomeManager", "❌ Google Home app NOT FOUND. Checked packages: $GOOGLE_HOME_PACKAGE_ALT")
            false
        } catch (e: Exception) {
            android.util.Log.e("SmartHomeManager", "Error checking Google Home app installation: ${e.message}", e)
            false
        }
    }
    
    /**
     * Checks if Gemini or Google Assistant is available on this device.
     */
    fun isGoogleAssistantAvailable(): Boolean {
        // Check for Gemini
        val geminiIntent = context.packageManager.getLaunchIntentForPackage(GEMINI_PACKAGE)
        if (geminiIntent != null) {
            return true
        }
        
        // Check for Google Assistant via voice command
        val voiceIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (voiceIntent.resolveActivity(context.packageManager) != null) {
            return true
        }
        
        // Check for Google Assistant via deep link
        val assistantIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("googleassistant://")
            setPackage(GOOGLE_APP_PACKAGE)
        }
        return assistantIntent.resolveActivity(context.packageManager) != null
    }
}

