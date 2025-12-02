package com.familylogbook.app.data.export

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages export of logbook data to various formats.
 */
class ExportManager {
    
    /**
     * Exports all data to JSON format.
     */
    fun exportToJson(children: List<Child>, entries: List<LogEntry>): String {
        val json = JSONObject()
        json.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date()))
        json.put("version", "1.0")
        
        // Children array
        val childrenArray = JSONArray()
        children.forEach { child ->
            val childObj = JSONObject()
            childObj.put("id", child.id)
            childObj.put("name", child.name)
            child.dateOfBirth?.let { childObj.put("dateOfBirth", it) }
            childObj.put("avatarColor", child.avatarColor)
            childObj.put("emoji", child.emoji)
            childrenArray.put(childObj)
        }
        json.put("children", childrenArray)
        
        // Entries array
        val entriesArray = JSONArray()
        entries.forEach { entry ->
            val entryObj = JSONObject()
            entryObj.put("id", entry.id)
            entry.childId?.let { entryObj.put("childId", it) }
            entryObj.put("timestamp", entry.timestamp)
            entryObj.put("rawText", entry.rawText)
            entryObj.put("category", entry.category.name)
            entryObj.put("tags", JSONArray(entry.tags))
            entry.mood?.let { entryObj.put("mood", it.name) }
            entry.temperature?.let { entryObj.put("temperature", it) }
            entry.medicineGiven?.let { entryObj.put("medicineGiven", it) }
            entry.medicineTimestamp?.let { entryObj.put("medicineTimestamp", it) }
            entry.feedingType?.let { entryObj.put("feedingType", it.name) }
            entry.feedingAmount?.let { entryObj.put("feedingAmount", it) }
            entriesArray.put(entryObj)
        }
        json.put("entries", entriesArray)
        
        return json.toString(2) // Pretty print with 2 spaces indentation
    }
    
    /**
     * Exports entries to CSV format.
     */
    fun exportToCsv(children: List<Child>, entries: List<LogEntry>): String {
        val childrenMap = children.associateBy { it.id }
        
        val csv = StringBuilder()
        // Header
        csv.appendLine("Date,Time,Child,Category,Text,Tags,Mood,Temperature,Medicine,Feeding Type,Feeding Amount")
        
        // Data rows
        entries.forEach { entry ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(entry.timestamp))
            val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(entry.timestamp))
            val childName = entry.childId?.let { childrenMap[it]?.name } ?: "Family"
            
            csv.appendLine(
                listOf(
                    date,
                    time,
                    childName,
                    entry.category.name,
                    "\"${entry.rawText.replace("\"", "\"\"")}\"", // Escape quotes
                    entry.tags.joinToString(";"),
                    entry.mood?.name ?: "",
                    entry.temperature?.toString() ?: "",
                    entry.medicineGiven ?: "",
                    entry.feedingType?.name ?: "",
                    entry.feedingAmount?.toString() ?: ""
                ).joinToString(",")
            )
        }
        
        return csv.toString()
    }
    
    /**
     * Parses JSON export and returns children and entries.
     * @return Pair of (children, entries) or null if parsing fails
     */
    fun parseJsonImport(jsonString: String): Pair<List<Child>, List<LogEntry>>? {
        return try {
            val json = JSONObject(jsonString)
            
            // Parse children
            val childrenList = mutableListOf<Child>()
            if (json.has("children")) {
                val childrenArray = json.getJSONArray("children")
                for (i in 0 until childrenArray.length()) {
                    val childObj = childrenArray.getJSONObject(i)
                    val child = Child(
                        id = childObj.getString("id"),
                        name = childObj.getString("name"),
                        dateOfBirth = if (childObj.has("dateOfBirth") && !childObj.isNull("dateOfBirth")) {
                            childObj.getLong("dateOfBirth")
                        } else null,
                        avatarColor = childObj.getString("avatarColor"),
                        emoji = childObj.getString("emoji")
                    )
                    childrenList.add(child)
                }
            }
            
            // Parse entries
            val entriesList = mutableListOf<LogEntry>()
            if (json.has("entries")) {
                val entriesArray = json.getJSONArray("entries")
                for (i in 0 until entriesArray.length()) {
                    val entryObj = entriesArray.getJSONObject(i)
                    
                    // Parse category
                    val categoryStr = entryObj.getString("category")
                    val category = try {
                        Category.valueOf(categoryStr)
                    } catch (e: Exception) {
                        Category.OTHER
                    }
                    
                    // Parse mood
                    val mood = if (entryObj.has("mood") && !entryObj.isNull("mood")) {
                        try {
                            Mood.valueOf(entryObj.getString("mood"))
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                    
                    // Parse tags
                    val tagsList = mutableListOf<String>()
                    if (entryObj.has("tags")) {
                        val tagsArray = entryObj.getJSONArray("tags")
                        for (j in 0 until tagsArray.length()) {
                            tagsList.add(tagsArray.getString(j))
                        }
                    }
                    
                    // Parse feeding type
                    val feedingType = if (entryObj.has("feedingType") && !entryObj.isNull("feedingType")) {
                        try {
                            FeedingType.valueOf(entryObj.getString("feedingType"))
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                    
                    val entry = LogEntry(
                        id = entryObj.getString("id"),
                        childId = if (entryObj.has("childId") && !entryObj.isNull("childId")) {
                            entryObj.getString("childId")
                        } else null,
                        timestamp = entryObj.getLong("timestamp"),
                        rawText = entryObj.getString("rawText"),
                        category = category,
                        tags = tagsList,
                        mood = mood,
                        temperature = if (entryObj.has("temperature") && !entryObj.isNull("temperature")) {
                            entryObj.getDouble("temperature").toFloat()
                        } else null,
                        medicineGiven = if (entryObj.has("medicineGiven") && !entryObj.isNull("medicineGiven")) {
                            entryObj.getString("medicineGiven")
                        } else null,
                        medicineTimestamp = if (entryObj.has("medicineTimestamp") && !entryObj.isNull("medicineTimestamp")) {
                            entryObj.getLong("medicineTimestamp")
                        } else null,
                        feedingType = feedingType,
                        feedingAmount = if (entryObj.has("feedingAmount") && !entryObj.isNull("feedingAmount")) {
                            entryObj.getInt("feedingAmount")
                        } else null
                    )
                    entriesList.add(entry)
                }
            }
            
            Pair(childrenList, entriesList)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

