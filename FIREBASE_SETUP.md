# Firebase Setup - Detaljne Upute 🔥

Ovaj vodič će te provesti kroz cijeli proces postavljanja Firebase projekta i Firestore baze podataka za Family Logbook aplikaciju.

---

## 📋 Korak 1: Kreiranje Firebase Projekta

### 1.1. Otvori Firebase Console

1. Idi na [Firebase Console](https://console.firebase.google.com/)
2. Prijavi se sa svojim Google računom
3. Ako nemaš projekat, klikni **"Add project"** ili **"Create a project"**

### 1.2. Kreiraj Novi Projekt

1. **Ime projekta**: Unesi `Family Logbook` (ili bilo koje ime koje želiš)
2. **Google Analytics**: 
   - Preporučujem da **uključiš** Google Analytics (korisno za budućnost)
   - Odaberi ili kreiraj Google Analytics account
3. Klikni **"Create project"**
4. Sačekaj da se projekt kreira (30-60 sekundi)

---

## 📱 Korak 2: Dodavanje Android Aplikacije

### 2.1. Dodaj Android App u Firebase

1. U Firebase Console-u, klikni na **Android ikonu** (ili "Add app" → Android)
2. Unesi podatke:
   - **Android package name**: `com.familylogbook.app`
     - ⚠️ **VAŽNO**: Ovo mora točno odgovarati `applicationId` u `app/build.gradle.kts`!
   - **App nickname** (opcionalno): `Family Logbook`
   - **Debug signing certificate SHA-1** (opcionalno za sada, možeš preskočiti)
3. Klikni **"Register app"**

### 2.2. Preuzmi google-services.json

1. Firebase će ti ponuditi da preuzmeš `google-services.json` fajl
2. **Preuzmi fajl** na svoj računalo
3. **Kopiraj fajl** u `app/` folder projekta (ne u `app/src/main/`, nego direktno u `app/`)
   ```
   Family Logbook/
   └── app/
       ├── build.gradle.kts
       ├── google-services.json  ← OVDJE!
       └── src/
   ```

### 2.3. Provjeri google-services.json

Otvori `google-services.json` i provjeri da sadrži:
- `project_id` - ID tvog Firebase projekta
- `package_name` - mora biti `com.familylogbook.app`
- `client` sekciju s Android podacima

---

## 🗄️ Korak 3: Postavljanje Firestore Database

### 3.1. Kreiraj Firestore Database

1. U Firebase Console-u, idi na **"Firestore Database"** u lijevom meniju
2. Klikni **"Create database"**
3. Odaberi **"Start in test mode"** (za početak, kasnije ćemo dodati pravila)
   - ⚠️ **NAPOMENA**: Test mode dozvoljava sve read/write operacije. OK za development, ali **NE za production!**
4. Odaberi **lokaciju** za bazu podataka:
   - Preporučujem: `europe-west` (bliže Hrvatskoj) ili `us-central`
   - Ovo se ne može promijeniti kasnije!
5. Klikni **"Enable"**

### 3.2. Firestore Security Rules (za sada - test mode)

Za sada ćemo koristiti test mode. Kasnije ćemo dodati pravila:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null; // Samo autentificirani korisnici
    }
  }
}
```

**Za sada**: Ostavi test mode, dodati ćemo auth kasnije.

---

## 🔧 Korak 4: Konfiguracija Android Projekta

### 4.1. Dodaj Google Services Plugin

U `build.gradle.kts` (root level), dodaj:

```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
```

ILI jednostavnije - u `settings.gradle.kts` plugins blok (ako koristiš plugin management).

### 4.2. Ažuriraj build.gradle.kts fajlove

**Root `build.gradle.kts`**:
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false  // ← DODAJ OVO
}
```

**`app/build.gradle.kts`**:
- Na **kraju fajla** (nakon dependencies bloka), dodaj:
```kotlin
plugins {
    // ... postojeći plugins
    id("com.google.gms.google-services")  // ← DODAJ OVO
}
```

### 4.3. Dodaj Firebase Dependencies

U `app/build.gradle.kts`, u `dependencies` blok, dodaj:

