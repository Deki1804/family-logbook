# FamilyOS 👨‍👩‍👧‍👦📱

> **Vaš kompletan obiteljski život manager** - praćenje zdravlja, hranjenja, spavanja, financija, automobila, kuće i još puno toga!

## 🌟 O aplikaciji

FamilyOS (ranije Family Logbook) je sveobuhvatna Android aplikacija za upravljanje obiteljskim životom. Aplikacija omogućava roditeljima i članovima obitelji da lakše prate važne aspekte djece i obiteljskog života kroz jedinstveno sučelje.

**Trenutna verzija:** v0.9 (Interni beta)

## ✨ Glavne značajke

### 👶 Praćenje djece i obitelji
- **Zdravlje**: Temperatura, lijekovi, simptomi, podsjetnici za uzimanje lijekova
- **Hranjenje**: Praćenje hranjenja (dojenje, bočica), timer, podsjetnici
- **Spavanje**: Praćenje spavanja i budnosti
- **Razvoj**: Bilježenje razvojnih prekretnica
- **Raspoloženje**: Praćenje raspoloženja i emocija

### 🏠 Upravljanje kućom i entitetima
- **Automobili**: Servisi, kilometraža, troškovi
- **Kuća**: Popravci, računi, održavanje
- **Financije**: Praćenje troškova po kategorijama
- **Pametna kuća**: Integracija s Google Assistantom za upravljanje pametnim uređajima

### 📊 Statistika i pregledi
- Grafički prikazi temperature, hranjenja, spavanja
- Pregled troškova po kategorijama
- Statistički pregledi po djetetu/osobi/entitetu

### 🔔 Pametni podsjetnici
- **Lijekovi**: Automatski podsjetnici prema intervalu uzimanja
- **Hranjenje**: Podsjetnici za bebe (< 2 godine)
- **Servisi i termini**: Podsjetnici za važne događaje

### 💾 Sigurnost i backup
- **Firebase integracija**: Svi podaci se sigurno pohranjuju u cloudu
- **Anonimni login**: Brz start bez registracije
- **Upgrade accounta**: Mogućnost trajne registracije s Google ili email računom
- **Export/Import**: JSON i CSV backup/restore

### 🤖 Pametna klasifikacija
- Automatska kategorizacija unosa prema ključnim riječima
- Detekcija raspoloženja, temperature, lijekova
- Kontekstualni savjeti o zdravlju i prehrani (generalne preporuke, ne medicinski savjeti)

## 🏗️ Arhitektura

Aplikacija je izgrađena na čistoj slojevitoj arhitekturi:

```
app/src/main/java/com/familylogbook/app/
├── domain/
│   ├── classifier/          # EntryClassifier (rule-based AI)
│   ├── model/               # LogEntry, Person, Entity, Category, Mood
│   └── repository/          # LogbookRepository interface
├── data/
│   ├── auth/                # AuthManager (Firebase Auth)
│   ├── repository/          # FirestoreLogbookRepository, InMemoryLogbookRepository
│   ├── notification/        # NotificationManager, ReminderWorker
│   ├── export/              # ExportManager (JSON/CSV)
│   └── smarthome/           # SmartHomeManager, SmartHomeCommandParser
└── ui/
    ├── navigation/          # Screen definicije
    ├── screen/              # Compose ekrani
    ├── component/           # Reusable komponente
    ├── theme/               # Material3 teme
    └── viewmodel/           # ViewModels (MVVM)
```

### Slojevi

- **UI Layer**: Jetpack Compose ekrani i komponente
- **Domain Layer**: Čisti business logika i modeli
- **Data Layer**: Firebase Firestore, Auth, WorkManager, Export/Import

### Repozitoriji

- `InMemoryLogbookRepository`: Demo/offline testiranje
- `FirestoreLogbookRepository`: Pravi user-scoped podaci (`users/{uid}/entries`, `persons`, `entities`)

## 🛠️ Tech Stack

- **Jezik**: Kotlin
- **UI**: Jetpack Compose
- **Arhitektura**: MVVM + Clean Architecture
- **Navigacija**: Navigation Compose
- **State Management**: StateFlow, MutableStateFlow
- **Backend**: Firebase (Firestore, Auth)
- **Background Processing**: WorkManager
- **Notifications**: Android Notification Channels

## 📱 Preduvjeti

- Android Studio (Hedgehog ili noviji)
- JDK 17+
- Android SDK (minSdk 24, targetSdk 34)
- Google račun za Firebase

## 🚀 Početak rada

### 1. Kloniraj repozitorij

```bash
git clone <repo-url>
cd "Family Logbook"
```

### 2. Otvori projekt u Android Studio

- File → Open → odaberi folder projekta
- Android Studio će automatski sinkronizirati Gradle

### 3. Postavi Firebase

**Opcija A: Koristiš pravi Firebase projekt**

