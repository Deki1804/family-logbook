# TODO List za FamilyOS v1.0

> Konkretne akcije koje treba napraviti prije prve javne verzije

## 🔴 PRIORITET 1: Kritično za v1.0

### [ ] (A) README i Branding
**Fajl:** `README.md`

**Akcije:**
1. Ažurirati README.md sa realnim stanjem projekta
2. Objasniti:
   - Anonimni login + Firestore integraciju
   - Podsjetnike i background processing
   - Export/Import funkcionalnost
   - Persone/entitete sistem
   - Smart Home integraciju
3. Dodati screenshot-e glavnih ekrana
4. Ažurirati "Phase" status (Phase 1.5/2 umjesto "Phase 1 - in-memory only")
5. Riješiti naming:
   - Odlučiti se: "FamilyOS" vs "Family Logbook"
   - Ažurirati branding konzistentno kroz projekt

**Vrijeme:** 1-2h

---

### [ ] (B) Auth & Login Flow
**Fajlovi:** 
- `app/src/main/java/com/familylogbook/app/ui/screen/SettingsScreen.kt`
- `app/src/main/java/com/familylogbook/app/MainActivity.kt`
- `app/src/main/java/com/familylogbook/app/ui/screen/LoginScreen.kt`

**Akcije:**

1. **U SettingsScreen dodati Account sekciju:**
   ```kotlin
   // Ako je user anonimni
   Card {
       Text("🔒 Nadogradi račun")
       Text("Spremi svoje podatke trajno s Google ili email računom")
       Button(onClick = { onNavigateToLogin() }) {
           Text("Nadogradi račun")
       }
   }
   
   // Ako je user "pravi" (email)
   Card {
       Text("✅ Prijavljen")
       Text(currentUser.email)
       TextButton(onClick = { /* promijeni lozinku */ }) {
           Text("Promijeni lozinku")
       }
   }
   ```

2. **U MainActivity dodati Splash/Loading screen:**
   - Ukloniti `runBlocking { authManager.ensureSignedIn() }` iz onCreate
   - Dodati SplashScreen composable
   - U SplashScreen napraviti async auth check
   - Tek kad je auth spreman, navigirati na Home

3. **Povezati LoginScreen:**
   - U SettingsScreen dodati callback `onNavigateToLogin: () -> Unit`
   - U MainActivity dodati composable za `Screen.Login.route`
   - Navigacija: Settings → Login Screen

**Vrijeme:** 2-3h

---

### [ ] (C) Notifikacije Runtime Permission (Android 13+)
**Fajlovi:**
- `app/src/main/java/com/familylogbook/app/MainActivity.kt`
- Ili kreirati `app/src/main/java/com/familylogbook/app/ui/screen/SettingsScreen.kt` ekstenziju

**Akcije:**

1. **Dodati permission check u MainActivity:**
   ```kotlin
   import android.Manifest
   import androidx.activity.compose.rememberLauncherForActivityResult
   import androidx.activity.result.contract.ActivityResultContracts
   import androidx.core.app.NotificationManagerCompat
   
   // U FamilyLogbookApp composable:
   val context = LocalContext.current
   val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
   
   val requestPermissionLauncher = rememberLauncherForActivityResult(
       contract = ActivityResultContracts.RequestPermission()
   ) { isGranted ->
       if (!isGranted) {
           // Prikaži poruku da notifikacije nisu omogućene
       }
   }
   
   LaunchedEffect(Unit) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
           if (!notificationsEnabled) {
               requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
           }
       }
   }
   ```

2. **Ili dodati u SettingsScreen:**
   - Dodati karticu "Notifikacije" sa statusom
   - Gumb za request permission ako nije dan
   - Friendly poruka ako permission nije dan

**Vrijeme:** 1h

---

### [ ] (D) Export/Import Proširenje
**Fajl:** `app/src/main/java/com/familylogbook/app/data/export/ExportManager.kt`

**Akcije:**

1. **Dodati u JSON export (`exportToJson`):**
   ```kotlin
   // U entryObj dodati:
   entry.aiAdvice?.let { 
       entryObj.put("aiAdvice", it) 
   }
   
   entry.symptoms?.takeIf { it.isNotEmpty() }?.let { symptomsList ->
       val symptomsArray = JSONArray()
       symptomsList.forEach { symptom ->
           symptomsArray.put(symptom)
       }
       entryObj.put("symptoms", symptomsArray)
   }
   ```

2. **Dodati u JSON import (`parseJsonImport`):**
   ```kotlin
   // U LogEntry constructor dodati:
   aiAdvice = entryObj.optString("aiAdvice").takeIf { it.isNotEmpty() },
   
   symptoms = if (entryObj.has("symptoms")) {
       val symptomsArray = entryObj.getJSONArray("symptoms")
       (0 until symptomsArray.length()).mapNotNull { 
           symptomsArray.getString(it) 
       }
   } else null
   ```

3. **Testirati:**
   - Export entryja sa `aiAdvice` i `symptoms`
   - Import istog JSON-a
   - Provjeriti da se sve učitava ispravno

