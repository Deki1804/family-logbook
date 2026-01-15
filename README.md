# Parent OS 💊📱

> **Health-focused app za roditelje djece 0–8 godina**: lijekovi, simptomi, cjepiva i jasne informacije spremne za pedijatra.

## 🌟 O aplikaciji

Parent OS je Android aplikacija fokusirana na **zdravstvene događaje** djece: brzo bilježenje, pregled povijesti i podsjetnici.

**Trenutna verzija:** 1.0.0-beta.1

## ✨ Glavne značajke

### 👶 Djeca (profili)
- Dodavanje djece/osoba (ime, tip, emoji, datum rođenja)
- Prikaz dobi (mjeseci/godine) i “health overview” po djetetu

### 💊 Zdravlje (core)
- **Lijekovi**: brzi unos + automatski podsjetnici prema intervalu
- **Simptomi**: temperatura + lista simptoma
- **Cjepiva**: evidencija + preporuka sljedećeg cjepiva (HR kalendar)
- **Hranjenje** (samo za bebe): timer + informativni podsjetnici

### 📋 Dan (rutine)
- Dnevne obaveze, checklist i podsjetnici

### 📊 Uvid
- Pregledi po kategorijama + povijest temperature / hranjenja (bočica)

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
│   └── timer/               # TimerWorker (WorkManager-based timers)
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
cd "family-logbook"
```

### 2. Otvori projekt u Android Studio

- File → Open → odaberi folder projekta
- Android Studio će automatski sinkronizirati Gradle

### 3. Postavi Firebase

**Opcija A: Koristiš pravi Firebase projekt**

1. Kreiraj Firebase projekt na [Firebase Console](https://console.firebase.google.com)
2. Dodaj Android app u Firebase projekt
3. Preuzmi `google-services.json` i stavi ga u `app/` folder

**Opcija B: Koristiš demo mode (bez Firebase)**

- U `MainActivity.kt` postavi `useFirestore = false`
- Aplikacija će koristiti `InMemoryLogbookRepository` za testiranje

### 4. Postavi Firestore Security Rules

Kopiraj pravila iz `firestore.rules` u Firebase Console → Firestore Database → Rules.

### 5. Pokreni aplikaciju

- Poveži Android uređaj ili pokreni emulator
- Klikni Run (▶️) ili pritisni `Shift+F10`

## 📚 Dokumentacija

- **[PROJECT_STATUS.md](PROJECT_STATUS.md)** ⭐ - Status projekta i plan
- **[FIREBASE_SETUP.md](FIREBASE_SETUP.md)** - Firebase setup
- **[FIREBASE_RULES_DEPLOY.md](FIREBASE_RULES_DEPLOY.md)** - Deploy Firestore rules
- **[PRODUCTION_CHECKLIST.md](PRODUCTION_CHECKLIST.md)** - Checklist za release
- **[SECURITY_NOTES.md](SECURITY_NOTES.md)** - Sigurnosne napomene
- **[PRIVACY_POLICY.md](PRIVACY_POLICY.md)** - Pravila privatnosti
- **[TERMS_OF_SERVICE.md](TERMS_OF_SERVICE.md)** - Uvjeti korištenja
- **[GDPR_COMPLIANCE.md](GDPR_COMPLIANCE.md)** - GDPR usklađenost

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

## 🤝 Doprinos

Projekt je trenutno u internoj beta fazi. Za pristup ili doprinos, kontaktiraj maintainera.

## 📄 Licenca

Privatni projekt - sva prava pridržana.

## 📞 Kontakt

Za pitanja, bugove ili prijedloge, otvori issue na repozitoriju.

---

**Napomena**: Ova aplikacija pruža generalne informacije i preporuke. Ne zamjenjuje profesionalni medicinski savjet. Za zdravstvene probleme, uvijek konzultiraj liječnika.