1. Kreiraj Firebase projekt na [Firebase Console](https://console.firebase.google.com)
2. Dodaj Android app u Firebase projekt
3. Preuzmi `google-services.json` i stavi ga u `app/` folder
4. Detaljne upute: [FIREBASE_SETUP.md](FIREBASE_SETUP.md) ili [FIREBASE_QUICK_START.md](FIREBASE_QUICK_START.md)

**Opcija B: Koristiš demo mode (bez Firebase)**

- U `MainActivity.kt` postavi `useFirestore = false`
- Aplikacija će koristiti `InMemoryLogbookRepository` za testiranje

### 4. Postavi Firestore Security Rules

Kopiraj pravila iz `firestore.rules` u Firebase Console → Firestore Database → Rules.

Detaljno objašnjenje: [FIRESTORE_SECURITY_RULES.md](FIRESTORE_SECURITY_RULES.md)

### 5. Pokreni aplikaciju

- Poveži Android uređaj ili pokreni emulator
- Klikni Run (▶️) ili pritisni `Shift+F10`

## 📚 Dokumentacija

- **[PROJECT_STATUS.md](PROJECT_STATUS.md)** - Kompletan pregled projekta, roadmap, status
- **[TODO_v1.0.md](TODO_v1.0.md)** - Konkretne akcije i checklist za v1.0
- **[QUICK_STATUS.md](QUICK_STATUS.md)** - Brzi pregled trenutnog stanja
- **[FIRESTORE_SECURITY_RULES.md](FIRESTORE_SECURITY_RULES.md)** - Objašnjenje sigurnosnih pravila
- **[MIPMAP_SETUP_INSTRUCTIONS.md](MIPMAP_SETUP_INSTRUCTIONS.md)** - Upute za postavljanje ikona

## 🔐 Sigurnost

- Svi podaci su user-scoped u Firestore (`users/{userId}/...`)
- Anonimni login omogućava brz start bez registracije
- Upgrade accounta omogućava trajnu pohranu podataka
- Firestore security rules osiguravaju da korisnici vide samo svoje podatke

## 📦 Export/Import

Aplikacija podržava backup i restore podataka:

- **JSON Export**: Kompletan backup svih podataka
- **CSV Export**: Pregledan export za analizu u Excel-u
- **JSON Import**: Restore podataka sa starog uređaja

Lokacija: Settings → Export & Import

## 🌍 Lokalizacija

- **Hrvatski jezik**: Potpuno prevedeno 🇭🇷
- Buduće verzije će podržavati više jezika

## 🎯 Trenutno stanje (v0.9)

### ✅ Što radi

- ✅ Firestore integracija (per-user storage)
- ✅ Anonimni login + upgrade path
- ✅ Background podsjetnici (lijekovi, hranjenje, servisi)
- ✅ Export/Import (JSON + CSV)
- ✅ Smart Home integracija
- ✅ Potpuno na hrvatskom jeziku
- ✅ Entity Profiles (Auto, Kuća, Financije)
- ✅ Symptom Helper
- ✅ Bogat domain model za sve aspekte obiteljskog života

### 🔄 U razvoju

- 🔄 README i Branding (trenutno)
- 🔄 Auth & Login Flow poboljšanja
- 🔄 Notifikacije Runtime Permission (Android 13+)
- 🔄 Export/Import proširenje (aiAdvice, symptoms)

### 📅 Planirano za v1.0

- 📅 Onboarding flow
- 📅 Today Summary na Home screen
- 📅 UX poboljšanja
- 📅 Error handling

## 🗺️ Roadmap

### v0.9 (Trenutno) - Interni Beta
- Osnovne funkcionalnosti
- Firebase integracija
- Lokalizacija
- Background processing

### v0.95 (Uskoro) - Prije javnog releasea
- README i branding
- Auth flow poboljšanja
- Notifikacije permission
- Export/Import proširenje

### v1.0 (Planirano) - Prva javna verzija
- Onboarding
- UX polish
- Finalni testing
- App Store / Play Store release

### Post v1.0
- Multi-language support
- Dark mode toggle
- Widgeti za home screen
- Wear OS companion app
- Sharing između obitelji

## 🤝 Doprinos

Projekt je trenutno u internoj beta fazi. Za pristup ili doprinos, kontaktiraj maintainera.

## 📄 Licenca

Privatni projekt - sva prava pridržana.

## 📞 Kontakt

Za pitanja, bugove ili prijedloge, otvori issue na repozitoriju.

---

**Napomena**: Ova aplikacija pruža generalne informacije i preporuke. Ne zamjenjuje profesionalni medicinski savjet. Za zdravstvene probleme, uvijek konzultiraj liječnika.

**Version**: 0.9  
**Phase**: 1.5 (Firebase Integration + Advanced Features)  
**Status**: Interni Beta - Gotovo za svakodnevnu upotrebu
