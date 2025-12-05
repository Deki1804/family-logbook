# 🚀 Play Store Setup - Korak po Korak Vodič

Ovaj vodič će te provesti kroz **sve korake** za pripremu aplikacije za Google Play Store.

---

## 📋 Faza 1: Privacy Policy URL (Obavezno!)

### Korak 1.1: Provjeri GitHub Repozitorij

Prvo, provjerimo imaš li GitHub repo:

```powershell
git remote -v
```

**Ako vidiš URL** (npr. `https://github.com/tvoj-username/family-logbook.git`):
- ✅ Imamo GitHub repo! → Idi na Korak 1.2

**Ako ne vidiš ništa**:
- ❌ Treba kreirati GitHub repo → Vidi [GITHUB_SETUP.md](GITHUB_SETUP.md) prvo

---

### Korak 1.2: Kreiraj `docs/` Folder

Kreiramo folder za dokumente koje ćemo objaviti online:

```powershell
# U root folderu projekta
mkdir docs
```

---

### Korak 1.3: Kopiraj Privacy Policy u `docs/`

Kopirajmo Privacy Policy u docs folder:

```powershell
Copy-Item PRIVACY_POLICY.md docs\PRIVACY_POLICY.md
```

---

### Korak 1.4: Pushaj na GitHub

Dodajmo novi folder u Git i pushajmo:

```powershell
git add docs/
git commit -m "docs: Dodaj docs folder za GitHub Pages"
git push
```

---

### Korak 1.5: Uključi GitHub Pages

Sada treba uključiti GitHub Pages u GitHub postavkama:

1. **Otvori GitHub repozitorij** u browseru
2. Idi na **Settings** (gore desno)
3. U lijevom meniju, klikni na **Pages** (pod "Code and automation")
4. Pod **Source**, odaberi:
   - **Branch**: `main`
   - **Folder**: `/docs`
5. Klikni **Save**

**Čekaj 1-2 minute** da se GitHub Pages aktivira.

---

### Korak 1.6: Provjeri Privacy Policy URL

Nakon što se aktivira, tvoj Privacy Policy će biti dostupan na:

```
https://[tvoj-username].github.io/[repo-name]/PRIVACY_POLICY.html
```

**Primjer**:
```
https://deki1804.github.io/family-logbook/PRIVACY_POLICY.html
```

**Provjeri URL** - otvori u browseru i provjeri da se prikazuje!

---

## 📧 Faza 2: Ažuriranje Email Adresa

### Korak 2.1: Odaberi Email

Trebamo email adresu za kontakt u dokumentima. Možeš koristiti:
- Svoj personalni email
- Ili kreirati novi email: `familyos.privacy@gmail.com` (ili slično)

**Koji email želiš koristiti?** 

Za sada ćemo koristiti placeholder, ali **trebat ćeš ažurirati prije Play Store submissiona**.

---

### Korak 2.2: Ažuriraj Privacy Policy

U `PRIVACY_POLICY.md`, pronađi:

```markdown
- **Email**: [Tvoj email ovdje]
```

Zamijeni sa pravim emailom.

---

### Korak 2.3: Ažuriraj Terms of Service

U `TERMS_OF_SERVICE.md`, pronađi:

```markdown
- **Email**: [Tvoj email ovdje]
```

Zamijeni sa istim emailom.

---

### Korak 2.4: Ažuriraj GDPR Compliance

U `GDPR_COMPLIANCE.md`, pronađi:

```markdown
**Kontakt**: [Tvoj email ovdje]
```

Zamijeni sa istim emailom.

---

## 🎯 Faza 3: Google Play Console Setup

### Korak 3.1: Kreiraj Google Play Console Account

1. Idi na [Google Play Console](https://play.google.com/console)
2. Prijavi se sa Google računom
3. Plati jednokratnu naknadu od **$25 USD** (jednokratno, ne godišnje!)
4. Kreiraj Developer Account

---

### Korak 3.2: Kreiraj Aplikaciju

1. U Play Console, klikni **"Create app"**
2. Unesi:
   - **App name**: `FamilyOS` (ili `Family Logbook`)
   - **Default language**: `Hrvatski (Croatia)`
   - **App or game**: `App`
   - **Free or paid**: `Free`
3. Klikni **"Create app"**

---

### Korak 3.3: Dodaj Privacy Policy URL

1. U aplikaciji, idi na **Policy → Privacy policy**
2. U polje za URL, unesi Privacy Policy URL (iz Koraka 1.6):
   ```
   https://tvoj-username.github.io/family-logbook/PRIVACY_POLICY.html
   ```
3. Klikni **Save**

---

### Korak 3.4: Ispuni Data Safety Form

1. Idi na **Policy → Data safety**
2. Odgovori na pitanja koristeći informacije iz `LEGAL_DOCUMENTS_CHECKLIST.md`

**Kratki vodič za Data Safety:**

#### Što prikupljamo?
- ✅ **Osobni podaci**: Ime, datum rođenja (opcionalno)
- ✅ **Zdravstveni podaci**: Temperatura, lijekovi, simptomi
- ✅ **Financijski podaci**: Iznosi, valuta
- ✅ **Email adresa** (opcionalno, ako se prijaviš)

#### Kako koristimo?
- ✅ Za funkcionalnost aplikacije
- ❌ Za marketing/reklame
- ❌ Za dijeljenje s trećim stranama (osim Firebase za storage)

#### Gdje spremamo?
- ✅ Cloud storage (Google Firebase Firestore)
- ✅ Enkripcija (Firestore koristi enkripciju)

#### Treće stranke?
- ✅ Google Firebase (za storage i authentication)

---

## 📝 Faza 4: Upload APK

### Korak 4.1: Kreiraj Release APK

Za Play Store, trebaš **signed release APK**. Za beta testiranje, možeš koristiti debug APK.

**Za produkcioni release**, trebaš:
1. Release signing key
2. Signed APK

Za sada, za **Internal Testing track**, možeš koristiti debug APK.

---

### Korak 4.2: Upload u Play Console

1. U Play Console, idi na **Testing → Internal testing**
2. Klikni **"Create new release"**
3. Upload APK fajl
4. Unesi **Release notes** (npr. "Beta 1.0 - Prva verzija za testiranje")
5. Klikni **"Save"**

---

## ✅ Checklist - Što Trebaš

### Obavezno prije Play Store Submissiona:
- [ ] GitHub repo postoji
- [ ] GitHub Pages uključen (`/docs` folder)
- [ ] Privacy Policy dostupan online (URL radi)
- [ ] Email ažuriran u svim dokumentima
- [ ] Google Play Console account kreiran
- [ ] Privacy Policy URL dodan u Play Console
- [ ] Data Safety form ispunjen

### Prije Public Release:
- [ ] Signed release APK kreiran
- [ ] App screenshots dodani
- [ ] App description napisan (hrvatski)
- [ ] App icon dodan
- [ ] Beta testiranje završeno

---

**Napravljeno**: $(Get-Date -Format "dd.MM.yyyy")  
**Status**: U tijeku - korak po korak setup

