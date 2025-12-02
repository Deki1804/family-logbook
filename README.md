# Family Logbook 👶📖

Aplikacija za roditelje za praćenje važnih događaja o djeci i obiteljskom životu.

## Phase 1 - MVP

Ovo je početna MVP verzija sa:
- In-memory pohranom podataka (bez perzistencije za sada)
- Jednostavnom klasifikacijom na temelju ključnih riječi (fake AI)
- Osnovnim UI-om s Jetpack Compose
- Čistom arhitekturom (ui/domain/data slojevi)

## Značajke

- **Home/Timeline**: Pregled svih unosa u feedu
- **Add Entry**: Kreiranje novih unosa s automatskom kategorizacijom
- **Stats**: Statistika po kategorijama i raspoloženju
- **Settings**: Upravljanje profilima djece

## Arhitektura

```
app/src/main/java/com/familylogbook/app/
├── domain/
│   ├── classifier/          # EntryClassifier (fake AI za Phase 1)
│   ├── model/               # Child, LogEntry, Category, Mood
│   └── repository/          # LogbookRepository interface
├── data/
│   └── repository/          # InMemoryLogbookRepository implementacija
└── ui/
    ├── navigation/          # Definicije ekrana
    ├── screen/             # Compose ekrani
    ├── theme/              # Material3 teme
    └── viewmodel/          # ViewModels (MVVM)
```

## Tech Stack

- **Jezik**: Kotlin
- **UI**: Jetpack Compose
- **Arhitektura**: MVVM + Clean Architecture
- **Navigacija**: Navigation Compose
- **State Management**: StateFlow

## Početak rada

### Preduvjeti

- Android Studio (Hedgehog ili noviji)
- JDK 17
- Android SDK (minSdk 24, targetSdk 34)
- Google račun (za Firebase)

### Postupak

1. **Kloniraj repozitorij** (ili otvori postojeći folder u Android Studio)
   ```bash
   git clone <repo-url>
   cd "Family Logbook"
   ```

2. **Postavi local.properties**
   - Ako ne postoji `local.properties`, kopiraj `local.properties.template` u `local.properties`
   - Postavi putanju do svog Android SDK-a:
     ```
     sdk.dir=C\:\\Users\\TvojeIme\\AppData\\Local\\Android\\Sdk
     ```

3. **Otvori projekt u Android Studio**
   - File → Open → odaberi folder projekta
   - Android Studio će automatski sinkronizirati Gradle

4. **Sinkroniziraj Gradle**
   - Klikni "Sync Now" ako se pojavi notifikacija
   - Ili File → Sync Project with Gradle Files

5. **Postavi Firebase** (opcionalno za Phase 1, potrebno za Phase 2)
   - Detaljne upute: [FIREBASE_SETUP.md](FIREBASE_SETUP.md)
   - Quick start: [FIREBASE_QUICK_START.md](FIREBASE_QUICK_START.md)

6. **Pokreni aplikaciju**
   - Poveži Android uređaj ili pokreni emulator
   - Klikni Run (▶️) ili pritisni Shift+F10

## Git Setup

Projekt je spreman za Git:
- ✅ `.gitignore` - konfiguriran za Android projekte
- ✅ `.gitattributes` - postavljen za konzistentne line endings
- ✅ `local.properties` se automatski ignorira (ne commitaj ga!)

### Detaljne upute
- **GitHub Setup**: [GITHUB_SETUP.md](GITHUB_SETUP.md) - kompletan vodič
- **Quick Start**: [GITHUB_QUICK_START.md](GITHUB_QUICK_START.md) - brzi pregled
- **Checklist**: [GITHUB_CHECKLIST.md](GITHUB_CHECKLIST.md) - za praćenje napretka

### Brzi start

```bash
# Inicijaliziraj repo
git init
git branch -M main

# Dodaj sve fajlove
git add .

# Prvi commit
git commit -m "Initial commit: Family Logbook MVP - Phase 1"

# Poveži s GitHub-om (zamijeni USERNAME i REPO_NAME)
git remote add origin https://github.com/USERNAME/REPO_NAME.git
git push -u origin main
```

**Napomena**: GitHub više ne koristi password autentifikaciju. Trebaš kreirati Personal Access Token. Vidi [GITHUB_SETUP.md](GITHUB_SETUP.md) za detalje.

## Sljedeći koraci (Future Phases)

- [ ] Dodaj Firebase (Auth + Firestore)
- [ ] Zamijeni fake classifier s pravim AI backendom
- [ ] Dodaj priloge slika
- [ ] Dodaj praćenje datuma rođenja
- [ ] Dodaj export funkcionalnost
- [ ] Dodaj podsjetnike i notifikacije

## Napomene

- Svi podaci su in-memory i gube se pri restartu aplikacije
- Klasifikacija koristi jednostavno prepoznavanje ključnih riječi
- Još nema pravog AI backenda (samo Phase 1)

## Verzija

- **Version Code**: 1
- **Version Name**: 1.0
- **Phase**: 1 (MVP)

