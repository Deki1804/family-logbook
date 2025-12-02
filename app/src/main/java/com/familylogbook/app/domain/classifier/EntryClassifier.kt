package com.familylogbook.app.domain.classifier

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.ClassifiedEntryMetadata
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
        
        return ClassifiedEntryMetadata(
            category = category,
            tags = tags,
            mood = mood
        )
    }
    
    private fun detectCategory(text: String): Category {
        // Health keywords
        val healthKeywords = listOf(
            "fever", "temperature", "sick", "ill", "medicine", "medication", "syrup",
            "doctor", "hospital", "cough", "cold", "flu", "virus", "infection",
            "vaccine", "vaccination", "pain", "ache", "hurt", "injury", "bandage",
            "tooth", "teeth", "dentist", "zub", "zubić"
        )
        
        // Sleep keywords
        val sleepKeywords = listOf(
            "sleep", "slept", "asleep", "wake", "woke", "awake", "bedtime",
            "nap", "napping", "tired", "exhausted", "rest", "spavao", "spava",
            "budan", "budio", "ne spava"
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
        
        // Home keywords
        val homeKeywords = listOf(
            "filter", "broke", "broken", "fixed", "repair", "maintenance",
            "cleaned", "cleaning", "house", "home", "filter", "appliance",
            "kuća", "pokvario", "popravio", "filter"
        )
        
        when {
            healthKeywords.any { text.contains(it) } -> return Category.HEALTH
            sleepKeywords.any { text.contains(it) } -> return Category.SLEEP
            moodKeywords.any { text.contains(it) } -> return Category.MOOD
            developmentKeywords.any { text.contains(it) } -> return Category.DEVELOPMENT
            schoolKeywords.any { text.contains(it) } -> return Category.KINDERGARTEN_SCHOOL
            homeKeywords.any { text.contains(it) } -> return Category.HOME
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
            "fever" to "fever",
            "temperature" to "fever",
            "tooth" to "tooth",
            "teeth" to "tooth",
            "zub" to "tooth",
            "medicine" to "medicine",
            "medication" to "medicine",
            "syrup" to "medicine",
            "doctor" to "doctor",
            "sleep" to "sleep",
            "wake" to "wake",
            "spava" to "sleep",
            "happy" to "happy",
            "sad" to "sad",
            "mood" to "mood",
            "first" to "milestone",
            "milestone" to "milestone",
            "learned" to "learning",
            "word" to "language",
            "riječ" to "language",
            "kindergarten" to "kindergarten",
            "school" to "school",
            "vrtić" to "kindergarten",
            "filter" to "maintenance",
            "broke" to "repair",
            "broken" to "repair"
        )
        
        tagMap.forEach { (keyword, tag) ->
            if (text.contains(keyword) && !tags.contains(tag)) {
                tags.add(tag)
            }
        }
        
        // Limit to 3 tags max
        return tags.take(3)
    }
}

