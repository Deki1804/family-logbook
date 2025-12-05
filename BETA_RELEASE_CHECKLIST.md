# 🚀 Beta Release Checklist - FamilyOS

## ✅ Što je Gotovo

- ✅ PRIORITET 1 (A-D): Sve kritične stvari
  - (A) README i Branding
  - (B) Auth & Login Flow  
  - (C) Notifikacije (Android 13+)
  - (D) Export/Import – Proširiti Backup

- ✅ PRIORITET 2 (E-H): UX Poboljšanja
  - (E) Smart Home UX Poboljšanja
  - (F) Onboarding Flow
  - (G) Today Summary na Home Screen
  - (H) Error Handling Poboljšanja

## 📋 Pre-Build Checklist

### Firebase & Backend
- [x] `google-services.json` postoji
- [ ] Firestore Security Rules provjerene
- [ ] Firebase Auth anonimni login radi
- [ ] Testirano da podaci se syncaju na Firestore

### Core Funkcionalnosti
- [ ] Testirano na realnom Android uređaju (min API 24)
- [ ] Notifikacije rade (lijekovi, servisi, hranjenje)
- [ ] Export/Import testiran (JSON)
- [ ] Onboarding flow radi kroz cijeli flow
- [ ] Smart Home komande rade (ako ima Google Assistant)

### UI/UX
- [x] Sve prevedeno na hrvatski
- [ ] Sve ekrane testirane
- [ ] Error handling radi (network errors, etc.)
- [ ] Loading states prikazuju se ispravno

### Build Konfiguracija
- [ ] Version code i version name ažurirani
- [ ] Release signing key (ili debug za beta)
- [ ] ProGuard (opcionalno za beta - može ostati `isMinifyEnabled = false`)

## 🏗️ Build Komande

### Debug APK (za brzo testiranje)
```bash
.\gradlew.bat assembleDebug
```
APK će biti u: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (za beta distribuciju)
```bash
.\gradlew.bat assembleRelease
```
APK će biti u: `app/build/outputs/apk/release/app-release.apk`

**Napomena**: Release build zahtijeva signing key. Za beta testiranje, možeš koristiti debug APK ili kreirati release key.

## 📦 Distribucija Beta APK-a

### Opcija 1: Direktno dijeljenje
1. Kreiraj debug ili release APK
2. Upload na Google Drive / Dropbox / etc.
3. Pošalji link testerima
4. Testeri moraju omogućiti "Install from unknown sources" na Androidu

### Opcija 2: Firebase App Distribution
1. Upload APK na Firebase Console
2. Dodaj testere po emailu
3. Oni dobiju notifikaciju i mogu instalirati direktno

### Opcija 3: Internal Testing Track (Google Play)
1. Kreiraj Google Play Console account
2. Upload APK na Internal Testing track
3. Dodaj testere
4. Oni instaliraju preko Google Play (lakše)

## 🧪 Što Testirati

### Osnovno
- [ ] App se otvara i radi
- [ ] Firebase auth radi (anonimni login)
- [ ] Podaci se spremaju i učitavaju
- [ ] Navigation između ekrana radi

### Funkcionalnosti
- [ ] Dodavanje zapisa (svi tipovi)
- [ ] Editovanje zapisa
- [ ] Brisanje zapisa
- [ ] Dodavanje osoba/entiteta
- [ ] Onboarding flow
- [ ] Export/Import

### Notifikacije
- [ ] Medicine reminders rade
- [ ] Service reminders rade
- [ ] Feeding reminders rade
- [ ] Notifikacije se prikazuju na vrijeme

### Edge Cases
- [ ] Offline mode (što se događa bez interneta?)
- [ ] Network errors (prijateljske poruke?)
- [ ] Prazni podaci (nema osoba/entiteta)
- [ ] Veliki broj zapisa (performance)

## 📝 Beta Testiranje Plan

### Faza 1: Interno (1-2 tjedna)
- Testiraš sam/a na svom uređaju
- Provjeri sve funkcionalnosti
- Fiksaj kritične bugove

### Faza 2: Mala grupa (5-10 testera)
- Dodaj prijatelje/obitelj
- Prikupi feedback
- Fiksaj glavne probleme

### Faza 3: Šira beta (20+ testera)
- Više ljudi testira
- Različiti uređaji/Android verzije
- Pripremi za public release

## 🐛 Bug Reporting

Testeri trebaju reportirati:
1. **Što su pokušali** - koraci za reprodukciju
2. **Što se dogodilo** - opis problema
3. **Što su očekivali** - očekivano ponašanje
4. **Screenshot/Video** - ako je moguće
5. **Device info** - Android verzija, model telefona

## 🎯 Verzija za Beta

**Trenutna verzija**: 
- Version Code: 1
- Version Name: "1.0"

**Preporuka za beta**:
- Version Code: 100 (ili 10 za prvu beta)
- Version Name: "1.0.0-beta.1"

## 📱 Minimalni Zahtjevi

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Firebase**: Potrebno za punu funkcionalnost
- **Internet**: Potreban za sync (offline mode limited)

---

**Napravljeno**: $(Get-Date -Format "dd.MM.yyyy HH:mm")
**Verzija**: Beta 1.0