```kotlin
dependencies {
    // ... postojeće dependencies
    
    // Firebase BOM (Bill of Materials) - upravlja verzijama
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Firebase Auth (za kasnije)
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Firebase Storage (za slike, ako treba)
    // implementation("com.google.firebase:firebase-storage-ktx")
}
```

---

## ✅ Korak 5: Provjera i Test

### 5.1. Sinkroniziraj Gradle

1. U Android Studio, klikni **"Sync Now"** ili
2. File → Sync Project with Gradle Files
3. Sačekaj da se sinkronizacija završi

### 5.2. Provjeri da nema grešaka

- Ako vidiš grešku o `google-services.json`, provjeri da je fajl u `app/` folderu
- Ako vidiš grešku o package name, provjeri da se podudara s `applicationId`

### 5.3. Test Konekcije (opcionalno)

Možeš dodati jednostavan test u `MainActivity` da provjeriš da Firebase radi:

```kotlin
import com.google.firebase.firestore.FirebaseFirestore

// U onCreate ili negdje
val db = FirebaseFirestore.getInstance()
Log.d("Firebase", "Firestore instance created: ${db.app.name}")
```

---

## 📊 Korak 6: Firestore Struktura (Plan)

Za Family Logbook, planirana struktura:

```
firestore/
├── users/
│   └── {userId}/
│       ├── children/
│       │   └── {childId}/
│       │       ├── name: String
│       │       ├── dateOfBirth: Timestamp
│       │       ├── avatarColor: String
│       │       └── emoji: String
│       └── entries/
│           └── {entryId}/
│               ├── childId: String? (nullable)
│               ├── timestamp: Timestamp
│               ├── rawText: String
│               ├── category: String
│               ├── tags: Array<String>
│               ├── mood: String? (nullable)
│               └── hasAttachment: Boolean
```

**Za sada**: Ne kreiraj kolekcije ručno - aplikacija će ih kreirati automatski kada počneš pisati podatke.

---

## 🔐 Korak 7: Security Rules (Kasnije)

Kada dodamo autentifikaciju, ažuriraj pravila u Firebase Console:

1. Idi na Firestore Database → Rules
2. Zamijeni test mode pravilima:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users mogu čitati/pisati samo svoje podatke
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      match /children/{childId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /entries/{entryId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

**Za sada**: Ostavi test mode, dodati ćemo auth u sljedećoj fazi.

---

## 🎯 Checklist

- [ ] Firebase projekt kreiran
- [ ] Android app dodana u Firebase
- [ ] `google-services.json` preuzet i stavljen u `app/` folder
- [ ] Google Services plugin dodan u `build.gradle.kts`
- [ ] Firebase dependencies dodane
- [ ] Gradle sinkroniziran bez grešaka
- [ ] Firestore Database kreirana (test mode)
- [ ] Projekt kompajlira bez grešaka

---

## 🆘 Troubleshooting

### Problem: "File google-services.json is missing"
- **Rješenje**: Provjeri da je fajl u `app/` folderu (ne u `app/src/main/`)

### Problem: "Package name mismatch"
- **Rješenje**: Provjeri da je `package_name` u `google-services.json` jednak `applicationId` u `app/build.gradle.kts`

### Problem: Gradle sinkronizacija ne radi
- **Rješenje**: 
  1. File → Invalidate Caches / Restart
  2. Provjeri da su sve verzije ispravne
  3. Provjeri internet konekciju (Gradle preuzima dependencies)

### Problem: "FirebaseApp not initialized"
- **Rješenje**: Provjeri da je `google-services.json` u `app/` folderu i da je plugin dodan

---

## 📚 Korisni Linkovi

- [Firebase Console](https://console.firebase.google.com/)
- [Firebase Android Dokumentacija](https://firebase.google.com/docs/android/setup)
- [Firestore Dokumentacija](https://firebase.google.com/docs/firestore)
- [Firebase Security Rules](https://firebase.google.com/docs/firestore/security/get-started)

---

**Kada završiš sve korake, javi mi pa ćemo nastaviti s implementacijom Firestore repository-ja!** 🚀

