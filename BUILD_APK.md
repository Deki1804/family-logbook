# 🚀 Kako Izgraditi Beta APK

## Brzo Rješenje - Debug APK

Za beta testiranje, debug APK je dovoljan i ne zahtijeva signing key!

```bash
# Izgradi debug APK
.\gradlew.bat assembleDebug
```

APK će biti u:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Verzija

- **Version Code**: 100 (beta)
- **Version Name**: "1.0.0-beta.1"

## Distribucija

### Opcija 1: Direktno Dijeljenje
1. Upload `app-debug.apk` na Google Drive / Dropbox
2. Podijeli link
3. Testeri moraju omogućiti "Install from unknown sources"

**Kako omogućiti na Androidu:**
- Settings → Security → "Install from unknown sources"
- Ili Settings → Apps → Special access → Install unknown apps

### Opcija 2: Email
- Pošalji APK direktno kao attachment
- Gmail/Outlook će prikazati upozorenje - to je normalno

## Testiranje Checklist

### Prije Dijeljenja
- [ ] Testirati na svom uređaju prvo
- [ ] Provjeriti da Firebase radi
- [ ] Provjeriti da notifikacije rade (ako je Android 13+)

### Što Testirati
- [ ] App se otvara
- [ ] Onboarding flow radi
- [ ] Dodavanje zapisa radi
- [ ] Podaci se spremaju na Firestore
- [ ] Notifikacije rade

## Poznato

- Debug APK je **potpisan debug key-em** - to je OK za beta
- Ne možeš update-ovati production app s debug APK-om
- Za production treba release build s proper signing key

---

**Napravljeno**: Beta 1.0 - $(Get-Date -Format "dd.MM.yyyy")

