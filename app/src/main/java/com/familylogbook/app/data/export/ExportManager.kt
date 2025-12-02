package com.familylogbook.app.data.export

import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
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
}

