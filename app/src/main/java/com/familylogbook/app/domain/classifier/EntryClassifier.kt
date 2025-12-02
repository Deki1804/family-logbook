package com.familylogbook.app.domain.classifier

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.ClassifiedEntryMetadata
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.Mood

/**
 * Fake AI classifier for Phase 1.
 * Uses simple keyword matching to classify entries.
 * Later this will be replaced with a real AI backend service.
 */
class EntryClassifier {
    
    fun classifyEntry(text: String): ClassifiedEntryMetadata {
        val lowerText = text.lowercase()
        
        val category = detectCategory(lowerText)
        val mood = detectMood(lowerText)
        val tags = extractTags(lowerText, category)
        val temperature = extractTemperature(text)
        val medicine = extractMedicine(text)
        val (feedingType, feedingAmount) = extractFeeding(text)
        
        return ClassifiedEntryMetadata(
            category = category,
            tags = tags,
            mood = mood,
            temperature = temperature,
            medicineGiven = medicine,
            feedingType = feedingType,
            feedingAmount = feedingAmount
        )
    }
    
    private fun detectCategory(text: String): Category {
        // Health keywords
        val healthKeywords = listOf(
            "fever", "temperature", "sick", "ill", "medicine", "medication", "syrup",
            "doctor", "hospital", "cough", "cold", "flu", "virus", "infection",
            "vaccine", "vaccination", "pain", "ache", "hurt", "injury", "bandage",
            "tooth", "teeth", "dentist", "zub", "zubić", "temperatur", "vruć",
            "grč", "grčevi", "colic", "plače", "plač", "crying"
        )
        
        // Sleep keywords
        val sleepKeywords = listOf(
            "sleep", "slept", "asleep", "wake", "woke", "awake", "bedtime",
            "nap", "napping", "tired", "exhausted", "rest", "spavao", "spava",
            "budan", "budio", "ne spava", "trouble sleeping", "can't sleep"
        )
        
        // Feeding keywords
        val feedingKeywords = listOf(
            "feeding", "feed", "fed", "dojenje", "dojio", "dojka", "bočica",
            "bottle", "breast", "mlijeko", "milk", "hranjenje", "hranio",
            "breast_left", "breast_right", "ml"
        )
        
        // Mood keywords
        val moodKeywords = listOf(
            "mood", "happy", "sad", "angry", "excited", "calm", "anxious",
            "worried", "stressed", "relaxed", "cheerful", "grumpy", "cranky",
            "volja", "raspoloženje", "sretan", "tužan", "nervozan"
        )
        
        // Development keywords
        val developmentKeywords = listOf(
            "first", "milestone", "learned", "said", "word", "walked", "crawled",
            "stood", "clapped", "waved", "smiled", "laughed", "tooth", "teeth",
            "prvi put", "naučio", "rekao", "riječ", "korak", "razvoj"
        )
        
        // Kindergarten/School keywords
        val schoolKeywords = listOf(
            "kindergarten", "school", "teacher", "class", "homework", "project",
            "playground", "friend", "friends", "vrtić", "škola", "odgajatelj",
            "prijatelj", "maskenbal", "predstava"
        )
        
        // Auto keywords
        val autoKeywords = listOf(
            "auto", "car", "vehicle", "tire", "tyre", "guma", "servis", "service",
            "oil", "ulje", "brake", "kočnica", "engine", "motor", "battery", "baterija",
            "flat tire", "punctured", "probušena", "procurila", "registracija", "registration",
            "osiguranje", "insurance", "mileage", "kilometraža", "fuel", "gorivo"
        )
        
        // House keywords
        val houseKeywords = listOf(
            "house", "home", "kuća", "stan", "apartment", "filter", "broke", "broken",
            "fixed", "repair", "maintenance", "cleaned", "cleaning", "appliance", "uređaj",
            "pokvario", "popravio", "plumbing", "vodoinstalater", "electric", "električar",
            "heating", "grijanje", "cooling", "hladenje", "roof", "krov", "window", "prozor"
        )
        
        // Finance keywords
        val financeKeywords = listOf(
            "bill", "račun", "invoice", "račun", "payment", "plaćanje", "cost", "trošak",
            "expense", "trošak", "money", "novac", "eur", "euro", "€", "kn", "kuna",
            "electricity", "struja", "water", "voda", "gas", "plin", "internet", "phone", "telefon",
            "rent", "najam", "mortgage", "hipoteka", "loan", "kredit", "debt", "dug"
        )
        
        // Work keywords
        val workKeywords = listOf(
            "work", "posao", "job", "meeting", "sastanak", "deadline", "rok", "project", "projekt",
            "client", "klijent", "colleague", "kolega", "boss", "šef", "office", "ured",
            "presentation", "prezentacija", "report", "izvještaj", "task", "zadatak"
        )
        
        // Smart Home keywords
        val smartHomeKeywords = listOf(
            "svjetlo", "svjetla", "light", "lights", "upali", "ugasi", "turn on", "turn off",
            "rumbu", "usisavač", "vacuum", "robot", "robot usisavač", "robot vacuum",
            "klima", "air conditioning", "AC", "grijanje", "heating", "hladenje", "cooling",
            "TV", "television", "televizor", "upali tv", "ugasi tv",
            "rolete", "blinds", "zavjese", "curtains", "zatvori", "otvori",
            "muziku", "music", "speaker", "zvučnik", "pusti", "play", "stop",
            "termostat", "thermostat", "temperatura", "temperature", "postavi", "set",
            "pametna kuća", "smart home", "google home", "alexa", "assistant",
            "grijanje u autu", "car heating", "upali grijanje", "turn on heating"
        )
        
        // Shopping keywords
        val shoppingKeywords = listOf(
            "shopping", "kupovina", "buy", "kupio", "purchase", "nabava", "grocery", "namirnice",
            "store", "trgovina", "shop", "dućan", "list", "lista", "need to buy", "treba kupiti"
        )
        
        // Home keywords (legacy, keeping for backward compatibility)
        val homeKeywords = listOf(
            "filter", "broke", "broken", "fixed", "repair", "maintenance",
            "cleaned", "cleaning", "house", "home", "filter", "appliance",
            "kuća", "pokvario", "popravio", "filter"
        )
        
        when {
            // Priority order matters - more specific first
            smartHomeKeywords.any { text.contains(it) } -> return Category.SMART_HOME
            autoKeywords.any { text.contains(it) } -> return Category.AUTO
            financeKeywords.any { text.contains(it) } -> return Category.FINANCE
            houseKeywords.any { text.contains(it) } -> return Category.HOUSE
            workKeywords.any { text.contains(it) } -> return Category.WORK
            shoppingKeywords.any { text.contains(it) } -> return Category.SHOPPING
            feedingKeywords.any { text.contains(it) } -> return Category.FEEDING
            healthKeywords.any { text.contains(it) } -> return Category.HEALTH
            sleepKeywords.any { text.contains(it) } -> return Category.SLEEP
            moodKeywords.any { text.contains(it) } -> return Category.MOOD
            developmentKeywords.any { text.contains(it) } -> return Category.DEVELOPMENT
            schoolKeywords.any { text.contains(it) } -> return Category.SCHOOL
            else -> return Category.OTHER
        }
    }
    
