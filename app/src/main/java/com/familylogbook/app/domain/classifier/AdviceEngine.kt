package com.familylogbook.app.domain.classifier

import com.familylogbook.app.domain.model.AdviceTemplate
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.data.shopping.GoogleCustomSearchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Rule-based engine that maps symptoms and keywords to parenting advice templates.
 * This is NOT medical advice - just general parenting tips.
 * 
 * Also handles shopping deals via Google Custom Search API.
 */
class AdviceEngine {
    
    private val adviceTemplates = createAdviceTemplates()
    
    // Google Custom Search Service for shopping deals
    private val customSearchService: GoogleCustomSearchService? by lazy {
        try {
            val apiKey = com.familylogbook.app.BuildConfig.GOOGLE_CSE_API_KEY
            val engineId = com.familylogbook.app.BuildConfig.GOOGLE_CSE_ENGINE_ID
            if (apiKey.isNotEmpty() && engineId.isNotEmpty()) {
                GoogleCustomSearchService(apiKey, engineId)
            } else {
                android.util.Log.w("AdviceEngine", "Google Custom Search API key or Engine ID not configured")
                null
            }
        } catch (e: Exception) {
            android.util.Log.w("AdviceEngine", "Failed to initialize Google Custom Search Service: ${e.message}")
            null
        }
    }
    
    /**
     * Finds relevant advice for a log entry based on its text and category.
     * Returns null if no relevant advice is found.
     */
    fun findAdvice(text: String, category: Category): AdviceTemplate? {
        return findAdvice(text, category, null)
    }
    
    /**
     * Finds relevant advice for a log entry based on its text, category, and symptoms.
     * Returns null if no relevant advice is found.
     */
    fun findAdvice(text: String, category: Category, symptoms: List<String>?): AdviceTemplate? {
        val lowerText = text.lowercase()
        
        // FIRST check category-based advice (more reliable)
        val categoryAdvice = when (category) {
            Category.HEALTH -> findHealthAdvice(lowerText, symptoms)
            Category.FEEDING -> findFeedingAdvice(lowerText)
            Category.SLEEP -> findSleepAdvice(lowerText)
            Category.MOOD -> findMoodAdvice(lowerText)
            Category.AUTO -> findAutoAdvice(lowerText)
            Category.HOUSE -> findHouseAdvice(lowerText)
            Category.FINANCE -> findFinanceAdvice(lowerText)
            Category.WORK -> findWorkAdvice(lowerText)
            Category.SHOPPING -> findShoppingAdvice(lowerText)
            Category.SMART_HOME -> findSmartHomeAdvice(lowerText)
            else -> null
        }
        
        // Return category-based advice if found (prioritize category over keyword matching)
        if (categoryAdvice != null) {
            return categoryAdvice
        }
        
        // Only check keywords if no category advice found AND category allows it
        // Don't show feeding/health advice for shopping lists or other non-relevant categories
        val allowedAdviceIds = when (category) {
            Category.SHOPPING -> listOf("shopping_list") // Only shopping advice for shopping category
            Category.OTHER -> emptyList() // No advice for OTHER category to avoid false positives
            Category.WORK -> listOf("work_reminder") // Only work advice for WORK category
            else -> null // Allow all for other categories (but exclude work_reminder)
        }
        
        // Check keyword-based templates (only if category allows it)
        for (template in adviceTemplates) {
            // Skip if category restricts which advice types are allowed
            if (allowedAdviceIds != null && template.id !in allowedAdviceIds) {
                continue
            }
            // IMPORTANT: Never show work_reminder advice via keyword matching for non-WORK categories
            // Work advice should ONLY appear for Category.WORK entries
            if (template.id == "work_reminder" && category != Category.WORK) {
                continue
            }
            if (template.relatedKeywords.any { keyword -> lowerText.contains(keyword) }) {
                return template
            }
        }
        
        return null
    }
    
