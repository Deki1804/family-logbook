# Firebase Quick Start - TL;DR ⚡

Ako već znaš što radiš, evo brzog pregleda:

## 5-Minutni Setup

1. **Firebase Console** → Create project → "Family Logbook"
2. **Add Android app** → Package: `com.familylogbook.app`
3. **Download** `google-services.json` → stavi u `app/` folder
4. **Firestore Database** → Create → Test mode → Enable
5. **Ažuriraj build.gradle.kts** (vidi dolje)
6. **Sync Gradle** → Gotovo! ✅

## Build.gradle.kts Promjene

### Root `build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false  // ← DODAJ
}
```

### `app/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")  // ← DODAJ na kraju
}

dependencies {
    // ... postojeće
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
}
```

## Provjera

- [ ] `google-services.json` u `app/` folderu?
- [ ] Gradle sinkroniziran?
- [ ] Nema grešaka?

**Za detaljne upute, vidi [FIREBASE_SETUP.md](FIREBASE_SETUP.md)**

