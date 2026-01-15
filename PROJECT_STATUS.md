# Parent OS - Status Projekta i Plan

**Datum ažuriranja:** 2025-12-08  
**Status:** U razvoju - Beta verzija

---

## 📋 Sadržaj

1. [Pregled projekta](#pregled-projekta)
2. [Što smo sve radili](#što-smo-sve-radili)
3. [Trenutno stanje](#trenutno-stanje)
4. [Riješeni problemi](#riješeni-problemi)
5. [Poznati problemi](#poznati-problemi)
6. [Plan rada](#plan-rada)
7. [Arhitektura i tehnologije](#arhitektura-i-tehnologije)

---

## 🎯 Pregled projekta

**Parent OS** je Android aplikacija fokusirana na **zdravlje djece** (0–8 godina). Omogućava korisnicima da:
- Bilježe zdravstvene događaje (lijekovi, simptomi, temperatura)
- Prate cjepiva i preporuke sljedećih cjepiva
- Vode dnevne obaveze (checklist/podsjetnici)
- Koriste podsjetnike (lijekovi, hranjenje za bebe)
- Rade backup/restore (JSON/CSV)

**Tehnologije:**
- Kotlin + Jetpack Compose (Material3)
- Firebase (Authentication + Firestore)
- MVVM arhitektura
- WorkManager za notifikacije

---

## ✅ Što smo sve radili

### 1. **Početni razvoj i osnovna funkcionalnost**
- Implementirana osnovna struktura aplikacije
- Firebase integracija (Auth + Firestore)
- Osnovni CRUD operacije za log entries
- Profili za osobe, djecu i entitete
- Kategorizacija unosa (HEALTH, SHOPPING, FINANCE, SCHOOL, WORK, itd.)

### 2. **UI/UX modernizacija**
- **Kompletan redesign Home screena** - transformiran iz feed-a u "FamilyOS kontrolni centar"
- Implementirani novi komponenti:
  - `TodayOverviewCard` - kompaktni pregled današnjih događaja
  - `ImportantCardsGrid` - 2x2 grid akcijskih kartica (dinamički prikaz)
  - `RecentEntriesList` - kondenzirani prikaz nedavnih unosa
  - `AdvicePill` - kompaktni "pill" stil za savjete
- Uklonjeni zastarjeli komponenti (SearchBar, TodaySummaryCard, FinanceSummaryCard)
- Implementiran Material3 dizajn sustav

### 3. **AI-powered funkcionalnosti**
- **AdviceEngine** - generira savjete na temelju unosa
  - Parent OS: savjeti su fokusirani na health kategorije (bez shopping/smart-home feature-a)

### 4. **Navigacija i routing**
- Implementiran Navigation Compose
- Dodan `AdviceDetailScreen` za prikaz detalja savjeta
- Razdvojeni click handleri:
  - `onAdviceClick` → Advice detail screen
  - `onEntryClick` → Entry detail (read-only)
  - `onEditEntryClick` → Edit screen

### 5. **Smart Home integracija**
Uklonjeno iz Parent OS scope-a (legacy FamilyOS feature).

### 6. **Optimizacije i bug fixovi**
- **Shopping deals API optimizacija:**
  - Cache u ViewModel-u (`_shoppingDealsByEntryId`)
  - Ograničenje na max 3 concurrent requesta
  - Jedan poziv po entryju (tracking kroz `_shoppingDealsLoaded`)
  - Proper handling `CancellationException`
- **Gradle JVM konfiguracija:**
  - Java 17 toolchain auto-detect i auto-provision
  - Riješen `Incompatible Gradle JVM` error
- **Firestore permissions:**
  - Dodani auth checks u repository metodama
  - Riješen `PERMISSION_DENIED` error
- **Compose layout:**
  - Riješen `Vertically scrollable component was measured with an infinity maximum height constraints`
  - Refaktoriran `RecentEntriesList` (Column umjesto LazyColumn)

### 7. **Dinamički prikaz komponenti**
- `ImportantCardsGrid` prikazuje kartice samo ako su relevantne:
  - "Djeca" kartica samo ako postoje djeca u profilu
  - "House" kartica samo ako postoji house entity
  - "Smart Home" kartica uvijek dostupna
  - "Shopping" kartica za shopping kategoriju

### 8. **Shopping list funkcionalnost**
- Checkbox integracija u `RecentEntriesList`
- Prikaz prvih 3 shopping itema s "...+ X more" indikatorom
- `updateShoppingItemChecked()` metoda u ViewModel-u

### 9. **Lokalizacija**
- Svi tekstovi prevedeni na hrvatski
- Advice templates na hrvatskom
- Shopping deals formatiranje na hrvatskom

---

## 🔄 Trenutno stanje

### Funkcionalno
✅ **Home Screen** - potpuno funkcionalan, moderni dizajn  
✅ **Entry management** - CRUD operacije rade  
✅ **Advice system** - generiranje i prikaz savjeta radi  
✅ **Navigation** - osnovna navigacija radi  
✅ **Firebase Auth** - Google sign-in radi  
✅ **Firestore** - sve operacije rade  
✅ **Export/Import** - JSON/CSV radi (uključuje DOB i health polja)  
✅ **Timer** - radi preko WorkManager (bez exact-alarm permisija)  

### U razvoju / Potrebno testirati
⚠️ **AdviceDetailScreen navigacija** - nedavno popravljena, treba testirati  
⚠️ **Back button handling** - popravljen, treba provjeriti crash scenarije  

### Poznati problemi
❌ **AdviceDetailScreen crash na back** - korisnik je prijavio da se app ruši kada ide back s AdviceDetailScreen-a (prazan ekran prije crasha)

---

## 🐛 Riješeni problemi

### 1. **Gradle JVM Incompatibility**
**Problem:** `Incompatible Gradle JVM` error pri kompilaciji  
**Rješenje:** 
- Dodano u `gradle.properties`:
  ```
  org.gradle.java.toolchain.auto-detect=true
  org.gradle.java.toolchain.auto-provision=true
  ```
- Java 17 toolchain konfiguriran u `build.gradle.kts`

### 2. **Shopping Deals - Previše API poziva**
**Problem:** Konstantni API pozivi na svaki scroll/recomposition, "coroutine scope left composition" greške  
**Rješenje:**
- Cache implementiran u `HomeViewModel` (`_shoppingDealsByEntryId`)
- Tracking loaded entries (`_shoppingDealsLoaded`)
- Ograničenje na max 3 concurrent requesta
- Proper exception handling za `CancellationException`

### 3. **Advice Card navigacija**
**Problem:** Klik na Advice card/pill otvara Edit Entry umjesto Advice detail  
**Rješenje:**
- Razdvojeni click handleri u `HomeScreen`
- Implementiran `AdviceDetailScreen`
- ViewModel scope popravljen (activity scope za shared instance)

### 4. **Smart Home kartica**
**Problem:** Uvijek otvara Play Store, čak i kad je Google Home instaliran  
**Rješenje:**
- Refaktoriran `SmartHomeManager.openGoogleHomeApp()`
- Prvo pokušava direktno pokrenuti app
- Tek ako ne uspije, otvara Play Store

### 5. **Firestore PERMISSION_DENIED**
**Problem:** Greške pri dohvaćanju podataka  
**Rješenje:**
- Dodani auth checks u `FirestoreLogbookRepository`
- Provjera `currentUser` i `currentUserId` prije svakog poziva

### 6. **Compose Layout Crash**
**Problem:** `Vertically scrollable component was measured with an infinity maximum height constraints`  
**Rješenje:**
- Refaktoriran `RecentEntriesList` - `Column` umjesto `LazyColumn`

### 7. **EntityType.PET error**
**Problem:** `Unresolved reference: PET` u `ImportantCardsGrid`  
**Rješenje:**
- Uklonjen nepostojeći `EntityType.PET` reference
- Dodan fallback `else -> entity.emoji.ifEmpty { "📦" }`

### 8. **Shopping items extraction**
**Problem:** Ekstrahira cijele fraze ("treba kupiti kruh") umjesto čistih artikala  
**Rješenje:**
- Poboljšan `extractShoppingItems()` u `AdviceEngine`
- Filtriranje common words ("treba", "kupiti", "lista", itd.)
- Limit na max 5 artikala po entryju

### 9. **Shopping deals tekst formatiranje**
**Problem:** "Semi-random" tekst u savjetima  
**Rješenje:**
- Strukturirani format:
  - Naslov: "Akcije za tvoju shopping listu"
  - Podnaslov: "Našao sam akcije za: artikal1, artikal2..."
  - Lista: "artikal – trgovina – cijena"
- Grupiranje dealova po produktu (samo najbolji deal po artiklu)

---

## ⚠️ Poznati problemi

### 1. **AdviceDetailScreen crash na back** 🔴 KRITIČNO
**Opis:** Korisnik prijavljuje da se app ruši kada ide back s AdviceDetailScreen-a. Prije crasha vidi prazan ekran.  
**Status:** Nedavno popravljeno (ViewModel scope), ali treba testirati  
**Prioritet:** VISOK  
**Lokacija:** `MainActivity.kt` - `advice_detail` composable

**Mogući uzroci:**
- ViewModel scope issue (možda se kreira novi instance umjesto shared)
- Null advice state handling
- Navigation back stack issue

**Sljedeći koraci:**
- Testirati navigaciju u detail screen i back
- Dodati dodatne null checks
- Provjeriti logove za stack trace

### 2. **Shopping deals - Job cancellation**
**Opis:** Pojavljuju se "Job was cancelled" greške u logovima  
**Status:** Djelomično riješeno (proper exception handling), ali još se pojavljuje  
**Prioritet:** SREDNJI  
**Lokacija:** `HomeViewModel.kt` - `loadShoppingDealsForEntry()`

**Napomena:** Ovo je sada handled kao `CancellationException` i ne logira se kao error, ali treba provjeriti zašto se job-ovi cancelaju.

### 3. **Firebase Locale header**
**Opis:** `Ignoring header X-Firebase-Locale because its value was null` warning  
**Status:** Nije kritično, samo warning  
**Prioritet:** NIZAK

### 4. **ManagedChannelImpl warnings**
**Opis:** `Failed to resolve name. status={1}` warnings u logovima  
**Status:** Network-related, možda normalno ponašanje  
**Prioritet:** NIZAK

---

## 📝 Plan rada

### Kratkoročno (Sada - Sljedeći tjedan)

#### 1. **Testiranje i debug AdviceDetailScreen** 🔴 PRIORITET
- [ ] Reproducirati crash scenarij
- [ ] Provjeriti logove za stack trace
- [ ] Testirati različite navigacijske scenarije
- [ ] Dodati dodatne null checks i error handling
- [ ] Provjeriti ViewModel lifecycle

#### 2. **Shopping deals finalizacija**
- [ ] Testirati cache mehanizam pod opterećenjem
- [ ] Provjeriti da li se još uvijek šalju previše requestova
- [ ] Optimizirati limit (možda 5 umjesto 3 concurrent)
- [ ] Dodati retry mehanizam za failed requests

#### 3. **UI polish**
- [ ] Provjeriti sve tekstove (lokalizacija)
- [ ] Testirati na različitim screen size-ovima
- [ ] Provjeriti dark mode support
- [ ] Optimizirati loading states

### Srednjoročno (Sljedeći mjesec)

#### 4. **Notifikacije i podsjetnici**
- [ ] Implementirati WorkManager za reminder notifikacije
- [ ] Testirati background notifikacije
- [ ] Dodati reminder management UI

#### 5. **Statistike i analitika**
- [ ] Implementirati StatsScreen sa grafikama
- [ ] Dodati trend analizu
- [ ] Export funkcionalnost (CSV/PDF)

#### 6. **Offline support**
- [ ] Implementirati lokalno cache (Room database?)
- [ ] Sync mehanizam kada se vrati online
- [ ] Offline indicator u UI

### Dugoročno (Sljedeća 2-3 mjeseca)

#### 7. **Dodatne funkcionalnosti**
- [ ] Photo attachments za entries
- [ ] Voice notes integracija
- [ ] Calendar sync
- [ ] Export/Import podataka
- [ ] Multi-language support (engleski, njemački)

#### 8. **Performance optimizacije**
- [ ] Image caching i optimization
- [ ] Lazy loading za velike liste
- [ ] Database indexing
- [ ] Network request batching

#### 9. **Security i privacy**
- [ ] End-to-end encryption za osjetljive podatke
- [ ] Biometric authentication
- [ ] Privacy settings
- [ ] GDPR compliance review

#### 10. **Testing**
- [ ] Unit tests za ViewModels
- [ ] UI tests za kritične flow-ove
- [ ] Integration tests
- [ ] Performance tests

---

## 🏗️ Arhitektura i tehnologije

### Arhitektura
```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  - HomeScreen, AddEntryScreen, etc.     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│        ViewModel Layer (MVVM)            │
│  - HomeViewModel, AddEntryViewModel     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Domain Layer (Business Logic)        │
│  - AdviceEngine, EntryClassifier         │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Data Layer (Repository)             │
│  - FirestoreLogbookRepository            │
│  - GoogleCustomSearchService             │
└──────────────────────────────────────────┘
```

### Ključni fajlovi

**UI Screens:**
- `HomeScreen.kt` - glavni ekran, kontrolni centar
- `AddEntryScreen.kt` - dodavanje/uređivanje unosa
- `AdviceDetailScreen.kt` - prikaz detalja savjeta
- `SettingsScreen.kt` - postavke aplikacije

**UI Components:**
- `TodayOverviewCard.kt` - pregled današnjih događaja
- `ImportantCardsGrid.kt` - grid akcijskih kartica
- `RecentEntriesList.kt` - lista nedavnih unosa
- `AdvicePill.kt` - kompaktni prikaz savjeta

**ViewModels:**
- `HomeViewModel.kt` - glavni ViewModel, cache za shopping deals
- `AddEntryViewModel.kt` - upravljanje unosima
- `SettingsViewModel.kt` - postavke

**Domain:**
- `AdviceEngine.kt` - generiranje savjeta i shopping deals
- `EntryClassifier.kt` - klasifikacija unosa

**Data:**
- `FirestoreLogbookRepository.kt` - Firestore operacije
- `GoogleCustomSearchService.kt` - API integracija za shopping deals

**Navigation:**
- `MainActivity.kt` - glavna aktivnost, navigation setup
- `Screen.kt` - navigation routes

### Konfiguracija

**gradle.properties:**
```properties
# Google Custom Search API
GOOGLE_CSE_API_KEY=your_api_key_here
GOOGLE_CSE_ENGINE_ID=your_engine_id_here

# Gradle JVM
org.gradle.java.toolchain.auto-detect=true
org.gradle.java.toolchain.auto-provision=true
```

**build.gradle.kts:**
- Java 17 toolchain
- Kotlin 1.9+
- Compose BOM
- Firebase dependencies

---

## 📊 Metričke i monitoring

### Trenutno nema implementiranog:
- Analytics
- Crash reporting (osim osnovnih logova)
- Performance monitoring

### Preporučeno za budućnost:
- Firebase Crashlytics
- Firebase Analytics
- Performance monitoring
- Custom event tracking

---

## 🔐 Security considerations

### Trenutno:
- Firebase Auth za autentifikaciju
- Firestore Security Rules za autorizaciju
- API keys u `gradle.properties` (NE commitati!)

### Potrebno:
- Review Firestore Security Rules
- API key rotation plan
- Secrets management za production
- Rate limiting za API pozive

---

## 📚 Dokumentacija

### Postojeća:
- `COMPREHENSIVE_REVIEW.md` - detaljni pregled (možda zastario)
- `PROJECT_STATUS.md` - ovaj dokument
- `PRODUCTION_CHECKLIST.md` - checklist za production (ako postoji)
- `SECURITY_NOTES.md` - security napomene (ako postoji)

### Potrebno:
- User guide
- Developer setup guide
- API documentation
- Architecture decision records (ADR)

---

## 🎯 Sljedeći koraci (Prioritet)

1. **🔴 KRITIČNO:** Riješiti AdviceDetailScreen crash
2. **🟡 VISOKO:** Testirati i optimizirati shopping deals
3. **🟡 VISOKO:** Finalizirati UI polish
4. **🟢 SREDNJE:** Implementirati reminder notifikacije
5. **🟢 SREDNJE:** Dodati statistike i analitiku

---

## 📝 Notes za sljedeći chat

### Što provjeriti prvo:
1. AdviceDetailScreen crash - reprodukcija i stack trace
2. Shopping deals - provjeriti da li se još šalju previše requestova
3. ViewModel scope - provjeriti da li se koristi isti instance

### Što je gotovo:
- Home Screen redesign ✅
- Shopping deals API integracija ✅
- Cache mehanizam ✅
- Navigacija (osnovna) ✅
- Smart Home integracija ✅

### Što treba:
- Debug AdviceDetailScreen crash
- Finalizirati shopping deals optimizacije
- Testirati sve flow-ove end-to-end

---

**Zadnja izmjena:** 2025-12-08  
**Autor:** AI Assistant + User collaboration  
**Status:** Active development