    private fun findHealthAdvice(text: String, symptoms: List<String>? = null): AdviceTemplate? {
        // Check symptoms first (if provided) - use symptom-based matching
        symptoms?.forEach { symptom ->
            val lowerSymptom = symptom.lowercase()
            when {
                lowerSymptom.contains("temperatur") || lowerSymptom.contains("temperatura") -> 
                    return adviceTemplates.find { it.id == "fever" }
                lowerSymptom.contains("grč") || lowerSymptom.contains("grčevi") || lowerSymptom.contains("bol u trbuhu") -> 
                    return adviceTemplates.find { it.id == "colic" }
                // For other symptoms, return general health advice (fever template works as general health)
                lowerSymptom.isNotEmpty() -> 
                    return adviceTemplates.find { it.id == "fever" }
            }
        }
        
        // Fallback to text-based detection
        when {
            text.contains("grč") || text.contains("colic") || text.contains("grčevi") -> 
                return adviceTemplates.find { it.id == "colic" }
            text.contains("temperatur") || text.contains("fever") || text.contains("vruć") -> 
                return adviceTemplates.find { it.id == "fever" }
            text.contains("plače") || text.contains("plač") || text.contains("crying") -> 
                return adviceTemplates.find { it.id == "crying" }
        }
        return null
    }
    
    private fun findFeedingAdvice(@Suppress("UNUSED_PARAMETER") text: String): AdviceTemplate? {
        return adviceTemplates.find { it.id == "feeding" }
    }
    
    private fun findSleepAdvice(text: String): AdviceTemplate? {
        when {
            text.contains("ne spava") || text.contains("can't sleep") || text.contains("trouble sleeping") -> 
                return adviceTemplates.find { it.id == "sleep_trouble" }
        }
        return null
    }
    
    private fun findMoodAdvice(text: String): AdviceTemplate? {
        when {
            text.contains("uzrujan") || text.contains("nervozan") || text.contains("stressed") -> 
                return adviceTemplates.find { it.id == "soothing" }
        }
        return null
    }
    
    private fun findAutoAdvice(text: String): AdviceTemplate? {
        when {
            text.contains("guma") || text.contains("tire") || text.contains("probušena") || text.contains("procurila") || text.contains("flat") -> 
                return adviceTemplates.find { it.id == "flat_tire" }
            text.contains("servis") || text.contains("service") -> 
                return adviceTemplates.find { it.id == "car_service" }
        }
        return adviceTemplates.find { it.id == "auto_general" }
    }
    
    private fun findHouseAdvice(text: String): AdviceTemplate? {
        when {
            text.contains("pokvario") || text.contains("broken") || text.contains("broke") -> 
                return adviceTemplates.find { it.id == "house_repair" }
        }
        return adviceTemplates.find { it.id == "house_general" }
    }
    
    private fun findFinanceAdvice(@Suppress("UNUSED_PARAMETER") text: String): AdviceTemplate? {
        return adviceTemplates.find { it.id == "finance_bill" }
    }
    
    private fun findWorkAdvice(@Suppress("UNUSED_PARAMETER") text: String): AdviceTemplate? {
        return adviceTemplates.find { it.id == "work_reminder" }
    }
    
    private fun findShoppingAdvice(@Suppress("UNUSED_PARAMETER") text: String): AdviceTemplate? {
        // Return null for shopping - we'll use async findShoppingDealsAdvice instead
        // This prevents showing generic shopping list advice
        return null
    }
    
