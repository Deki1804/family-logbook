package com.familylogbook.app.domain.classifier

/**
 * Helper object for formatting voice input as shopping lists.
 * Automatically adds commas between items when shopping list keywords are detected.
 */
object ShoppingListFormatter {
    
    // Keywords that indicate this is a shopping list
    private val shoppingListKeywords = listOf(
        "popis", "lista", "spisak",
        "kupiti", "kupit", "kupi", "kupio", "kupila", "kupujemo", "kupovina",
        "treba kupiti", "treba kupit", "trebam kupiti", "trebamo kupiti",
        "za kupiti", "za kupit", "mogu kupiti",
        "shopping", "shopping list", "grocery list", "grocery",
        "namirnice", "dućan", "trgovina", "shop", "store",
        "hrana", "hranu", "za pse", "pse", "psa", "psi",
        "buy", "need to buy", "purchase"
    )
    
    /**
     * Checks if the text starts with shopping list keywords.
     * Returns true if it looks like the user is starting a shopping list.
     */
    fun startsWithShoppingKeyword(text: String): Boolean {
        val lowerText = text.lowercase().trim()
        return shoppingListKeywords.any { keyword ->
            lowerText.startsWith(keyword) || 
            lowerText.contains("$keyword ", ignoreCase = true) ||
            lowerText.contains(" $keyword ", ignoreCase = true) ||
            // Dodatno: provjeri da li keyword dolazi prije bilo koje stavke (prvih 20 karaktera)
            lowerText.length > keyword.length && lowerText.substring(0, minOf(20 + keyword.length, lowerText.length)).contains(keyword, ignoreCase = true)
        }
    }
    
    /**
     * Checks if text contains shopping keywords anywhere (not just at start).
     */
    fun containsShoppingKeyword(text: String): Boolean {
        val lowerText = text.lowercase().trim()
        return shoppingListKeywords.any { keyword ->
            lowerText.contains(keyword, ignoreCase = true)
        }
    }
    
    /**
     * Formats voice input text as a shopping list by adding commas between items.
     * Removes shopping keywords and formats the remaining text.
     * 
     * Examples:
     * "treba kupiti mlijeko kruh jaja" -> "mlijeko, kruh, jaja"
     * "popis kupovine mlijeko kruh jaja" -> "mlijeko, kruh, jaja"
     * "mlijeko kruh jaja" -> "mlijeko, kruh, jaja" (if startsWithShopping is true)
     */
    fun formatAsShoppingList(text: String, startsWithShopping: Boolean = false): String {
        var cleanedText = text.trim()
        
        // Remove shopping keywords from the beginning
        if (startsWithShopping) {
            for (keyword in shoppingListKeywords.sortedByDescending { it.length }) {
                val lowerKeyword = keyword.lowercase()
                // Remove from start
                if (cleanedText.lowercase().startsWith(lowerKeyword)) {
                    cleanedText = cleanedText.substring(keyword.length).trim()
                    // Also remove common connecting words
                    cleanedText = cleanedText.removePrefix(":").trim()
                    cleanedText = cleanedText.removePrefix("-").trim()
                    break
                }
                // Remove if it's in the middle
                val regex = Regex("\\b${Regex.escape(lowerKeyword)}\\s*:?\\s*-?\\s*", RegexOption.IGNORE_CASE)
                cleanedText = regex.replace(cleanedText, "").trim()
            }
        }
        
        // Split by existing commas, newlines, or multiple spaces
        var items = cleanedText
            .split("\n", ",")
            .flatMap { it.split(Regex("\\s{2,}")) } // Split by multiple spaces
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        // If we only have one item or no items, try to split by single spaces
        // but only if it looks like multiple items (3+ words)
        val words = cleanedText.split(Regex("\\s+")).filter { it.isNotBlank() }
        
        // If no commas/newlines found and we have multiple words, split intelligently
        val formattedItems = if (items.size <= 1 && words.size >= 2 && !cleanedText.contains(",") && !cleanedText.contains("\n")) {
            // Likely multiple items without separators - split by words
            // Changed threshold from 3 to 2 words to handle cases like "mlijeko kruh"
            // But try to keep common phrases together
            splitIntoItems(words)
        } else if (items.size == 1 && words.size >= 2) {
            // One item but multiple words - split intelligently
            // Changed threshold from 3 to 2 words
            splitIntoItems(words)
        } else {
            items
        }
        
        // Filter out empty items and join with commas
        return formattedItems
            .filter { it.isNotEmpty() }
            .joinToString(", ")
    }
    
