# 📄 Legal Documents Checklist za Play Store

## ✅ Što Treba za Google Play Store

### Obavezno
- [x] **Privacy Policy** - ✅ Kreirano (`PRIVACY_POLICY.md`)
- [ ] **Privacy Policy URL** - Treba biti dostupan online (GitHub Pages, web stranica, itd.)
- [ ] **Data Safety Form** - Ispuniti u Google Play Console

### Preporučeno
- [x] **Terms of Service** - ✅ Kreirano (`TERMS_OF_SERVICE.md`)
- [x] **GDPR Compliance** - ✅ Dokumentirano (`GDPR_COMPLIANCE.md`)
- [ ] **Contact Email** - Treba dodati pravi email u dokumente

## 🔗 Kako Objaviti Privacy Policy Online

### Opcija 1: GitHub Pages (Besplatno)
1. Kreiraj `docs/` folder u repo-u
2. Kopiraj `PRIVACY_POLICY.md` u `docs/PRIVACY_POLICY.md`
3. Uključi GitHub Pages u Settings → Pages
4. URL će biti: `https://[tvoj-username].github.io/[repo-name]/PRIVACY_POLICY.html`

### Opcija 2: Web Stranica
- Upload na svoju web stranicu
- Npr: `https://tvoja-stranica.com/privacy-policy`

### Opcija 3: Google Sites (Besplatno)
- Kreiraj Google Site
- Upload markdown ili kopiraj tekst
- Dobit ćeš URL poput: `https://sites.google.com/view/[naziv]/privacy-policy`

## 📋 Data Safety Form u Play Console

Google Play traži da ispuniš formu o podacima. Evo što trebaš odgovoriti:

### Što Prikupljamo
- ✅ **Osobni podaci**: 
  - Ime (osobe/djeca) - opcionalno, korisnik unosi
  - Datum rođenja - opcionalno, korisnik unosi
  - Email - opcionalno (ako se prijavi)
  
- ✅ **Zdravstveni podaci**:
  - Temperatura
  - Lijekovi
  - Simptomi
  
- ✅ **Financijski podaci**:
  - Iznosi transakcija
  - Valuta

### Kako Koristimo
- ✅ **Funkcionalnost aplikacije** - da, za osnovne funkcije
- ❌ **Marketing** - ne
- ❌ **Reklame** - ne
- ❌ **Dijeljenje s trećim stranama** - ne (osim Firebase za storage)

### Gdje Spremamo
- ✅ **Cloud storage** - da (Google Firebase Firestore)
- ✅ **Enkripcija** - da (Firestore koristi enkripciju)

### Treće Stranke
- ✅ **Google Firebase** - da (za storage i authentication)
- ✅ **Google Assistant** - da (ako koristiš Smart Home, opcionalno)

### Prava Korisnika
- ✅ **Pristup podacima** - da (Export funkcionalnost)
- ✅ **Brisanje podataka** - da (Delete all data u Settings)
- ✅ **Prenosljivost podataka** - da (JSON export)

## 📝 Što Treba Još Učiniti

### 1. Ažurirati Email u Dokumentima
U `PRIVACY_POLICY.md`, `TERMS_OF_SERVICE.md`, i `GDPR_COMPLIANCE.md`:
- Zamijeni `[Tvoj email ovdje]` sa pravim emailom
- Ili stvori dedicated email: `privacy@tvoja-domena.com`

### 2. Objaviti Privacy Policy Online
- Izaberi metodu (GitHub Pages, web stranica, Google Sites)
- Upload Privacy Policy
- Dobit ćeš URL koji možeš staviti u Play Console

### 3. Ispuniti Data Safety Form
- U Google Play Console → Data Safety
- Odgovori na sva pitanja (koristi informacije iz ovog checklista)
- Dodaj Privacy Policy URL

### 4. (Opcionalno) Pravna Provjera
- Za beta testiranje: nije potrebno
- Za javni release: preporučujemo provjeru s odvjetnikom

## 🔍 Quick Checklist

### Prije Beta Releasea
- [ ] Privacy Policy napisan ✅
- [ ] Privacy Policy online (URL)
- [ ] Email dodan u dokumente
- [ ] Terms of Service napisan ✅

### Prije Public Releasea
- [ ] Privacy Policy online i dostupan
- [ ] Data Safety Form ispunjen u Play Console
- [ ] Terms of Service online (opcionalno)
- [ ] Pravna provjera (preporučeno)
- [ ] GDPR compliance dokumentiran ✅

## 📧 Kontakt Info za Dokumente

Trenutno u dokumentima:
- Email: `[Tvoj email ovdje]` ⚠️ **TREBA ZAMIJENITI**
- GitHub: `[Link na repo ako imaš]` ⚠️ **TREBA ZAMIJENITI**

**Akcija**: Ažuriraj ove informacije prije objave!

---

**Status**: Beta dokumentacija - spremno za testiranje, treba online URL prije Play Store submissiona.

