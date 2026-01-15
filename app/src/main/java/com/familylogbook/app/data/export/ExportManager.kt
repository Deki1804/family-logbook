package com.familylogbook.app.data.export

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.EntityType
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.PersonType
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages export of logbook data to various formats.
 */
class ExportManager {
    
    /**
     * Exports all data to JSON format (v2.0 with Persons and Entities support).
     */
    fun exportToJson(
        children: List<Child> = emptyList(),
        persons: List<Person> = emptyList(),
        entities: List<Entity> = emptyList(),
        entries: List<LogEntry>
    ): String {
        val json = JSONObject()
        json.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date()))
        json.put("version", "2.0") // Updated version for Persons/Entities support
        
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
        
        // Persons array (v2.0)
        val personsArray = JSONArray()
        persons.forEach { person ->
            val personObj = JSONObject()
            personObj.put("id", person.id)
            personObj.put("name", person.name)
            personObj.put("type", person.type.name)
            person.dateOfBirth?.let { personObj.put("dateOfBirth", it) }
            personObj.put("avatarColor", person.avatarColor)
            personObj.put("emoji", person.emoji)
            person.relationship?.let { personObj.put("relationship", it) }
            personsArray.put(personObj)
        }
        json.put("persons", personsArray)
        
        // Entities array (v2.0)
        val entitiesArray = JSONArray()
        entities.forEach { entity ->
            val entityObj = JSONObject()
            entityObj.put("id", entity.id)
            entityObj.put("name", entity.name)
            entityObj.put("type", entity.type.name)
            entityObj.put("avatarColor", entity.avatarColor)
            entityObj.put("emoji", entity.emoji)
            if (entity.metadata.isNotEmpty()) {
                val metadataObj = JSONObject()
                entity.metadata.forEach { (k, v) -> metadataObj.put(k, v) }
                entityObj.put("metadata", metadataObj)
            }
            entitiesArray.put(entityObj)
        }
        json.put("entities", entitiesArray)
        
        // Entries array
        val entriesArray = JSONArray()
        entries.forEach { entry ->
            val entryObj = JSONObject()
            entryObj.put("id", entry.id)
            entry.childId?.let { entryObj.put("childId", it) } // Legacy support
            entry.personId?.let { entryObj.put("personId", it) } // v2.0
            entry.entityId?.let { entryObj.put("entityId", it) } // v2.0
            entryObj.put("timestamp", entry.timestamp)
            entryObj.put("rawText", entry.rawText)
            entryObj.put("category", entry.category.name)
            entryObj.put("tags", JSONArray(entry.tags))
            entry.mood?.let { entryObj.put("mood", it.name) }
            entry.temperature?.let { entryObj.put("temperature", it) }
            entry.medicineGiven?.let { entryObj.put("medicineGiven", it) }
            entry.medicineDosage?.let { entryObj.put("medicineDosage", it) }
            entry.medicineTimestamp?.let { entryObj.put("medicineTimestamp", it) }
            entry.medicineIntervalHours?.let { entryObj.put("medicineIntervalHours", it) }
            entry.nextMedicineTime?.let { entryObj.put("nextMedicineTime", it) }
            entry.feedingType?.let { entryObj.put("feedingType", it.name) }
            entry.feedingAmount?.let { entryObj.put("feedingAmount", it) }
            entry.aiAdvice?.let { entryObj.put("aiAdvice", it) }
            entry.reminderDate?.let { entryObj.put("reminderDate", it) }
            entry.dayEntryType?.let { entryObj.put("dayEntryType", it.name) }
            entry.isCompleted?.let { entryObj.put("isCompleted", it) }
            entry.dueDate?.let { entryObj.put("dueDate", it) }
            entry.symptoms?.takeIf { it.isNotEmpty() }?.let { symptomsList ->
                val symptomsArray = JSONArray()
                symptomsList.forEach { symptom ->
                    symptomsArray.put(symptom)
                }
                entryObj.put("symptoms", symptomsArray)
            }
            entry.shoppingItems?.takeIf { it.isNotEmpty() }?.let { itemsList ->
                val itemsArray = JSONArray()
                itemsList.forEach { item ->
                    itemsArray.put(item)
                }
                entryObj.put("shoppingItems", itemsArray)
            }
            entry.checkedShoppingItems?.takeIf { it.isNotEmpty() }?.let { checkedSet ->
                val checkedArray = JSONArray()
                checkedSet.forEach { item ->
                    checkedArray.put(item)
                }
                entryObj.put("checkedShoppingItems", checkedArray)
            }
            entry.amount?.let { entryObj.put("amount", it) }
            entry.currency?.let { entryObj.put("currency", it) }
            entry.mileage?.let { entryObj.put("mileage", it) }
            entry.serviceType?.let { entryObj.put("serviceType", it) }
            entry.vaccinationName?.let { entryObj.put("vaccinationName", it) }
            entry.vaccinationDate?.let { entryObj.put("vaccinationDate", it) }
            entry.nextVaccinationDate?.let { entryObj.put("nextVaccinationDate", it) }
            entry.nextVaccinationMessage?.let { entryObj.put("nextVaccinationMessage", it) }
            entriesArray.put(entryObj)
        }
        json.put("entries", entriesArray)
        
        return json.toString(2) // Pretty print with 2 spaces indentation
    }
    
    /**
     * Exports entries to CSV format (v2.0 with Persons and Entities support).
     */
    fun exportToCsv(
        children: List<Child> = emptyList(),
        persons: List<Person> = emptyList(),
        entities: List<Entity> = emptyList(),
        entries: List<LogEntry>
    ): String {
        val childrenMap = children.associateBy { it.id }
        val personsMap = persons.associateBy { it.id }
        val entitiesMap = entities.associateBy { it.id }
        
        val csv = StringBuilder()
        // Header (v2.0 - expanded)
        csv.appendLine(
            "Date,Time,Person/Child,Entity,Category,Text,Tags,Mood,Temperature,Symptoms," +
                "Medicine,Medicine Dosage,Medicine Interval Hours,Next Medicine Time," +
                "Feeding Type,Feeding Amount," +
                "Reminder Date,Day Entry Type,Is Completed,Due Date," +
                "Vaccination Name,Vaccination Date,Next Vaccination Date," +
                "Amount,Currency,Mileage,Service Type"
        )
        
        // Data rows
        entries.forEach { entry ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(entry.timestamp))
            val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(entry.timestamp))
            
            // Person/Child name (v2.0 - supports both)
            val personOrChildName = entry.personId?.let { personsMap[it]?.name }
                ?: entry.childId?.let { childrenMap[it]?.name }
                ?: "Family"
            
            // Entity name (v2.0)
            val entityName = entry.entityId?.let { entitiesMap[it]?.name } ?: ""
            
            csv.appendLine(
                listOf(
                    date,
                    time,
                    personOrChildName,
                    entityName,
                    entry.category.name,
                    "\"${entry.rawText.replace("\"", "\"\"")}\"", // Escape quotes
                    entry.tags.joinToString(";"),
                    entry.mood?.name ?: "",
                    entry.temperature?.toString() ?: "",
                    entry.symptoms?.joinToString(";") ?: "",
                    entry.medicineGiven ?: "",
                    entry.medicineDosage ?: "",
                    entry.medicineIntervalHours?.toString() ?: "",
                    entry.nextMedicineTime?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it)) } ?: "",
                    entry.feedingType?.name ?: "",
                    entry.feedingAmount?.toString() ?: "",
                    entry.reminderDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "",
                    entry.dayEntryType?.name ?: "",
                    entry.isCompleted?.toString() ?: "",
                    entry.dueDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "",
                    entry.vaccinationName ?: "",
                    entry.vaccinationDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "",
                    entry.nextVaccinationDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "",
                    entry.amount?.toString() ?: "",
                    entry.currency ?: "",
                    entry.mileage?.toString() ?: "",
                    entry.serviceType ?: ""
                ).joinToString(",")
            )
        }
        
        return csv.toString()
    }
    
    /**
     * Parses JSON export and returns children, persons, entities, and entries.
     * Supports both v1.0 (legacy) and v2.0 (with Persons/Entities) formats.
     * @return Quadruple of (children, persons, entities, entries) or null if parsing fails
     */
    fun parseJsonImport(jsonString: String): ExportData? {
        return try {
            val json = JSONObject(jsonString)
            val version = if (json.has("version")) json.getString("version") else "1.0"
            
            // Parse children (legacy support)
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
            
            // Parse persons (v2.0)
            val personsList = mutableListOf<Person>()
            if (json.has("persons")) {
                val personsArray = json.getJSONArray("persons")
                for (i in 0 until personsArray.length()) {
                    val personObj = personsArray.getJSONObject(i)
                    val person = Person(
                        id = personObj.getString("id"),
                        name = personObj.getString("name"),
                        type = try {
                            PersonType.valueOf(personObj.getString("type"))
                        } catch (e: Exception) {
                            PersonType.CHILD // Default fallback
                        },
                        dateOfBirth = if (personObj.has("dateOfBirth") && !personObj.isNull("dateOfBirth")) {
                            personObj.getLong("dateOfBirth")
                        } else null,
                        avatarColor = personObj.getString("avatarColor"),
                        emoji = personObj.getString("emoji"),
                        relationship = if (personObj.has("relationship") && !personObj.isNull("relationship")) {
                            personObj.getString("relationship")
                        } else null
                    )
                    personsList.add(person)
                }
            }
            
            // Parse entities (v2.0)
            val entitiesList = mutableListOf<Entity>()
            if (json.has("entities")) {
                val entitiesArray = json.getJSONArray("entities")
                for (i in 0 until entitiesArray.length()) {
                    val entityObj = entitiesArray.getJSONObject(i)
                    val metadata: Map<String, String> = if (entityObj.has("metadata") && !entityObj.isNull("metadata")) {
                        val metadataObj = entityObj.getJSONObject("metadata")
                        metadataObj.keys().asSequence().mapNotNull { key ->
                            try {
                                key to metadataObj.getString(key)
                            } catch (e: Exception) {
                                null
                            }
                        }.toMap()
                    } else {
                        emptyMap()
                    }
                    val entity = Entity(
                        id = entityObj.getString("id"),
                        name = entityObj.getString("name"),
                        type = try {
                            EntityType.valueOf(entityObj.getString("type"))
                        } catch (e: Exception) {
                            EntityType.OTHER // Default fallback
                        },
                        avatarColor = entityObj.getString("avatarColor"),
                        emoji = entityObj.getString("emoji"),
                        metadata = metadata
                    )
                    entitiesList.add(entity)
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
                        personId = if (entryObj.has("personId") && !entryObj.isNull("personId")) {
                            entryObj.getString("personId")
                        } else null,
                        entityId = if (entryObj.has("entityId") && !entryObj.isNull("entityId")) {
                            entryObj.getString("entityId")
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
                        medicineDosage = if (entryObj.has("medicineDosage") && !entryObj.isNull("medicineDosage")) {
                            entryObj.getString("medicineDosage")
                        } else null,
                        medicineTimestamp = if (entryObj.has("medicineTimestamp") && !entryObj.isNull("medicineTimestamp")) {
                            entryObj.getLong("medicineTimestamp")
                        } else null,
                        medicineIntervalHours = if (entryObj.has("medicineIntervalHours") && !entryObj.isNull("medicineIntervalHours")) {
                            entryObj.getInt("medicineIntervalHours")
                        } else null,
                        nextMedicineTime = if (entryObj.has("nextMedicineTime") && !entryObj.isNull("nextMedicineTime")) {
                            entryObj.getLong("nextMedicineTime")
                        } else null,
                        feedingType = feedingType,
                        feedingAmount = if (entryObj.has("feedingAmount") && !entryObj.isNull("feedingAmount")) {
                            entryObj.getInt("feedingAmount")
                        } else null,
                        amount = if (entryObj.has("amount") && !entryObj.isNull("amount")) {
                            entryObj.getDouble("amount")
                        } else null,
                        currency = if (entryObj.has("currency") && !entryObj.isNull("currency")) {
                            entryObj.getString("currency")
                        } else null,
                        mileage = if (entryObj.has("mileage") && !entryObj.isNull("mileage")) {
                            entryObj.getInt("mileage")
                        } else null,
                        serviceType = if (entryObj.has("serviceType") && !entryObj.isNull("serviceType")) {
                            entryObj.getString("serviceType")
                        } else null,
                        reminderDate = if (entryObj.has("reminderDate") && !entryObj.isNull("reminderDate")) {
                            entryObj.getLong("reminderDate")
                        } else null,
                        dayEntryType = if (entryObj.has("dayEntryType") && !entryObj.isNull("dayEntryType")) {
                            try {
                                com.familylogbook.app.domain.model.DayEntryType.valueOf(entryObj.getString("dayEntryType"))
                            } catch (e: Exception) {
                                null
                            }
                        } else null,
                        isCompleted = if (entryObj.has("isCompleted") && !entryObj.isNull("isCompleted")) {
                            try {
                                entryObj.getBoolean("isCompleted")
                            } catch (e: Exception) {
                                null
                            }
                        } else null,
                        dueDate = if (entryObj.has("dueDate") && !entryObj.isNull("dueDate")) {
                            entryObj.getLong("dueDate")
                        } else null,
                        aiAdvice = if (entryObj.has("aiAdvice") && !entryObj.isNull("aiAdvice")) {
                            entryObj.getString("aiAdvice")
                        } else null,
                        symptoms = if (entryObj.has("symptoms") && !entryObj.isNull("symptoms")) {
                            val symptomsArray = entryObj.getJSONArray("symptoms")
                            (0 until symptomsArray.length()).mapNotNull { 
                                try {
                                    symptomsArray.getString(it)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        } else null,
                        shoppingItems = if (entryObj.has("shoppingItems") && !entryObj.isNull("shoppingItems")) {
                            val itemsArray = entryObj.getJSONArray("shoppingItems")
                            (0 until itemsArray.length()).mapNotNull {
                                try {
                                    itemsArray.getString(it)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        } else null,
                        checkedShoppingItems = if (entryObj.has("checkedShoppingItems") && !entryObj.isNull("checkedShoppingItems")) {
                            val checkedArray = entryObj.getJSONArray("checkedShoppingItems")
                            (0 until checkedArray.length()).mapNotNull {
                                try {
                                    checkedArray.getString(it)
                                } catch (e: Exception) {
                                    null
                                }
                            }.toSet()
                        } else null,
                        vaccinationName = if (entryObj.has("vaccinationName") && !entryObj.isNull("vaccinationName")) {
                            entryObj.getString("vaccinationName")
                        } else null,
                        vaccinationDate = if (entryObj.has("vaccinationDate") && !entryObj.isNull("vaccinationDate")) {
                            entryObj.getLong("vaccinationDate")
                        } else null,
                        nextVaccinationDate = if (entryObj.has("nextVaccinationDate") && !entryObj.isNull("nextVaccinationDate")) {
                            entryObj.getLong("nextVaccinationDate")
                        } else null,
                        nextVaccinationMessage = if (entryObj.has("nextVaccinationMessage") && !entryObj.isNull("nextVaccinationMessage")) {
                            entryObj.getString("nextVaccinationMessage")
                        } else null
                    )
                    entriesList.add(entry)
                }
            }
            
            ExportData(
                children = childrenList,
                persons = personsList,
                entities = entitiesList,
                entries = entriesList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Data class for export/import data structure.
     */
    data class ExportData(
        val children: List<Child>,
        val persons: List<Person>,
        val entities: List<Entity>,
        val entries: List<LogEntry>
    )
}

