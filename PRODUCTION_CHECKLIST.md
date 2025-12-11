# 🚀 Production Checklist - FamilyOS

## ⚠️ VAŽNO: Prije production release-a, provjeri sve ovo!

### 1. 🔐 API Keys Security

#### Google Custom Search API Key
- **Trenutno**: Restriction = "None" (za testiranje)
- **Prije production-a**: 
  1. Idite u Google Cloud Console → APIs & Services → Credentials
  2. Odaberite API key: `AIzaSyAN7eRinnKIjNO6D44b_3UObIr1r5sJTR0`
  3. Kliknite "Edit"
  4. U "API restrictions" odaberite "Restrict key"
  5. Odaberite samo "Custom Search API"
  6. U "Application restrictions" odaberite "Android apps"
  7. Dodajte package name: `com.familylogbook.app`
  8. Dodajte SHA-1 certificate fingerprint (za release build)
  9. Spremite promjene

**Lokacija**: Google Cloud Console → APIs & Services → Credentials

#### Google Gemini API Key (ako se koristi)
- Provjerite da li je restriction postavljen
- Ograničite na samo potrebne API-je

### 2. 📦 Build Configuration

#### gradle.properties
- **NE commitajte** `gradle.properties` sa API key-jevima u Git!
- Koristite `local.properties` ili environment variables za production
- Ili koristite Gradle secrets plugin

#### Build Variants
- Provjerite da li su API key-ji različiti za debug i release
- Za production, koristite secure storage (Android Keystore System)

### 3. 🔒 Security Best Practices

#### API Keys u Production
- **NE hardcode-ajte** API key-jeve u kod
- Koristite Android Keystore System ili secure storage
- Ili koristite backend server kao proxy (najsigurnije)

#### Google Custom Search API
- Trenutno: Restriction = None (OK za testiranje)
- Production: Restrict na Custom Search API + Android app

### 4. 📱 App Signing

#### Release Keystore
- Provjerite da li postoji release keystore
- Zabilježite SHA-1 fingerprint za API restrictions
- Backup keystore na sigurno mjesto

#### SHA-1 Fingerprint
```bash
# Za release build
keytool -list -v -keystore your-release-keystore.jks -alias your-alias
```

### 5. 🧪 Testing

#### Prije production-a, testirajte:
- [ ] Shopping deals feature radi s restricted API key-jem
- [ ] Google Custom Search API pozivi rade
- [ ] Error handling radi ispravno
- [ ] App ne crash-a ako API key nije postavljen
- [ ] App ne crash-a ako API vrati error

### 6. 📊 Monitoring

#### Google Cloud Console
- Provjerite API usage i quotas
- Postavite alerts za prekoračenje limita
- Monitorirajte error rate

### 7. 📝 Documentation

#### Ažuriraj dokumentaciju:
- [ ] `GOOGLE_CUSTOM_SEARCH_SETUP.md` - dodaj production setup
- [ ] README.md - dodaj production deployment instrukcije
- [ ] API keys management dokumentacija

---

## 🔄 Migration Steps (Testiranje → Production)

### Korak 1: Restrict API Key
1. Google Cloud Console → Credentials
2. Edit API key
3. Restrict na Custom Search API
4. Restrict na Android app (package name + SHA-1)

### Korak 2: Test Restricted Key
1. Build release APK
2. Testiraj shopping deals feature
3. Provjeri da API pozivi rade

### Korak 3: Update gradle.properties
1. Provjeri da API key-ji nisu u Git
2. Koristi secure storage za production

### Korak 4: Deploy
1. Build release APK/AAB
2. Upload na Google Play Console
3. Testiraj na production track

---

## 📌 Current API Key Status

**Google Custom Search API:**
- API Key: `AIzaSyAN7eRinnKIjNO6D44b_3UObIr1r5sJTR0`
- Engine ID: `a567f115f8e6048fc`
- **Restriction**: None (za testiranje) ⚠️ **MORA SE RESTRICTATI PRIJE PRODUCTION-A!**
- **Production**: Treba restrictati prije release-a
- **Lokacija**: Google Cloud Console → APIs & Services → Credentials

**Lokacija u kodu:**
- `gradle.properties`: API key i Engine ID
- `app/build.gradle.kts`: BuildConfig fields
- `AdviceEngine.kt`: Koristi BuildConfig za inicijalizaciju

---

## 🎯 Quick Reference

### API Key Restriction Setup
1. Google Cloud Console → APIs & Services → Credentials
2. Edit API key
3. API restrictions → Restrict key → Custom Search API
4. Application restrictions → Android apps
5. Add package: `com.familylogbook.app`
6. Add SHA-1 fingerprint (release keystore)

### SHA-1 Fingerprint Command
```bash
keytool -list -v -keystore release-keystore.jks -alias your-alias
```

---

**Napomena**: Ova checklista je kreirana [datum]. Ažurirajte je prije svakog production release-a.
