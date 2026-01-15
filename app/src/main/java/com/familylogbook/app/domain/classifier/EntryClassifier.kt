package com.familylogbook.app.domain.classifier

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.ClassifiedEntryMetadata
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.vaccination.VaccinationCalendar

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
        
        // Extract medicine info if category is MEDICINE or HEALTH
        val temperature = if (category == Category.HEALTH || category == Category.SYMPTOM) extractTemperature(text) else null
        val medicine = if (category == Category.MEDICINE || category == Category.HEALTH) extractMedicine(text) else null
        val medicineInterval = if (category == Category.MEDICINE || category == Category.HEALTH) extractMedicineInterval(text) else null
        
        // ONLY extract feeding info if category is FEEDING (not for shopping lists or other categories)
        val (feedingType, feedingAmount) = if (category == Category.FEEDING) extractFeeding(text) else Pair(null, null)
        
        val shoppingItems = if (category == Category.SHOPPING) extractShoppingItems(text) else null
        
        // Extract vaccination info if detected
        val vaccinationName = if (category == Category.VACCINATION || category == Category.HEALTH) {
            VaccinationCalendar.extractVaccinationName(text)
        } else {
            null
        }

        // Extract reminder date for DAY (and other reminder-like entries)
        // NOTE: this was previously implemented but never wired into classifyEntry().
        val reminderDate = if (
            category == Category.DAY ||
            lowerText.contains("podsjet") ||
            lowerText.contains("termin") ||
            lowerText.contains("pregled") ||
            lowerText.contains("sutra") ||
            lowerText.contains("za ")
        ) {
            extractReminderDate(text)
        } else {
            null
        }
        
        return ClassifiedEntryMetadata(
            category = category,
            tags = tags,
            mood = mood,
            temperature = temperature,
            medicineGiven = medicine,
            medicineIntervalHours = medicineInterval,
            feedingType = feedingType,
            feedingAmount = feedingAmount,
            reminderDate = reminderDate,
            shoppingItems = shoppingItems,
            vaccinationName = vaccinationName,
            nextVaccinationDate = null, // Will be calculated in AddEntryViewModel based on child's age
            nextVaccinationMessage = null
        )
    }
    
    private fun detectCategory(text: String): Category {
        // Parent OS Core Categories - PRIORITY ORDER MATTERS!
        
        // MEDICINE keywords - specific medicine names and medicine-related actions
        val medicineKeywords = listOf(
            "medicine", "medication", "lijek", "lijekovi", "sirup", "syrup", "tableta", "tablet",
            "nurofen", "ibuprofen", "brufen", "dalsy", "paracetamol", "panadol", "acetaminophen",
            "tylenol", "andol", "aspirin", "antibiotik", "antibiotic", "amoxicillin", "amoksicilin",
            "penicillin", "penicilin", "kapsula", "capsule", "drops", "kapi", "injekcija", "injection",
            "dao lijek", "dali lijek", "dali lijekove", "gave medicine", "took medicine", "uzeli lijek",
            "popio", "popila", "popila", "pio", "pila", "uzela", "uzeo", "uzeli", "uzela lijek", "uzeo lijek"
        )
        
        // SYMPTOM keywords - symptoms and health issues
        val symptomKeywords = listOf(
            "fever", "temperature", "temperatura", "temperaturu", "temperatur", "vrućica",
            "cough", "cold", "flu", "virus", "infection", "kašalj", "prehlada", "gripa",
            "pain", "ache", "hurt", "injury", "bandage", "bol", "boli", "bolest",
            "tooth", "teeth", "dentist", "zub", "zubić", "zubobolja",
            "grč", "grčevi", "colic", "plače", "plač", "crying", "kolika",
            "rash", "osip", "vomiting", "povraćanje", "diarrhea", "proljev", "constipation", "zatvor",
            "runny nose", "curek", "sneezing", "kihanje", "sore throat", "bol u grlu"
        )
        
        // VACCINATION keywords
        val vaccinationKeywords = listOf(
            "vaccine", "vaccination", "vakcina", "cjepivo", "cjepiv", "primio cjepivo", "primila cjepivo",
            "vaccinated", "cjepio", "cjepila", "cjepiti", "vakcinacija", "cjepiva"
        )
        
        // DAY keywords - daily routines, tasks, checklists
        val dayKeywords = listOf(
            "rutina", "routine", "dnevna obaveza", "daily task", "checklist", "lista obaveza",
            "task", "zadatak", "obaveza", "todo", "to do", "napraviti", "treba", "need to",
            "morning routine", "jutarnja rutina", "evening routine", "večernja rutina",
            "bedtime routine", "rutina prije spavanja", "playtime", "vrijeme za igru",
            "bath time", "vrijeme za kupanje", "meal time", "vrijeme za jelo"
        )
        
        // Health keywords (general health, doctor visits, etc.)
        val healthKeywords = listOf(
            "sick", "ill", "doctor", "hospital", "doktor", "bolnica", "pregled", "checkup",
            "visit", "posjeta", "appointment", "termin"
        )
        
        // Sleep keywords
        val sleepKeywords = listOf(
            "sleep", "slept", "asleep", "wake", "woke", "awake", "bedtime",
            "nap", "napping", "tired", "exhausted", "rest", "spavao", "spava",
            "budan", "budio", "ne spava", "trouble sleeping", "can't sleep"
        )
        
        // Feeding keywords - eksplicitni glagoli i akcije, NE samo namirnice
        val feedingKeywords = listOf(
            "feeding", "feed", "fed", "dojenje", "dojio", "dojka", "bočica",
            "bottle", "breast", "hranjenje", "hranio", "najeo", "jela", "jeo",
            "breast_left", "breast_right", "ml", "napit", "pio", "pila"
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
        
        // Legacy categories (kept for backward compatibility, but low priority)
        // These will be removed from UI in future versions
        val autoKeywords = listOf(
            "auto", "car", "vehicle", "tire", "tyre", "guma", "servis", "service",
            "oil", "ulje", "brake", "kočnica", "engine", "motor", "battery", "baterija",
            "flat tire", "punctured", "probušena", "procurila", "registracija", "registration",
            "osiguranje", "insurance", "mileage", "kilometraža", "fuel", "gorivo"
        )
        
        val houseKeywords = listOf(
            "house", "home", "kuća", "stan", "apartment", "filter", "broke", "broken",
            "fixed", "repair", "maintenance", "cleaned", "cleaning", "appliance", "uređaj",
            "pokvario", "popravio", "plumbing", "vodoinstalater", "electric", "električar",
            "heating", "grijanje", "cooling", "hladenje", "roof", "krov", "window", "prozor"
        )
        
        val financeKeywords = listOf(
            "bill", "račun", "invoice", "račun", "payment", "plaćanje", "cost", "trošak",
            "expense", "trošak", "money", "novac", "eur", "euro", "€", "kn", "kuna",
            "electricity", "struja", "water", "voda", "gas", "plin", "internet", "phone", "telefon",
            "rent", "najam", "mortgage", "hipoteka", "loan", "kredit", "debt", "dug"
        )
        
        val workKeywords = listOf(
            "work", "posao", "job", "meeting", "sastanak", "deadline", "rok", "project", "projekt",
            "client", "klijent", "colleague", "kolega", "boss", "šef", "office", "ured",
            "presentation", "prezentacija", "report", "izvještaj", "task", "zadatak"
        )
        
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
        
        val shoppingKeywords = listOf(
            "shopping", "kupovina", "grocery", "namirnice",
            "store", "trgovina", "shop", "dućan",
            "buy", "kupiti", "kupit", "kupi", "kupio", "kupujemo", "purchase", "nabava",
            "list", "lista", "spisak", "popis", "need to buy", "treba kupiti", "za kupiti",
            "psi", "pse", "psa", "hrana za pse", "krokice", "cigare", "salame"
        )
        
        when {
            // Parent OS Core Categories - HIGHEST PRIORITY
            medicineKeywords.any { text.contains(it) } -> return Category.MEDICINE
            vaccinationKeywords.any { text.contains(it) } -> return Category.VACCINATION
            symptomKeywords.any { text.contains(it) } -> return Category.SYMPTOM
            dayKeywords.any { text.contains(it) } -> return Category.DAY
            
            // Parent OS Health & Wellness Categories
            feedingKeywords.any { text.contains(it) } -> return Category.FEEDING
            healthKeywords.any { text.contains(it) } -> return Category.HEALTH
            sleepKeywords.any { text.contains(it) } -> return Category.SLEEP
            moodKeywords.any { text.contains(it) } -> return Category.MOOD
            developmentKeywords.any { text.contains(it) } -> return Category.DEVELOPMENT
            schoolKeywords.any { text.contains(it) } -> return Category.SCHOOL
            
            // Legacy categories (low priority, kept for backward compatibility)
            // These will be removed from UI in future versions
            looksLikeShoppingList(text) -> return Category.SHOPPING
            smartHomeKeywords.any { text.contains(it) } -> return Category.SMART_HOME
            autoKeywords.any { text.contains(it) } -> return Category.AUTO
            financeKeywords.any { text.contains(it) } -> return Category.FINANCE
            houseKeywords.any { text.contains(it) } -> return Category.HOUSE
            shoppingKeywords.any { text.contains(it) } -> return Category.SHOPPING
            workKeywords.any { text.contains(it) } -> return Category.WORK
            
            else -> return Category.OTHER
        }
    }

    /**
     * Heuristika za prepoznavanje popisa namirnica:
     * - puno zareza ili novih redova
     * - kratke riječi (1–3 riječi po stavci)
     * - gotovo bez glagola
     * - ILI sadrži shopping keyword-e
     */
    private fun looksLikeShoppingList(text: String): Boolean {
        // Ako je već jako dugačak narativni tekst, preskoči
        if (text.length > 200) return false
        
        // Provjeri da li tekst sadrži shopping keyword-e (možda je već formatiran)
        val shoppingKeywords = listOf(
            "shopping", "kupovina", "grocery", "namirnice",
            "store", "trgovina", "shop", "dućan",
            "buy", "kupiti", "kupit", "kupi", "kupio", "kupujemo", "purchase", "nabava",
            "list", "lista", "spisak", "popis", "need to buy", "treba kupiti", "za kupiti"
        )
        
        val lowerText = text.lowercase()
        val hasShoppingKeyword = shoppingKeywords.any { lowerText.contains(it) }
        
        // Ako ima shopping keyword, provjeri da li ima barem 1-2 stavke nakon toga
        if (hasShoppingKeyword) {
            // Razbij po zarezima ili razmacima da provjerimo koliko stavki ima
            val items = text
                .replace(";", ",")
                .split("\n", ",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .filter { item ->
                    // Filtrirati keyword-e
                    val lowerItem = item.lowercase()
                    !shoppingKeywords.any { keyword -> lowerItem.contains(keyword) }
                }
            
            // Ako ima barem 2 stavke nakon keyword-a, ili ako ima zareze i barem 2 stavke
            if (items.size >= 2 || (text.contains(",") && items.size >= 1)) {
                return true
            }
        }

        // Razbij po novom redu i zarezima
        val rawItems = text
            .replace(";", ",")
            .split("\n", ",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            // Filtrirati shopping keyword-e da ne bi bili računati kao stavke
            .filter { item ->
                val lowerItem = item.lowercase()
                !shoppingKeywords.any { keyword -> lowerItem.contains(keyword) }
            }

        // Ako ima manje od 2 stavke, nije lista (minimalno "kruh, mlijeko")
        if (rawItems.size < 2) return false

        // Riječi koje često označavaju glagole / rečenice (ono što NE želimo)
        val verbLikeTokens = listOf(
            "je", "sam", "si", "smo", "ste", "su",
            "bio", "bila", "bilo", "bili", "bile",
            "imam", "imamo", "imate", "imaju",
            "bio sam", "radili", "išli", "dosao", "došao",
            "dojio", "dojenje", "hranio", "jela", "jeo", "najeo",
            "feeding", "fed", "feed", "bottle", "breast",
            "went", "did", "was", "were", "have", "has", "is", "are"
        )

        var sentenceLikeCount = 0

        for (item in rawItems) {
            val words = item.split(" ").filter { it.isNotBlank() }
            // Ako je previše riječi, vjerojatno nije samo "mlijeko, kruh..."
            if (words.size > 4) {
                sentenceLikeCount++
                continue
            }

            // Ako sadrži glagolske fraze (feeding akcije), tretiraj kao rečenicu
            if (verbLikeTokens.any { token -> item.lowercase().contains(token) }) {
                sentenceLikeCount++
            }
        }

        // Ako je većina stavki kratka i bez glagola → shopping lista
        // Povećavamo prag - ako je manje od 50% rečenica, to je lista (strožiji kriterij)
        val listSize = rawItems.size
        val sentenceRatio = sentenceLikeCount.toDouble() / listSize.toDouble()
        
        // Dodatna provjera: ako su sve stavke kratke (1-3 riječi), vjerojatno je lista
        val shortItemsCount = rawItems.count { it.split(" ").filter { w -> w.isNotBlank() }.size <= 3 }
        val shortItemsRatio = shortItemsCount.toDouble() / listSize.toDouble()
        
        // Ako su sve stavke kratke (npr. "mlijeko, kolač, govno"), to je sigurno lista
        if (shortItemsRatio >= 0.8) {
            return true
        }
        
        return sentenceRatio < 0.3 // Ako je manje od 30% rečenica, to je lista
    }
    
    /**
     * Extracts shopping items from text (for SHOPPING category).
     * Splits text by commas, semicolons, or newlines and filters valid items.
     */
    private fun extractShoppingItems(text: String): List<String>? {
        // Razbij po novom redu, zarezima i točka-zarezima
        val rawItems = text
            .replace(";", ",")
            .replace(" i ", ",") // Handle "kruh i mlijeko" format
            .split("\n", ",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { item ->
                // Filter out common prefixes/verbs that indicate it's not a simple item
                val lowerItem = item.lowercase()
                !lowerItem.startsWith("treba") && 
                !lowerItem.startsWith("need") &&
                !lowerItem.startsWith("kupiti") &&
                !lowerItem.startsWith("buy") &&
                !lowerItem.contains("lista") &&
                !lowerItem.contains("list") &&
                !lowerItem.contains("itd") && // Filter "itd" (etc.)
                !lowerItem.contains("etc")
            }
        
        // Return items if we found at least 1 valid item
        return if (rawItems.isNotEmpty()) rawItems else null
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
        val knownMedicines = listOf(
            "nurofen", "ibuprofen", "brufen", "dalsy",
            "paracetamol", "panadol", "acetaminophen", "tylenol",
            "andol", "aspirin"
        )
        
        val genericKeywords = listOf("medicine", "medication", "lijek", "sirup", "syrup", "tableta", "tablet")
        
        // Prefer known medicine names directly
        val foundKnown = knownMedicines.firstOrNull { lowerText.contains(it) }
        if (foundKnown != null) return foundKnown
        
        // Fallback: try to grab the word after generic keywords
        if (genericKeywords.any { lowerText.contains(it) }) {
            for (keyword in genericKeywords) {
                val index = lowerText.indexOf(keyword)
                if (index != -1) {
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
    
    private fun extractMedicineInterval(text: String): Int? {
        val lowerText = text.lowercase()
        
        // First, try to extract explicit interval from text
        val patterns = listOf(
            Regex("svakih\\s+(\\d+)\\s*(?:sati|h|hours|hora)"),
            Regex("every\\s+(\\d+)\\s*(?:hours|h)"),
            Regex("(\\d+)\\s*(?:h|hours|sati)\\s*(?:kasnije|later|after)"),
            Regex("(\\d+)\\s*(?:h|hours|sati)\\s*(?:interval|intervala)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(lowerText)
            if (match != null) {
                val hours = match.groupValues[1].toIntOrNull()
                if (hours != null && hours > 0 && hours <= 24) {
                    return hours
                }
            }
        }
        
        // If no explicit interval, use AI knowledge of common medicines
        // Paracetamol / Panadol / Acetaminophen - usually every 4-6 hours
        if (lowerText.contains("paracetamol") || lowerText.contains("panadol") || 
            lowerText.contains("acetaminophen") || lowerText.contains("tylenol")) {
            return 6
        }
        
        // Ibuprofen / Brufen / Advil - usually every 6-8 hours
        if (lowerText.contains("ibuprofen") || lowerText.contains("brufen") || 
            lowerText.contains("advil") || lowerText.contains("nurofen")) {
            return 6 // prefer sooner reminder (6–8h typical)
        }
        
        // Aspirin - usually every 4-6 hours
        if (lowerText.contains("aspirin") || lowerText.contains("aspirin")) {
            return 6
        }
        
        // Antibiotics - usually every 8-12 hours
        if (lowerText.contains("amoxicillin") || lowerText.contains("amoksicilin") ||
            lowerText.contains("penicillin") || lowerText.contains("penicilin") ||
            lowerText.contains("antibiotik") || lowerText.contains("antibiotic")) {
            return 8
        }
        
        // Cough syrup / Kašalj sirup - usually every 6-8 hours
        if (lowerText.contains("sirup") || lowerText.contains("syrup") ||
            lowerText.contains("kašalj") || lowerText.contains("cough")) {
            return 6
        }
        
        // Fever medicine / Lijek za temperaturu - usually every 6 hours
        if (lowerText.contains("temperatur") || lowerText.contains("fever") ||
            lowerText.contains("vrućica")) {
            return 6
        }
        
        // If medicine is mentioned but no specific interval found, default to 6 hours
        // (safe default for most common medicines)
        val medicineKeywords = listOf(
            "lijek", "medicine", "medication", "tableta", "tablet", "kapsula", "capsule",
            "sirup", "syrup", "drops", "kapi", "injekcija", "injection"
        )
        if (medicineKeywords.any { lowerText.contains(it) }) {
            return 6 // Safe default
        }
        
        return null
    }
    
    private fun extractFeeding(text: String): Pair<FeedingType?, Int?> {
        val lowerText = text.lowercase()
        
        // Don't detect feeding if it looks like a shopping list (avoid false positives)
        if (looksLikeShoppingList(text)) {
            return Pair(null, null)
        }
        
        // Detect feeding type - only explicit feeding actions/verbs
        val feedingType = when {
            (lowerText.contains("lijeva") || lowerText.contains("left")) && 
            (lowerText.contains("dojka") || lowerText.contains("breast")) -> FeedingType.BREAST_LEFT
            (lowerText.contains("desna") || lowerText.contains("right")) && 
            (lowerText.contains("dojka") || lowerText.contains("breast")) -> FeedingType.BREAST_RIGHT
            lowerText.contains("bočic") || lowerText.contains("bottle") -> FeedingType.BOTTLE
            (lowerText.contains("dojenje") || lowerText.contains("dojio") || lowerText.contains("breast")) && 
            !looksLikeShoppingList(text) -> {
                // Default to left if not specified, but only if not a shopping list
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
    
    private fun extractReminderDate(text: String): Long? {
        val lowerText = text.lowercase()
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        
        // Patterns for dates: "15.12.2024", "15/12/2024", "15-12-2024", "15.12", "decembar 15", etc.
        val datePatterns = listOf(
            // DD.MM.YYYY or DD/MM/YYYY or DD-MM-YYYY
            Regex("(\\d{1,2})[./-](\\d{1,2})[./-](\\d{4})"),
            // DD.MM (assume current year)
            Regex("(\\d{1,2})[./-](\\d{1,2})(?!\\d)")
        )
        
        for (pattern in datePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                try {
                    val groups = match.groupValues
                    var day: Int
                    var month: Int
                    var year: Int = calendar.get(java.util.Calendar.YEAR)
                    
                    if (groups.size >= 3) {
                        day = groups[1].toInt()
                        month = groups[2].toInt()
                        if (groups.size > 3 && groups[3].length == 4) {
                            year = groups[3].toInt()
                        }
                        
                        // Validate date
                        if (day in 1..31 && month in 1..12 && year >= 2020 && year <= 2100) {
                            calendar.set(year, month - 1, day, 8, 0, 0) // Set to 8 AM
                            calendar.set(java.util.Calendar.MILLISECOND, 0)
                            
                            // If date is in the past this year, assume next year
                            if (calendar.timeInMillis < now && year == calendar.get(java.util.Calendar.YEAR)) {
                                calendar.set(java.util.Calendar.YEAR, year + 1)
                            }
                            
                            return calendar.timeInMillis
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next pattern
                }
            }
        }
        
        // Relative dates: "sutra" (tomorrow), "za 3 dana" (in 3 days), etc.
        if (lowerText.contains("sutra") || lowerText.contains("tomorrow")) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 8)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
        
        val daysPattern = Regex("(?:za|in|after)\\s+(\\d+)\\s*(?:dan|days|dana)")
        val daysMatch = daysPattern.find(lowerText)
        if (daysMatch != null) {
            val days = daysMatch.groupValues[1].toIntOrNull()
            if (days != null && days > 0 && days <= 365) {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 8)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                return calendar.timeInMillis
            }
        }
        
        return null
    }
}

