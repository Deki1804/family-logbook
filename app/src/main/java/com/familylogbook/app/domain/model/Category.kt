package com.familylogbook.app.domain.model

enum class Category {
    // Health & Wellness (Parent OS focus)
    MEDICINE,      // Lijekovi
    SYMPTOM,       // Simptomi
    VACCINATION,   // Cjepiva
    HEALTH,        // Opće zdravlje
    FEEDING,       // Hranjenje (za bebe)
    SLEEP,         // Spavanje (za bebe)
    MOOD,          // Raspoloženje
    DEVELOPMENT,   // Razvoj
    
    // Day & Activities
    DAY,           // Dnevne obaveze (vrtić, rutine, checklist)
    SCHOOL,        // Vrtić / škola
    
    // Legacy (keeping for backward compatibility - will be removed in future)
    AUTO,
    HOUSE,
    FINANCE,
    WORK,
    SHOPPING,
    SMART_HOME,
    KINDERGARTEN_SCHOOL,
    HOME,
    OTHER
}

