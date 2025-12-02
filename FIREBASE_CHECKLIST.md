# Firebase Setup Checklist ✅

Koristi ovu checklistu dok postavljaš Firebase.

## Firebase Projekt

- [ ] Otvoren [Firebase Console](https://console.firebase.google.com/)
- [ ] Kreiran novi projekt "Family Logbook"
- [ ] Google Analytics uključen (opcionalno)
- [ ] Projekt uspješno kreiran

## Android App u Firebase

- [ ] Kliknut "Add app" → Android
- [ ] Package name unesen: `com.familylogbook.app`
- [ ] App nickname unesen (opcionalno)
- [ ] App registrirana u Firebase
- [ ] `google-services.json` preuzet
- [ ] `google-services.json` kopiran u `app/` folder projekta

## Firestore Database

- [ ] Otvoren "Firestore Database" u Firebase Console
- [ ] Kliknut "Create database"
- [ ] Odabran "Start in test mode"
- [ ] Odabrana lokacija (npr. `europe-west`)
- [ ] Database uspješno kreirana

## Android Projekt Konfiguracija

### Root build.gradle.kts
- [ ] Dodan plugin: `id("com.google.gms.google-services") version "4.4.0" apply false`

### app/build.gradle.kts
- [ ] Dodan plugin: `id("com.google.gms.google-services")` (na kraju plugins bloka)
- [ ] Dodan Firebase BOM: `implementation(platform("com.google.firebase:firebase-bom:32.7.0"))`
- [ ] Dodan Firestore: `implementation("com.google.firebase:firebase-firestore-ktx")`
- [ ] Dodan Auth: `implementation("com.google.firebase:firebase-auth-ktx")`

## Provjera

- [ ] Gradle sinkroniziran (File → Sync Project with Gradle Files)
- [ ] Nema grešaka u Gradle sinkronizaciji
- [ ] Projekt se kompajlira bez grešaka
- [ ] `google-services.json` je u `app/` folderu (ne u `app/src/main/`)
- [ ] Package name u `google-services.json` odgovara `applicationId` u `app/build.gradle.kts`

## Test (Opcionalno)

- [ ] Aplikacija se pokreće bez grešaka
- [ ] Firebase se inicijalizira (možeš dodati log u MainActivity)

---

**Kada su svi checkboxi označeni, javi mi pa ćemo nastaviti s implementacijom!** 🚀