    private fun detectMood(text: String): Mood? {
        val veryGoodKeywords = listOf(
            "very happy", "very good", "excellent", "amazing", "fantastic",
            "super", "great", "wonderful", "delighted", "ecstatic",
            "odlično", "super", "fantastično"
        )
        
        val goodKeywords = listOf(
            "good", "happy", "nice", "pleasant", "fine", "well", "better",
            "dobro", "sretan", "lijepo"
        )
        
        val badKeywords = listOf(
            "bad", "sad", "unhappy", "upset", "crying", "cried", "tears",
            "loše", "tužan", "plače", "plačao"
        )
        
        val veryBadKeywords = listOf(
            "very bad", "terrible", "awful", "horrible", "miserable",
            "very sad", "crying a lot", "very upset",
            "jako loše", "užasno", "jako tužan"
        )
        
        when {
            veryBadKeywords.any { text.contains(it) } -> return Mood.VERY_BAD
            badKeywords.any { text.contains(it) } -> return Mood.BAD
            veryGoodKeywords.any { text.contains(it) } -> return Mood.VERY_GOOD
            goodKeywords.any { text.contains(it) } -> return Mood.GOOD
            else -> return null
        }
    }
    
    private fun extractTags(text: String, category: Category): List<String> {
        val tags = mutableListOf<String>()
        
        // Extract common tags based on keywords
        val tagMap = mapOf(
            // Health tags
            "fever" to "fever",
            "temperature" to "fever",
            "tooth" to "tooth",
            "teeth" to "tooth",
            "zub" to "tooth",
            "medicine" to "medicine",
            "medication" to "medicine",
            "syrup" to "medicine",
            "doctor" to "doctor",
            // Sleep tags
            "sleep" to "sleep",
            "wake" to "wake",
            "spava" to "sleep",
            // Mood tags
            "happy" to "happy",
            "sad" to "sad",
            "mood" to "mood",
            // Development tags
            "first" to "milestone",
            "milestone" to "milestone",
            "learned" to "learning",
            "word" to "language",
            "riječ" to "language",
            // School tags
            "kindergarten" to "kindergarten",
            "school" to "school",
            "vrtić" to "kindergarten",
            // Auto tags
            "guma" to "tire",
            "tire" to "tire",
            "servis" to "service",
            "service" to "service",
            "probušena" to "flat-tire",
            "flat tire" to "flat-tire",
            "registracija" to "registration",
            "osiguranje" to "insurance",
            // House tags
            "filter" to "maintenance",
            "broke" to "repair",
            "broken" to "repair",
            "popravio" to "repair",
            "repair" to "repair",
            // Finance tags
            "račun" to "bill",
            "bill" to "bill",
            "plaćanje" to "payment",
            "payment" to "payment",
            "struja" to "electricity",
            "voda" to "water",
            // Work tags
            "sastanak" to "meeting",
            "meeting" to "meeting",
            "deadline" to "deadline",
            "rok" to "deadline",
            // Shopping tags
            "kupovina" to "shopping",
            "shopping" to "shopping",
            "lista" to "shopping-list",
            "grocery" to "grocery"
        )
        
        tagMap.forEach { (keyword, tag) ->
            if (text.contains(keyword) && !tags.contains(tag)) {
                tags.add(tag)
            }
        }
        
        // Limit to 3 tags max
        return tags.take(3)
    }
    
