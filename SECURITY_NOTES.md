# 🔐 Security Notes - FamilyOS

## ⚠️ VAŽNO: API Keys i Osjetljivi Podaci

### Trenutno Stanje

**Google Custom Search API:**
- API Key: `AIzaSyAN7eRinnKIjNO6D44b_3UObIr1r5sJTR0`
- Engine ID: `a567f115f8e6048fc`
- **Restriction**: None (za testiranje) ⚠️
- **Lokacija**: `gradle.properties`

**Status**: API key je trenutno bez restrictions za lakše testiranje. **MORA SE RESTRICTATI PRIJE PRODUCTION RELEASE-A!**

### Što je u .gitignore

Sljedeći fajlovi se **NE commitaju** u Git:
- `gradle.properties` - sadrži API key-jeve
- `local.properties` - SDK paths
- `app/google-services.json` - Firebase config
- `*.jks`, `*.keystore` - signing keys

### Production Checklist

**Prije production release-a, OBAVEZNO:**

1. **Restrict Google Custom Search API Key**
   - Google Cloud Console → Credentials
   - Edit API key
   - API restrictions → Restrict key → Custom Search API
   - Application restrictions → Android apps
   - Add package: `com.familylogbook.app`
   - Add SHA-1 fingerprint (release keystore)

2. **Provjeri .gitignore**
   - `gradle.properties` mora biti u .gitignore
   - Ne commitaj API key-jeve

3. **Secure Storage (opcionalno, ali preporučeno)**
   - Za production, razmotri korištenje Android Keystore System
   - Ili backend server kao proxy za API pozive

**Vidi**: `PRODUCTION_CHECKLIST.md` za detaljne instrukcije.

### Template Fajlovi

- `gradle.properties.template` - template bez API key-jeva (može se commitati)
- Korisnik kopira template u `gradle.properties` i dodaje svoje key-jeve

### Backup

**VAŽNO**: Backup API key-jeva i Engine ID-jeva na sigurno mjesto (password manager, encrypted storage).

---

**Napomena**: Ova dokumentacija je kreirana za buduće reference. Ažurirajte je kada se promijene API key-ji ili security setup.