**Vrijeme:** 1h

---

## 🟡 PRIORITET 2: Poboljšanja za v1.0

### [ ] (E) Smart Home UX Poboljšanja
**Fajlovi:**
- `app/src/main/java/com/familylogbook/app/ui/screen/HomeScreen.kt`
- `app/src/main/java/com/familylogbook/app/ui/screen/AddEntryScreen.kt`
- `app/src/main/java/com/familylogbook/app/data/smarthome/SmartHomeManager.kt`

**Akcije:**

1. **Dodati jasne CTA-ove:**
   - U `LogEntryCard` za SMART_HOME kategoriju dodati gumb "Pošalji komandu"
   - U `AddEntryScreen` kada je kategorija SMART_HOME, prikazati hint: "Npr. 'Upali svjetla u dnevnom boravku'"

2. **Dodati fallback handling:**
   ```kotlin
   try {
       smartHomeManager.sendCommand(command)
   } catch (e: Exception) {
       // Prikazati friendly poruku
       when {
           e.message?.contains("Google Assistant") == true -> 
               "Google Assistant nije dostupan. Provjeri da li je instaliran."
           else -> 
               "Ne mogu poslati komandu. Provjeri da li je pametna kuća povezana."
       }
   }
   ```

3. **Testirati na realnom uređaju:**
   - Google Assistant instaliran
   - Bez Google Assistanta
   - Različite komande (svjetla, klima, temperatura)

**Vrijeme:** 2h

---

### [ ] (F) Onboarding Flow
**Fajlovi:**
- Kreirati `app/src/main/java/com/familylogbook/app/ui/screen/OnboardingScreen.kt`
- `app/src/main/java/com/familylogbook/app/MainActivity.kt`

**Akcije:**

1. **Kreirati OnboardingScreen:**
   - Page 1: Dobrodošli u FamilyOS
   - Page 2: Dodaj prvo dijete/osobu
   - Page 3: Dodaj prvi entitet (opcionalno)
   - Page 4: Gotovo - kreni koristiti

2. **Logika:**
   - Provjeriti da li korisnik ima barem jednu osobu
   - Ako nema, prikazati onboarding
   - Spremiti u SharedPreferences da je onboarding završen

3. **Integracija:**
   - U MainActivity, provjeriti da li je onboarding završen
   - Ako nije, prikazati OnboardingScreen umjesto HomeScreen

**Vrijeme:** 4-5h

---

### [ ] (G) Today Summary na Home Screen
**Fajl:** `app/src/main/java/com/familylogbook/app/ui/screen/HomeScreen.kt`

**Akcije:**

1. **Dodati TodaySummaryCard:**
   ```kotlin
   @Composable
   fun TodaySummaryCard(entries: List<LogEntry>) {
       val todayEntries = entries.filter { /* danas */ }
       val sleepHours = calculateSleepHours(todayEntries)
       val feedingCount = todayEntries.count { it.category == Category.FEEDING }
       val temperatureCount = todayEntries.count { it.temperature != null }
       
       Card {
           Text("Današnji pregled")
           Text("Spavanje: $sleepHours sati")
           Text("Hranjenja: $feedingCount")
           Text("Temperature: $temperatureCount")
       }
   }
   ```

2. **Pozicionirati:**
   - Na vrhu HomeScreen-a, ispod filtera
   - Ili kao floating card

**Vrijeme:** 2-3h

---

### [ ] (H) Error Handling Poboljšanja
**Fajlovi:**
- Svi ekrani s network/Firestore pozivima

**Akcije:**

1. **Dodati error handling:**
   - Network errors → prikazati friendly poruku
   - Firestore errors → prikazati friendly poruku
   - Offline mode → prikazati indikator

2. **Retry logika:**
   - Dodati "Pokušaj ponovo" gumb na error porukama

3. **Loading states:**
   - Prikazati loading indikatore dok se podaci učitavaju

**Vrijeme:** 3-4h

---

## 📋 Checklist za Finalni Release

### Pre-Build
- [ ] Svi TODO-iji iz PRIORITET 1 završeni
- [ ] Testirano na realnom Android uređaju
- [ ] Testirano na različitim Android verzijama (min API 24)
- [ ] Provjereno da sve notifikacije rade
- [ ] Export/Import testiran

### Build
- [ ] Release build kreiran
- [ ] ProGuard rules provjerene
- [ ] App signing konfiguriran
- [ ] Version code i version name ažurirani

### Pre-Release
- [ ] README.md ažuriran
- [ ] Screenshot-i dodani
- [ ] Privacy Policy napisan (ako treba)
- [ ] Terms of Service napisan (ako treba)

### Post-Release
- [ ] Monitorirati crash reports
- [ ] Prikupiti user feedback
- [ ] Pripremiti v1.1 roadmap

---

## 📝 Notes

- Sve akcije su konkretne i izvedive
- Vrijeme je procijenjeno za jednog developera
- Mogu se raditi paralelno gdje je moguće
- Prioritete prilagoditi prema potrebi

**Zadnje ažuriranje:** 4.12.2025