    /**
     * Finds shopping deals advice for a shopping list entry.
     * This is an async operation that searches for deals using Google Custom Search API.
     * 
     * @param text Shopping list text (e.g., "jaja, kruh, mlijeko")
     * @param location Optional location for better results (e.g., "Umag")
     * @return AdviceTemplate with shopping deals if found, null otherwise
     */
    suspend fun findShoppingDealsAdvice(
        text: String,
        location: String = "Hrvatska"
    ): AdviceTemplate? = withContext(Dispatchers.IO) {
        val service = customSearchService ?: return@withContext null
        
        // Extract shopping items from text
        val items = extractShoppingItems(text)
        if (items.isEmpty()) {
            return@withContext null
        }
        
        // Search for deals for each item
        val allDeals = mutableListOf<GoogleCustomSearchService.ShoppingDeal>()
        
        for (item in items.take(5)) { // Limit to 5 items to avoid too many API calls
            try {
                val deals = service.searchDeals(item, location)
                allDeals.addAll(deals)
            } catch (e: Exception) {
                android.util.Log.e("AdviceEngine", "Error searching deals for $item: ${e.message}")
            }
        }
        
        // If no deals found, return null (don't show advice)
        if (allDeals.isEmpty()) {
            return@withContext null
        }
        
        // Group deals by product and get best deal per product (max 1 per product)
        val dealsByProduct = allDeals.groupBy { it.productName }
        
        // Create tips from deals - format: "artikal – trgovina – cijena"
        val tips = mutableListOf<String>()
        val foundProducts = mutableListOf<String>()
        
        dealsByProduct.forEach { (product, deals) ->
            // Filter and sort deals by quality (best deals first)
            // IMPORTANT: Only show deals that are ACTUALLY on sale (have discount or explicit "akcija" mention)
            val goodDeals = deals
                .filter { deal -> 
                    isGoodDeal(deal, product) && isActuallyOnSale(deal)
                }
                .sortedByDescending { deal -> calculateDealScore(deal, product) }
            
            // Take the best deal (highest score)
            val bestDeal = goodDeals.firstOrNull() ?: return@forEach
            
            foundProducts.add(product)
            
            // Format: "artikal – trgovina – cijena"
            val storeInfo = bestDeal.storeName
            val priceInfo = bestDeal.price?.let { " – $it" } ?: ""
            val discountInfo = bestDeal.discount?.let { " ($it)" } ?: ""
            
            tips.add("$product – $storeInfo$priceInfo$discountInfo")
        }
        
        // Only show advice if we found deals for at least one product
        if (tips.isEmpty() || foundProducts.isEmpty()) {
            return@withContext null
        }
        
        // Create formatted description
        val productsList = foundProducts.joinToString(", ")
        val description = if (foundProducts.size == 1) {
            "Našao sam akciju za: $productsList"
        } else {
            "Našao sam akcije za: $productsList"
        }
        
        // Create AdviceTemplate for shopping deals
        AdviceTemplate(
            id = "shopping_deals",
            title = "💰 Akcije za tvoju shopping listu",
            shortDescription = description,
            tips = tips.take(5), // Limit to max 5 deals
            whenToCallDoctor = null,
            relatedKeywords = emptyList()
        )
    }
    
    /**
     * Checks if a deal is actually on sale (has discount or explicit "akcija" mention).
     * Returns true if deal appears to be a real sale, false otherwise.
     */
    private fun isActuallyOnSale(deal: GoogleCustomSearchService.ShoppingDeal): Boolean {
        val snippet = (deal.snippet + " " + deal.title).lowercase()
        
        // Must have explicit discount indicator
        val hasDiscount = deal.discount != null || 
            snippet.contains("popust") ||
            snippet.contains("snižen") ||
            snippet.contains("sniženo") ||
            snippet.contains("akcija") ||
            snippet.contains("-") && Regex("""\d+%""").containsMatchIn(snippet) ||
            Regex("""-\d+%""").containsMatchIn(snippet)
        
        if (!hasDiscount) {
            android.util.Log.d("AdviceEngine", "Filtering non-sale deal: ${deal.productName} - no discount indicator")
            return false
        }
        
        return true
    }
    
    /**
     * Checks if a deal is good (not obviously overpriced).
     * Returns true if deal seems reasonable, false if it's clearly a bad deal.
     */
    private fun isGoodDeal(deal: GoogleCustomSearchService.ShoppingDeal, productName: String): Boolean {
        val price = deal.price ?: return true // If no price, assume it's OK (can't judge)
        
        // Extract numeric price value
        val priceValue = extractPriceValue(price) ?: return true // If can't parse, assume OK
        
        // Expected price ranges for common products (in EUR)
        val expectedPriceRanges = mapOf(
            "kruh" to (0.5f..3.0f),
            "mlijeko" to (0.5f..2.5f),
            "jaja" to (1.0f..4.0f),
            "sir" to (2.0f..8.0f),
            "meso" to (3.0f..15.0f),
            "piletina" to (4.0f..12.0f),
            "svinjetina" to (5.0f..15.0f),
            "voće" to (1.0f..5.0f),
            "povrće" to (0.5f..4.0f),
            "kro" to (0.5f..3.0f), // krokice
            "cigare" to (3.0f..8.0f),
            "salame" to (2.0f..10.0f),
            "hrana za pse" to (2.0f..15.0f),
            "hrana za pse" to (2.0f..15.0f)
        )
        
        val productLower = productName.lowercase()
        
        // Check if product matches any expected price range
        for ((keyword, range) in expectedPriceRanges) {
            if (productLower.contains(keyword)) {
                // If price is way above expected range, it's a bad deal
                if (priceValue > range.endInclusive * 1.5f) { // Allow 50% above max as buffer
                    android.util.Log.d("AdviceEngine", "Filtering bad deal: $productName at $price (expected max: ${range.endInclusive})")
                    return false
                }
                return true
            }
        }
        
        // For unknown products, use general heuristics
        // If price is extremely high (>20 EUR) for a single item, it's probably wrong
        if (priceValue > 20.0f && productLower.length < 15) { // Short product names shouldn't cost >20 EUR
            android.util.Log.d("AdviceEngine", "Filtering suspiciously expensive deal: $productName at $price")
            return false
        }
        
        return true
    }
    
