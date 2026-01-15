package com.familylogbook.app.domain.classifier

import com.familylogbook.app.domain.model.AdviceTemplate
import com.familylogbook.app.domain.model.Category

/**
 * Rule-based engine that maps symptoms and keywords to parenting advice templates.
 * This is NOT medical advice - just general parenting tips.
 * 
 * Parent OS focus: Health, Medicine, Symptoms, Vaccination advice.
 */
class AdviceEngine {
    
    private val adviceTemplates = createAdviceTemplates()
    
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
            // Legacy categories removed for Parent OS
            Category.AUTO -> null
            Category.HOUSE -> null
            Category.FINANCE -> null
            Category.WORK -> null
            Category.SHOPPING -> null
            Category.SMART_HOME -> null
            // New Parent OS categories
            Category.MEDICINE -> findMedicineAdvice(lowerText)
            Category.SYMPTOM -> findSymptomAdvice(lowerText, symptoms)
            Category.VACCINATION -> findVaccinationAdvice(lowerText)
            Category.DAY -> findDayAdvice(lowerText)
            else -> null
        }
        
        // Return category-based advice if found (prioritize category over keyword matching)
        if (categoryAdvice != null) {
            return categoryAdvice
        }
        
        // Only check keywords if no category advice found AND category allows it
        val allowedAdviceIds: List<String>? = when (category) {
            Category.OTHER -> emptyList() // No advice for OTHER category to avoid false positives
            else -> null // Allow all for other categories
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
    
    // Shopping deals methods removed - no longer needed for Parent OS
    // findShoppingDealsAdvice and all helper methods removed
    
    // New Parent OS advice methods (stubs for now - will be implemented in Faza 2)
    private fun findMedicineAdvice(text: String): AdviceTemplate? {
        // TODO: Implement medicine advice (Faza 2)
        return null
    }
    
    private fun findSymptomAdvice(text: String, symptoms: List<String>?): AdviceTemplate? {
        // Use existing health advice for symptoms
        return findHealthAdvice(text, symptoms)
    }
    
    private fun findVaccinationAdvice(@Suppress("UNUSED_PARAMETER") text: String): AdviceTemplate? {
        // TODO: Implement vaccination advice (Faza 5)
        return null
    }
    
    private fun findDayAdvice(@Suppress("UNUSED_PARAMETER") text: String): AdviceTemplate? {
        // TODO: Implement day/routine advice (Faza 6.5)
        return null
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

