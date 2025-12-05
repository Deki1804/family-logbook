# 🧪 Beta Testing - FamilyOS v1.0.0-beta.1

## 📦 APK Lokacija

**Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`

## 🚀 Kako Instalirati

1. **Preuzmi APK** na svoj Android telefon
2. **Omogući instalaciju**:
   - Settings → Security → "Install from unknown sources"
   - Ili: Settings → Apps → Special access → Install unknown apps
3. **Otvori APK fajl** i instaliraj

## ✅ Što Testirati

### Osnovno
- [ ] App se otvara i radi
- [ ] Onboarding flow (prvi put)
- [ ] Dodavanje osobe/djeteta
- [ ] Dodavanje zapisa (različite kategorije)

### Funkcionalnosti
- [ ] Dodavanje zapisa o zdravlju (temperatura, lijekovi)
- [ ] Dodavanje zapisa o hranjenju
- [ ] Dodavanje zapisa o financijama
- [ ] Dodavanje entiteta (auto, kuća)
- [ ] Editovanje zapisa
- [ ] Brisanje zapisa

### Notifikacije
- [ ] Prikazuje li se zahtjev za dozvole (Android 13+)?
- [ ] Dodaj zapis s lijekom i intervalom - provjeri notifikaciju
- [ ] Dodaj reminder datum - provjeri notifikaciju

### Export/Import
- [ ] Export podataka u JSON
- [ ] Import podataka iz JSON

### Edge Cases
- [ ] Što se događa bez interneta?
- [ ] Error poruke (ugasi internet pa pokušaj spremiti)
- [ ] Prazna lista (nema zapisa još)

## 🐛 Bug Reporting

Ako nađeš bug, molimo prijavi:

1. **Opis problema** - Što se dogodilo?
2. **Koraci za reprodukciju** - Kako doći do problema?
3. **Screenshot** (ako je moguće)
4. **Device info**:
   - Model telefona
   - Android verzija
   - Build number

## 📱 Minimalni Zahtjevi

- **Android**: 7.0+ (API 24)
- **Internet**: Potreban za Firestore sync
- **Firebase**: Konfiguriran (sve radi automatski)

## 💡 Feedback

Javi mi:
- Što ti se sviđa? ❤️
- Što bi poboljšao? 💡
- Ima li nešto što ne radi? 🐛

---

**Verzija**: 1.0.0-beta.1 (Version Code: 100)
**Datum**: $(Get-Date -Format "dd.MM.yyyy")

