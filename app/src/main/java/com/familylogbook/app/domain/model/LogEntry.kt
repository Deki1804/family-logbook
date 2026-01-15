package com.familylogbook.app.domain.model

import java.util.UUID

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    // Person or Entity reference (backward compatible with childId)
    val personId: String? = null, // person this entry relates to
    val entityId: String? = null, // entity this entry relates to (car, house, etc.)
    @Deprecated("Use personId instead", ReplaceWith("personId"))
    val childId: String? = null, // kept for backward compatibility
    
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String,
    val category: Category = Category.OTHER,
    val tags: List<String> = emptyList(),
    val mood: Mood? = null,
    val hasAttachment: Boolean = false,
    
    // AI-generated advice (optional)
    val aiAdvice: String? = null,
    
    // Reminders (for future dates like car service, bills, etc.)
    val reminderDate: Long? = null,
    
    // Feeding tracking (for babies/toddlers)
    val feedingType: FeedingType? = null, // breast_left, breast_right, bottle
    val feedingAmount: Int? = null, // ml for bottle feeding
    
    // Health tracking
    val temperature: Float? = null, // in Celsius
    val symptoms: List<String>? = null, // list of symptoms (e.g., "temperatura", "kašalj", "povraćanje")
    
    // Medicine tracking (Parent OS core feature)
    val medicineGiven: String? = null, // medicine name (e.g., "Nurofen", "Paracetamol")
    val medicineDosage: String? = null, // dosage amount (e.g., "5ml", "1 tableta", "10mg")
    val medicineTimestamp: Long? = null, // when medicine was given (timestamp)
    val nextMedicineTime: Long? = null, // when next medicine dose should be taken (calculated from interval)
    val medicineIntervalHours: Int? = null, // interval between medicine doses in hours (e.g., 6, 8, 12)
    
    // Finance tracking
    val amount: Double? = null, // for finance entries
    val currency: String? = null, // e.g., "EUR", "USD"
    
    // Auto tracking
    val mileage: Int? = null, // for car entries
    val serviceType: String? = null, // e.g., "Oil change", "Tire replacement"
    
    // Shopping tracking
    val shoppingItems: List<String>? = null, // list of shopping items/products
    val checkedShoppingItems: Set<String>? = null, // items that have been checked/purchased
    
    // Vaccination tracking
    val vaccinationName: String? = null, // vaccination name (e.g., "MMR", "DTP-Hib-IPV")
    val vaccinationDate: Long? = null, // date when vaccination was given
    val nextVaccinationDate: Long? = null, // recommended date for next vaccination
    val nextVaccinationMessage: String? = null, // message about next vaccination
    
    // Day tracking (Parent OS core feature)
    val dayEntryType: DayEntryType? = null, // TODAY, CHECKLIST, REMINDER
    val isCompleted: Boolean? = null, // for checklist items
    val dueDate: Long? = null // for reminders (same as reminderDate, but more explicit for DAY category)
)

enum class FeedingType {
    BREAST_LEFT,
    BREAST_RIGHT,
    BOTTLE
}

enum class DayEntryType {
    TODAY,      // Daily overview/journal
    CHECKLIST,  // Checklist item (e.g., "Pack backpack", "Prepare clothes")
    REMINDER    // General reminder (e.g., "Parent meeting", "Excursion")
}