    private fun extractTemperature(text: String): Float? {
        // Try to find temperature patterns like "38.5", "38,5", "38", "temperatura 38.4"
        val patterns = listOf(
            Regex("(?:temperatur|temp|fever|vruć)[^0-9]*([0-9]+[.,][0-9]+)"),
            Regex("([0-9]+[.,][0-9]+)[^0-9]*(?:°|celzij|celzija|celsius)"),
            Regex("([0-9]+)[^0-9]*(?:°|celzij|celzija|celsius)"),
            Regex("([3][0-9][.,][0-9]+)"), // 30.x to 39.x range
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text.lowercase())
            if (match != null) {
                val tempStr = match.groupValues[1].replace(',', '.')
                return tempStr.toFloatOrNull()
            }
        }
        
        return null
    }
    
    private fun extractMedicine(text: String): String? {
        val lowerText = text.lowercase()
        val medicineKeywords = listOf(
            "sirup", "syrup", "paracetamol", "ibuprofen", "panadol", "brufen",
            "lijek", "medicine", "medication", "tableta", "tablet"
        )
        
        // Simple extraction - if medicine keywords are present, try to extract the medicine name
        if (medicineKeywords.any { lowerText.contains(it) }) {
            // Try to find medicine name after keywords
            for (keyword in medicineKeywords) {
                val index = lowerText.indexOf(keyword)
                if (index != -1) {
                    // Try to extract a word or phrase after the keyword
                    val afterKeyword = text.substring(index + keyword.length).trim()
                    val words = afterKeyword.split(Regex("[\\s,.]"))
                    if (words.isNotEmpty() && words[0].length > 2) {
                        return words[0].take(50) // Limit length
                    }
                    return keyword // Fallback to keyword itself
                }
            }
        }
        
        return null
    }
    
    private fun extractFeeding(text: String): Pair<FeedingType?, Int?> {
        val lowerText = text.lowercase()
        
        // Detect feeding type
        val feedingType = when {
            lowerText.contains("lijeva") || lowerText.contains("left") -> FeedingType.BREAST_LEFT
            lowerText.contains("desna") || lowerText.contains("right") -> FeedingType.BREAST_RIGHT
            lowerText.contains("bočic") || lowerText.contains("bottle") -> FeedingType.BOTTLE
            lowerText.contains("dojenje") || lowerText.contains("breast") -> {
                // Default to left if not specified
                FeedingType.BREAST_LEFT
            }
            else -> null
        }
        
        // Extract amount for bottle feeding
        val amount = if (feedingType == FeedingType.BOTTLE) {
            val mlPattern = Regex("([0-9]+)\\s*(?:ml|mL|mililitar)")
            val match = mlPattern.find(lowerText)
            match?.groupValues?.get(1)?.toIntOrNull()
        } else {
            null
        }
        
        return Pair(feedingType, amount)
    }
}