    /**
     * Calculates a score for a deal (higher = better).
     * Considers: discount percentage, price reasonableness, store reputation.
     */
    private fun calculateDealScore(deal: GoogleCustomSearchService.ShoppingDeal, productName: String): Float {
        var score = 0f
        
        // Prefer deals with explicit discounts
        if (deal.discount != null) {
            val discountValue = extractDiscountPercentage(deal.discount)
            if (discountValue != null) {
                score += discountValue * 10f // Each % discount = 10 points
            } else {
                score += 20f // Has discount text but can't parse percentage
            }
        }
        
        // Prefer certain stores (reputation bonus)
        val storeBonus = when (deal.storeName.lowercase()) {
            "lidl" -> 5f
            "kaufland" -> 3f
            "konzum" -> 2f
            "spar" -> 1f
            else -> 0f
        }
        score += storeBonus
        
        // Prefer deals with price information (more reliable)
        if (deal.price != null) {
            score += 5f
        }
        
        return score
    }
    
    /**
     * Extracts numeric price value from price string (e.g., "3,98 EUR" -> 3.98f).
     */
    private fun extractPriceValue(price: String): Float? {
        return try {
            // Remove currency symbols and extract number
            val cleaned = price
                .replace("€", "")
                .replace("EUR", "")
                .replace("kn", "")
                .replace("HRK", "")
                .replace(" ", "")
                .trim()
            
            // Handle both comma and dot as decimal separator
            val normalized = cleaned.replace(",", ".")
            
            // Extract first number (in case there are multiple)
            val numberPattern = Regex("""(\d+\.?\d*)""")
            val match = numberPattern.find(normalized)
            
            match?.value?.toFloatOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extracts discount percentage from discount string (e.g., "-30%" -> 30).
     */
    private fun extractDiscountPercentage(discount: String): Int? {
        return try {
            val numberPattern = Regex("""(\d+)%""")
            val match = numberPattern.find(discount)
            match?.groupValues?.get(1)?.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extracts shopping items from text.
     * Handles comma-separated lists and space-separated items.
     */
    private fun extractShoppingItems(text: String): List<String> {
        // Words to ignore (verbs, prefixes, etc.)
        val ignoreWords = setOf(
            "treba", "kupiti", "kupi", "kupujem", "kupujemo", "kupujete", "kupuju",
            "need", "buy", "buying", "purchase", "get", "getting",
            "lista", "list", "stavke", "items", "namirnice", "groceries",
            "za", "malo", "brzo", "hitno", "sutra", "danas",
            "i", "ili", "itd", "etc", "itd.", "etc."
        )
        
        // Split by comma, newline, or "i" (and)
        val rawItems = text
            .replace(";", ",")
            .replace(" i ", ",") // "kruh i mlijeko" -> "kruh, mlijeko"
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        // Extract clean items
        val cleanItems = mutableListOf<String>()
        
        for (item in rawItems) {
            // Remove common prefixes
            var cleanItem = item
                .lowercase()
                .trim()
            
            // Remove ignore words from beginning
            val words = cleanItem.split(" ").filter { it.isNotEmpty() }
            val filteredWords = words.filter { word ->
                !ignoreWords.contains(word.lowercase())
            }
            
            if (filteredWords.isNotEmpty()) {
                // Reconstruct item from filtered words
                cleanItem = filteredWords.joinToString(" ").trim()
                
                // Only add if it's a meaningful item (at least 2 characters)
                if (cleanItem.length >= 2 && !cleanItem.all { it.isDigit() || it.isWhitespace() }) {
                    cleanItems.add(cleanItem)
                }
            }
        }
        
        // Limit to max 5 items to avoid too many API calls
        return cleanItems.take(5).distinct()
    }
    
    private fun findSmartHomeAdvice(@Suppress("UNUSED_PARAMETER") text: String): AdviceTemplate? {
        return adviceTemplates.find { it.id == "smart_home" }
    }
    
    /**
     * Gets advice template by ID.
     * Returns null if not found.
     */
    fun getAdviceById(adviceId: String): AdviceTemplate? {
        return adviceTemplates.find { it.id == adviceId }
    }
    
    private fun createAdviceTemplates(): List<AdviceTemplate> {
        return listOf(
            // Colic / Grčevi
            AdviceTemplate(
                id = "colic",
                title = "Grčevi - Što možeš probati",
                shortDescription = "Grčevi su česti kod beba. Evo nekoliko načina kako možete pomoći.",
                tips = listOf(
                    "Nosite dijete u položaju 'mačkica' (na rukama, trbuhom prema dolje)",
                    "Lagana masaža trbuščića u smjeru kazaljke na satu",
                    "Lagano njihanje ili šetnja s djetetom",
                    "Pratite prehranu - možda dijete reagira na određenu hranu",
                    "Topli oblog na trbuh (pažljivo, ne previše vruće)",
                    "Bijeli šum ili umirujuća glazba"
                ),
                whenToCallDoctor = "Ako grčevi traju dugo, dijete ne može zaspati ili ima temperaturu, kontaktirajte pedijatra.",
                relatedKeywords = listOf("grč", "grčevi", "colic", "trbuh", "boli")
            ),
            
            // Fever / Temperatura
            AdviceTemplate(
                id = "fever",
                title = "Temperatura - Opći savjeti",
                shortDescription = "Kada dijete ima temperaturu, važno je pratiti njegovo stanje.",
                tips = listOf(
                    "Lagano razodjenite dijete (ne pretoplite)",
                    "Često nudite tekućinu (voda, mlijeko)",
                    "Pratite ponašanje - je li dijete aktivno ili letargično?",
                    "Provjerite da li dijete dobro diše",
                    "Kratki, topli tuš može pomoći (ne hladan!)",
                    "Pratite temperaturu svakih par sati"
                ),
                whenToCallDoctor = "Ovo NIJE zamjena za liječnika. Ako si zabrinut/a, dijete je letargično, ima problema s disanjem ili temperatura traje dugo, kontaktirajte pedijatra odmah.",
                relatedKeywords = listOf("temperatur", "fever", "vruć", "vrućica", "38", "39", "40")
            ),
            
            // Crying / Plač
            AdviceTemplate(
                id = "crying",
                title = "Dijete plače - Što provjeriti",
                shortDescription = "Plač je način komunikacije. Evo što možete provjeriti.",
                tips = listOf(
                    "Provjerite pelenu - je li mokra ili prljava?",
                    "Je li dijete gladno? Probajte hranjenje",
                    "Provjerite da li rastu zubići (često uzrok plača)",
                    "Bijeli šum ili umirujuća glazba",
                    "Nošenje i lagano njihanje",
                    "Skin-to-skin kontakt (posebno za bebe)",
                    "Tihi mrak i mirna soba",
                    "Provjerite temperaturu - nije li dijete previše toplo/hladno"
                ),
                whenToCallDoctor = "Ako dijete plače neprekidno satima, ima temperaturu, ili se ponaša neuobičajeno, kontaktirajte liječnika.",
                relatedKeywords = listOf("plače", "plač", "crying", "ne može", "ne znam")
            ),
            
            // Feeding - Only for FEEDING category, removed "mlijeko" from keywords to avoid false positives
            AdviceTemplate(
                id = "feeding",
                title = "Hranjenje - Praćenje",
                shortDescription = "Pratite hranjenje da biste vidjeli obrasce i osigurali dovoljno hrane.",
                tips = listOf(
                    "Tipičan interval za bebe: 2-4 sata (ovisi o dobi)",
                    "Pratite koliko dijete jede dnevno",
                    "Za dojenje: alternirajte lijeva/desna dojka",
                    "Za bočicu: pratite količinu (ml)",
                    "Ako dijete ne želi jesti, provjerite da li je umorno ili možda rastu zubići"
                ),
                whenToCallDoctor = "Ako dijete ne želi jesti dugo, gubi na težini, ili ima znakove dehidracije, kontaktirajte pedijatra.",
                relatedKeywords = listOf("dojenje", "bočica", "hranjenje", "feeding", "breast", "bottle", "najeo", "jela")
                // Removed "mlijeko" from keywords to avoid false positives with shopping lists
            ),
            
            // Sleep trouble
            AdviceTemplate(
                id = "sleep_trouble",
                title = "Problemi sa spavanjem - Savjeti",
                shortDescription = "Pomažemo dijete da lakše zaspi.",
                tips = listOf(
                    "Uspostavite rutinu pred spavanje (kupanje, priča, pjesma)",
                    "Prigušeno svjetlo i mirna soba",
                    "Bijeli šum ili umirujuća glazba",
                    "Provjerite da li je dijete gladno, umorno, ili ima mokru pelenu",
                    "Lagano njihanje ili nošenje",
                    "Ritual pred spavanje - isti redoslijed svaki dan"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("ne spava", "can't sleep", "trouble sleeping", "noćne more")
            ),
            
            // Soothing
            AdviceTemplate(
                id = "soothing",
                title = "Kako umiriti dijete",
                shortDescription = "Neki opći trikovi za umirivanje djeteta.",
                tips = listOf(
                    "Bijeli šum ili umirujuća glazba",
                    "Nošenje i lagano njihanje",
                    "Tihi mrak i mirna soba",
                    "Skin-to-skin kontakt",
                    "Ritual pred spavanje (prigušeno svjetlo + priča)",
                    "Provjerite osnovne potrebe: glad, pelena, temperatura"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("uzrujan", "nervozan", "stressed", "ne može", "umiri")
            ),
            
            // Auto - Flat Tire
            AdviceTemplate(
                id = "flat_tire",
                title = "Probušena guma - Što napraviti",
                shortDescription = "Ako imate probušenu gumu, evo koraka koje trebate poduzeti.",
                tips = listOf(
                    "Provjerite ima li rezervnu gumu u prtljažniku",
                    "Ako nema rezervne, nazovite osiguranje ili vučnu službu",
                    "Ne vozite dalje ako je guma potpuno prazna - može oštetiti felgu",
                    "Ako imate rezervnu, zamijenite je (ako znate kako)",
                    "Provjerite tlak u ostalim gumama nakon zamjene",
                    "Odmah idite na servis da popravite probušenu gumu"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("guma", "tire", "probušena", "procurila", "flat", "punctured")
            ),
            
            // Auto - Service
            AdviceTemplate(
                id = "car_service",
                title = "Servis auta - Podsjetnik",
                shortDescription = "Redovno održavanje auta je važno za sigurnost i pouzdanost.",
                tips = listOf(
                    "Zabilježite datum i kilometražu servisa",
                    "Provjerite što je uključeno u servis (ulje, filteri, kočnice, itd.)",
                    "Spremite račun za buduće reference",
                    "Postavite podsjetnik za sljedeći servis (obično nakon 10-15k km)",
                    "Provjerite garanciju ako je auto novi"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("servis", "service", "održavanje", "maintenance")
            ),
            
            // Auto - General
            AdviceTemplate(
                id = "auto_general",
                title = "Auto - Opći savjeti",
                shortDescription = "Koristan savjet za održavanje vašeg vozila.",
                tips = listOf(
                    "Redovno provjeravajte tlak u gumama",
                    "Pratite kilometražu za redovne servise",
                    "Spremite važne dokumente (osiguranje, registracija)",
                    "Provjerite datum registracije i osiguranja",
                    "Imajte rezervnu gumu i alat u prtljažniku"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("auto", "car", "vozilo", "vehicle")
            ),
            
            // House - Repair
            AdviceTemplate(
                id = "house_repair",
                title = "Kućni popravak - Što napraviti",
                shortDescription = "Ako se nešto pokvarilo u kući, evo koraka koje možete poduzeti.",
                tips = listOf(
                    "Provjerite je li problem hitan (curenje vode, struja, plin) - ako jeste, nazovite hitnu",
                    "Ako nije hitno, zabilježite što je pokvareno i kada",
                    "Provjerite garanciju ako je uređaj novi",
                    "Kontaktirajte majstora ili servis ako ne znate popraviti sami",
                    "Spremite račune za popravke za buduće reference",
                    "Ako je problem jednostavan, možete probati sami (pažljivo!)"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("pokvario", "broken", "broke", "popravak", "repair", "kvar")
            ),
            
            // House - General
            AdviceTemplate(
                id = "house_general",
                title = "Kuća - Opći savjeti",
                shortDescription = "Koristan savjet za održavanje doma.",
                tips = listOf(
                    "Redovno mijenjajte filtere (zrak, voda)",
                    "Provjerite baterije u detektorima dima",
                    "Pratite redovne servise (grijanje, hladenje)",
                    "Zabilježite važne popravke i troškove",
                    "Imajte kontakt brojeve za hitne slučajeve (vodoinstalater, električar)"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("kuća", "house", "home", "stan", "apartment")
            ),
            
            // Finance - Bill
            AdviceTemplate(
                id = "finance_bill",
                title = "Račun - Podsjetnik",
                shortDescription = "Kako pravilno pratiti i plaćati račune.",
                tips = listOf(
                    "Zabilježite datum dospijeća računa",
                    "Postavite podsjetnik 2-3 dana prije dospijeća",
                    "Provjerite iznos i usporedite s prethodnim računima",
                    "Ako je račun neuobičajeno visok, provjerite zašto",
                    "Spremite račune za porezne svrhe",
                    "Koristite automatsko plaćanje ako je moguće"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("račun", "bill", "invoice", "plaćanje", "payment", "struja", "voda", "internet")
            ),
            
            // Work - Reminder
            AdviceTemplate(
                id = "work_reminder",
                title = "Posao - Podsjetnik",
                shortDescription = "Kako pravilno pratiti obaveze na poslu.",
                tips = listOf(
                    "Zabilježite važne rokove i sastanke",
                    "Postavite podsjetnike za važne zadatke",
                    "Spremite važne dokumente i prezentacije",
                    "Pratite sastanke i dogovore",
                    "Organizirajte se po prioritetima"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("posao", "work", "job", "sastanak", "meeting", "deadline", "rok")
            ),
            
            // Shopping - List
            AdviceTemplate(
                id = "shopping_list",
                title = "Kupovina - Lista",
                shortDescription = "Kako pravilno organizirati kupovinu.",
                tips = listOf(
                    "Napravite listu prije odlaska u trgovinu",
                    "Grupirajte artikle po kategorijama (namirnice, kuća, osobno)",
                    "Provjerite što već imate kod kuće",
                    "Pratite troškove da ne prekoračite budžet",
                    "Koristite aplikacije za popuste i kupone ako su dostupni"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf("kupovina", "shopping", "kupio", "buy", "lista", "list", "namirnice", "grocery")
            ),
            
            // Smart Home
            AdviceTemplate(
                id = "smart_home",
                title = "Pametna kuća - Komande",
                shortDescription = "Možete koristiti Google Home ili Alexa za upravljanje uređajima.",
                tips = listOf(
                    "Koristite Google Assistant ili Alexa za upravljanje svjetlima, klimom, TV-om i drugim uređajima",
                    "Imenujte sobe i uređaje jasno u aplikaciji za pametnu kuću",
                    "Možete postaviti scene i rutine za automatsko upravljanje",
                    "Provjerite da su vaši uređaji povezani s Google Home ili Alexa",
                    "Koristite glasovne komande za brže upravljanje"
                ),
                whenToCallDoctor = null,
                relatedKeywords = listOf(
                    "svjetlo", "light", "rumbu", "vacuum", "klima", "AC", "TV", "rolete", "blinds",
                    "muziku", "music", "google home", "alexa", "assistant", "pametna kuća", "smart home"
                )
            )
        )
    }
}

