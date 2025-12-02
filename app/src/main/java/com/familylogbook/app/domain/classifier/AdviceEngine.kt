package com.familylogbook.app.domain.classifier

import com.familylogbook.app.domain.model.AdviceTemplate
import com.familylogbook.app.domain.model.Category

/**
 * Rule-based engine that maps symptoms and keywords to parenting advice templates.
 * This is NOT medical advice - just general parenting tips.
 */
class AdviceEngine {
    
    private val adviceTemplates = createAdviceTemplates()
    
    /**
     * Finds relevant advice for a log entry based on its text and category.
     * Returns null if no relevant advice is found.
     */
    fun findAdvice(text: String, category: Category): AdviceTemplate? {
        val lowerText = text.lowercase()
        
        // Check each template's keywords
        for (template in adviceTemplates) {
            if (template.relatedKeywords.any { keyword -> lowerText.contains(keyword) }) {
                return template
            }
        }
        
        // Category-based fallback
        return when (category) {
            Category.HEALTH -> findHealthAdvice(lowerText)
            Category.FEEDING -> findFeedingAdvice(lowerText)
            Category.SLEEP -> findSleepAdvice(lowerText)
            Category.MOOD -> findMoodAdvice(lowerText)
            else -> null
        }
    }
    
    private fun findHealthAdvice(text: String): AdviceTemplate? {
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
    
    private fun findFeedingAdvice(text: String): AdviceTemplate? {
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
            
            // Feeding
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
                relatedKeywords = listOf("dojenje", "bočica", "hranjenje", "feeding", "mlijeko")
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
            )
        )
    }
}

