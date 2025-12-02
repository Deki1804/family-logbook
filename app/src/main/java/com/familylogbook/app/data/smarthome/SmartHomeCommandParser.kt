package com.familylogbook.app.data.smarthome

/**
 * Parses user text input and converts it to Google Assistant command format.
 * 
 * Example:
 * "Upali rumbu" → "start the vacuum cleaner"
 * "Ugasi svjetlo u kuhinji" → "turn off the lights in the kitchen"
 */
object SmartHomeCommandParser {
    
    /**
     * Parses user input and generates a Google Assistant command string.
     * Returns null if the command cannot be parsed.
     */
    fun parseCommand(userText: String): String? {
        val lowerText = userText.lowercase()
        
        // Vacuum / Robot vacuum
        if (lowerText.contains("rumbu") || lowerText.contains("usisavač") || 
            lowerText.contains("vacuum") || lowerText.contains("robot")) {
            return when {
                lowerText.contains("upali") || lowerText.contains("pokreni") || 
                lowerText.contains("start") || lowerText.contains("turn on") -> 
                    "start the vacuum cleaner"
                lowerText.contains("ugasi") || lowerText.contains("zaustavi") || 
                lowerText.contains("stop") || lowerText.contains("turn off") -> 
                    "stop the vacuum cleaner"
                else -> "start the vacuum cleaner" // Default
            }
        }
        
        // Lights
        if (lowerText.contains("svjetlo") || lowerText.contains("svjetla") || 
            lowerText.contains("light") || lowerText.contains("lights")) {
            val room = extractRoom(lowerText)
            return when {
                lowerText.contains("upali") || lowerText.contains("turn on") -> 
                    if (room != null) "turn on the lights in the $room" else "turn on the lights"
                lowerText.contains("ugasi") || lowerText.contains("turn off") -> 
                    if (room != null) "turn off the lights in the $room" else "turn off the lights"
                else -> if (room != null) "turn on the lights in the $room" else "turn on the lights"
            }
        }
        
        // Air Conditioning / Climate
        if (lowerText.contains("klima") || lowerText.contains("ac") || 
            lowerText.contains("air conditioning") || lowerText.contains("hladenje") || 
            lowerText.contains("grijanje") || lowerText.contains("heating")) {
            val temperature = extractTemperature(lowerText)
            return when {
                lowerText.contains("upali") || lowerText.contains("turn on") -> 
                    if (temperature != null) "set the air conditioning to $temperature degrees" 
                    else "turn on the air conditioning"
                lowerText.contains("ugasi") || lowerText.contains("turn off") -> 
                    "turn off the air conditioning"
                temperature != null -> 
                    "set the air conditioning to $temperature degrees"
                else -> "turn on the air conditioning"
            }
        }
        
        // TV
        if (lowerText.contains("tv") || lowerText.contains("televizor") || 
            lowerText.contains("television")) {
            return when {
                lowerText.contains("upali") || lowerText.contains("turn on") -> 
                    "turn on the TV"
                lowerText.contains("ugasi") || lowerText.contains("turn off") -> 
                    "turn off the TV"
                else -> "turn on the TV"
            }
        }
        
        // Blinds / Curtains
        if (lowerText.contains("rolete") || lowerText.contains("blinds") || 
            lowerText.contains("zavjese") || lowerText.contains("curtains")) {
            val room = extractRoom(lowerText)
            return when {
                lowerText.contains("zatvori") || lowerText.contains("close") -> 
                    if (room != null) "close the blinds in the $room" else "close the blinds"
                lowerText.contains("otvori") || lowerText.contains("open") -> 
                    if (room != null) "open the blinds in the $room" else "open the blinds"
                else -> if (room != null) "close the blinds in the $room" else "close the blinds"
            }
        }
        
        // Music / Speaker
        if (lowerText.contains("muziku") || lowerText.contains("music") || 
            lowerText.contains("zvučnik") || lowerText.contains("speaker")) {
            val room = extractRoom(lowerText)
            return when {
                lowerText.contains("pusti") || lowerText.contains("play") -> 
                    if (room != null) "play music in the $room" else "play music"
                lowerText.contains("zaustavi") || lowerText.contains("stop") -> 
                    if (room != null) "stop music in the $room" else "stop music"
                else -> if (room != null) "play music in the $room" else "play music"
            }
        }
        
        // Generic smart home command - try to extract action and device
        val action = extractAction(lowerText)
        val device = extractDevice(lowerText)
        
        if (action != null && device != null) {
            return "$action $device"
        }
        
        return null
    }
    
    private fun extractRoom(text: String): String? {
        val rooms = mapOf(
            "kuhinja" to "kitchen",
            "dnevna" to "living room",
            "spavaća" to "bedroom",
            "kupaonica" to "bathroom",
            "hodnik" to "hallway",
            "soba" to "room"
        )
        
        rooms.forEach { (hr, en) ->
            if (text.contains(hr)) return en
        }
        
        return null
    }
    
    private fun extractTemperature(text: String): Int? {
        val pattern = Regex("(\\d+)\\s*(?:°|degrees?|stupnjev?|celzij)")
        val match = pattern.find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
    
    private fun extractAction(text: String): String? {
        return when {
            text.contains("upali") || text.contains("pokreni") || text.contains("turn on") || text.contains("start") -> "turn on"
            text.contains("ugasi") || text.contains("zaustavi") || text.contains("turn off") || text.contains("stop") -> "turn off"
            text.contains("pusti") || text.contains("play") -> "play"
            text.contains("zatvori") || text.contains("close") -> "close"
            text.contains("otvori") || text.contains("open") -> "open"
            text.contains("postavi") || text.contains("set") -> "set"
            else -> null
        }
    }
    
    private fun extractDevice(text: String): String? {
        return when {
            text.contains("svjetlo") || text.contains("light") -> "the lights"
            text.contains("klima") || text.contains("ac") || text.contains("air conditioning") -> "the air conditioning"
            text.contains("tv") || text.contains("television") -> "the TV"
            text.contains("rumbu") || text.contains("vacuum") -> "the vacuum cleaner"
            text.contains("rolete") || text.contains("blinds") -> "the blinds"
            text.contains("muziku") || text.contains("music") -> "music"
            else -> null
        }
    }
}