    /**
     * Intelligently splits words into items, keeping common phrases together.
     * For example: "mlijeko kruh domaći sir" -> ["mlijeko", "kruh", "domaći sir"]
     */
    private fun splitIntoItems(words: List<String>): List<String> {
        if (words.size <= 2) {
            return words
        }
        
        val items = mutableListOf<String>()
        
        // Common shopping phrases that should stay together
        val commonPhrases = listOf(
            "domaći sir", "domaci sir",
            "mlijeko u prahu",
            "pšenično brašno", "pšenično brasno", "bijelo brašno", "bijelo brasno",
            "jaja domaća", "jaja domaca",
            "maslac domaći", "maslac domaci",
            "kruh bijeli", "kruh crni", "kruh integralni",
            "sok od", "sok od naranče", "sok od jabuke",
            "kava instant", "kava turska",
            "ulje maslinovo", "ulje suncokretovo",
            "šećer bijeli", "secer bijeli",
            "sol morska"
        )
        
        var i = 0
        while (i < words.size) {
            var item = words[i]
            var foundPhrase = false
            var consumedWords = 1
            
            // Try to match common phrases (2-3 words)
            for (phraseLength in 3 downTo 2) {
                if (i + phraseLength <= words.size) {
                    val potentialPhrase = words.subList(i, i + phraseLength).joinToString(" ").lowercase()
                    val matchedPhrase = commonPhrases.find { phrase -> 
                        potentialPhrase.contains(phrase.lowercase())
                    }
                    
                    if (matchedPhrase != null) {
                        // Found a matching phrase - use it as one item
                        item = words.subList(i, i + phraseLength).joinToString(" ")
                        consumedWords = phraseLength
                        foundPhrase = true
                        break
                    }
                }
            }
            
            if (!foundPhrase) {
                // Single word item - but check if next word is a preposition/connector
                // Prepositions that might indicate a phrase: "od", "u", "za", "sa", "s"
                val prepositions = listOf("od", "u", "za", "sa", "s", "na", "do", "iz")
                
                if (i + 1 < words.size) {
                    val nextWord = words[i + 1].lowercase()
                    if (prepositions.contains(nextWord) && i + 2 < words.size) {
                        // Three-word phrase: "word + preposition + word"
                        item = "${words[i]} ${words[i + 1]} ${words[i + 2]}"
                        consumedWords = 3
                    } else if (prepositions.contains(nextWord)) {
                        // Two-word phrase: "word + preposition"
                        item = "${words[i]} ${words[i + 1]}"
                        consumedWords = 2
                    }
                }
            }
            
            if (item.isNotBlank()) {
                items.add(item)
            }
            
            i += consumedWords
        }
        
        // Fallback: if we couldn't intelligently split and have too few items,
        // split more aggressively (every 1-2 words)
        if (items.size < 2 && words.size >= 3) {
            return words.chunked(2).map { it.joinToString(" ") }
        }
        
        return items
    }
    
    /**
     * Processes voice input text and formats it as shopping list if shopping keywords are detected.
     * Returns the formatted text.
     */
    fun processVoiceInput(text: String): String {
        val startsWithShopping = startsWithShoppingKeyword(text)
        val containsShopping = containsShoppingKeyword(text)
        
        // If it starts with shopping keyword, format as shopping list
        if (startsWithShopping) {
            return formatAsShoppingList(text, startsWithShopping = true)
        }
        
        // If it contains shopping keyword anywhere, format as shopping list
        if (containsShopping) {
            return formatAsShoppingList(text, startsWithShopping = false)
        }
        
        // Check if it might be a shopping list anyway (no keywords but looks like a list)
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        
        // If multiple words without separators, check if it looks like a shopping list
        if (words.size >= 3 && !text.contains(",") && !text.contains("\n")) {
            // Check if words are short and don't form sentences
            val averageWordLength = words.map { it.length }.average()
            val hasVerbs = text.lowercase().let { lower ->
                val verbTokens = listOf(
                    "je", "sam", "si", "smo", "ste", "su",
                    "bio", "bila", "bilo", "bili", "bile",
                    "imam", "imamo", "imate", "imaju",
                    "bio sam", "radili", "išli", "došao", "došla",
                    "dojio", "dojenje", "hranio", "hranila", "jela", "jeo", "najeo",
                    "feeding", "fed", "feed", "bottle", "breast",
                    "went", "did", "was", "were", "have", "has", "is", "are",
                    "spava", "budio", "plače", "plačao"
                )
                verbTokens.any { lower.contains(it) }
            }
            
            // If short words, no verbs, and 3+ items, likely a shopping list
            if (averageWordLength < 8 && !hasVerbs && words.size >= 3) {
                return formatAsShoppingList(text, startsWithShopping = false)
            }
        }
        
        return text
    }
}
