# GDPR Compliance - FamilyOS

Ovaj dokument objašnjava kako FamilyOS ispunjava zahtjeve GDPR-a (General Data Protection Regulation).

## 📋 Što je GDPR?

GDPR je Europska regulativa o zaštiti podataka koja daje korisnicima kontrolu nad svojim osobnim podacima.

## ✅ Kako FamilyOS Ispunjava GDPR

### 1. Pravna Osnova za Obradbu Podataka

**Osnova**: Tvoj pristanak (korištenjem aplikacije) i izvršavanje ugovora (pružanje usluge)

- Koristiš aplikaciju dobrovoljno
- Podaci su potrebni za funkcionalnost aplikacije
- Možeš prestati koristiti aplikaciju i obrisati podatke bilo kada

### 2. Prava Korisnika (Članak 15-22 GDPR)

#### ✅ Pravo na Pristup (Članak 15)
- Možeš eksportirati sve svoje podatke u JSON/CSV format
- Settings → Export & Import → Export to JSON
- Dobit ćeš sve: osobe, entitete, zapise, sve kategorije

#### ✅ Pravo na Ispravak (Članak 16)
- Možeš editirati bilo koji zapis u aplikaciji
- Možeš ažurirati podatke o osobama/entitetima
- Sve izmjene se sinkroniziraju u realnom vremenu

#### ✅ Pravo na Brisanje (Članak 17)
- Možeš obrisati bilo koji zapis
- Možeš obrisati osobe/entitete
- Možeš obrisati SVE podatke: Settings → Advanced → "Obriši sve moje podatke"
- Podaci se brišu iz Firestore baze

#### ✅ Pravo na Ograničenje Obradebe (Članak 18)
- Možeš prestati koristiti aplikaciju - podaci ostaju, ali se ne obrađuju
- Možeš obrisati podatke ako želiš potpuno ograničenje

#### ✅ Pravo na Prenosljivost Podataka (Članak 20)
- Možeš eksportirati sve podatke u standardni JSON format
- JSON sadrži sve strukturirane podatke
- Možeš importirati podatke nazad ili u drugu aplikaciju

#### ✅ Pravo na Prigovor (Članak 21)
- Možeš obrisati podatke ili prestati koristiti aplikaciju bilo kada
- Ne koristimo podatke za marketing ili reklame

### 3. Obaveštenje Korisnika (Članak 13-14)

**Gdje**: Privacy Policy dokument

- Objašnjeno što prikupljamo
- Objašnjeno kako koristimo podatke
- Objašnjeno gdje spremamo podatke
- Objašnjeno tvoja prava

### 4. Sigurnost Podataka (Članak 32)

**Mjere zaštite**:
- ✅ Enkripcija u tranzitu (HTTPS)
- ✅ Enkripcija u mirovanju (Firestore)
- ✅ Firebase Security Rules (samo ti imaš pristup)
- ✅ Anonimni login moguć (minimalni podaci)
- ✅ Lokalni backup moguć

### 5. Povjerenstvo za Zaštitu Podataka (DPO)

Za beta verziju, nije potreban DPO jer:
- Ne obrađujemo podatke na velikoj skali
- Ne radimo profiling ili automatizirano donošenje odluka
- Podaci su samo oni koje korisnik unese

**Za javni release**, razmotriti DPO ako aplikacija naraste.

### 6. Povrede Podataka (Članak 33-34)

**Naša obaveza**: Obavijestiti nadzorne tijela i korisnike u roku od 72 sata ako dođe do povrede podataka.

**Za beta verziju**: Kontaktiraj nas odmah ako primijetiš problem sa sigurnošću.

### 7. Transfer Podataka izvan EU (Članak 44-49)

**Gdje su podaci**:
- Firebase Firestore: Lokacija se bira pri kreiranju (preporuka: `europe-west`)
- Ako koristiš Google Assistant (Smart Home): Podaci se mogu prenijeti u SAD (Google servisi)

**Pravna osnova**: 
- Firebase/Google koriste Standard Contractual Clauses (SCC)
- Google je certificiran prema EU-US Privacy Shield (provjeri aktualni status)

### 8. Minimalni Podaci (Članak 5)

**Princip minimalnih podataka**:
- Prikupljamo samo podatke koje ti uneseš
- Ne tražimo nepotrebne dozvole (nema lokacije, kontakata, galerije)
- Anonimni login moguć (bez emaila)

## 🔧 Tehničke Implementacije

### Firebase Security Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

**Što to znači**: Samo ti (s tvojim User ID) možeš pristupiti svojim podacima.

### Anonimni Login
- Ne zahtijeva email ili druge identifikatore
- Firebase generira anonimni User ID
- Možeš kasnije nadograditi na email/Google account

### Export Funkcionalnost
- JSON format sa svim podacima
- CSV format za pregled u Excel-u
- Sve strukture podataka su uključene

### Delete Funkcionalnost
- Settings → Advanced → "Obriši sve moje podatke"
- Briše sve podatke iz Firestore
- Možeš obrisati i Firebase account (u Firebase Console)

## 📧 Kontakt za GDPR Zahtjeve

Ako želiš:
- Zatražiti pristup podacima
- Zatražiti brisanje podataka
- Podnijeti prigovor
- Postaviti pitanje o GDPR-u

**Kontakt**: LarryDJ@gmail.com

**Rok za odgovor**: Do 30 dana (prema GDPR-u)

## 📝 Checklist za GDPR Compliance

- [x] Privacy Policy napisan i dostupan
- [x] Objašnjeno što prikupljamo
- [x] Objašnjeno kako koristimo podatke
- [x] Objašnjeno gdje spremamo podatke
- [x] Export funkcionalnost (pravo na pristup i prenosljivost)
- [x] Delete funkcionalnost (pravo na brisanje)
- [x] Firebase Security Rules (zaštita podataka)
- [x] Anonimni login moguć (minimalni podaci)
- [ ] DPO (nije potreban za beta)
- [ ] Data Protection Impact Assessment (nije potreban za beta)

## 🚀 Za Budućnost

Prije javnog releasea, razmotriti:
1. **Data Protection Impact Assessment (DPIA)** - ako aplikacija naraste
2. **Dedicated DPO** - ako naraste na više od 250 zaposlenika ili veliki obim podataka
3. **Privacy by Design** - nastaviti s minimalnim podacima
4. **User Consent Management** - jasno prikazati što se dešava s podacima

---

**Verzija**: 1.0 (Beta)  
**Datum**: 5. prosinca 2025.

**Napomena**: Ovo je osnovna GDPR compliance za beta verziju. Prije javnog releasea, preporučujemo pravnu provjeru s odvjetnikom specijaliziranim za GDPR.

