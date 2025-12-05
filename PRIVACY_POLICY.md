# Pravila Privatnosti - FamilyOS

**Posljednja izmjena:** 5. prosinca 2025.

## 📋 Uvod

FamilyOS ("mi", "naš", "aplikacija") poštuje tvoju privatnost. Ovaj dokument objašnjava kako prikupljamo, koristimo i zaštitimo tvoje podatke kada koristiš našu aplikaciju.

## 🔒 Što Prikupljamo

### Podaci koje uneseš
Aplikacija sprema sljedeće podatke koje uneseš:

- **Osobe**: Ime, datum rođenja, tip osobe (roditelj, dijete, kućni ljubimac), emoji
- **Entiteti**: Ime entiteta (auto, kuća, financije), tip, emoji
- **Zapisi**: Tekst zapisa, kategorija, datum i vrijeme, tagovi, raspoloženje
- **Zdravlje**: Temperatura, lijekovi, simptomi, podsjetnici za lijekove
- **Hranjenje**: Tip hranjenja, količina, trajanje
- **Financije**: Iznosi, valuta, opisi transakcija
- **Servisi**: Tip servisa, kilometraža (za auto), datumi podsjetnika

### Automatski prikupljeni podaci
- **Firebase Authentication**: Anonimni User ID (automatski generiran) ili email adresa (ako se prijaviš)
- **Firestore Database**: Svi podaci koje uneseš spremaju se u Google Firestore (cloud baza podataka)
- **Lokalni podaci**: SharedPreferences za postavke aplikacije (npr. onboarding status)

### Dozvole koje tražimo
- **Notifikacije** (Android 13+): Za podsjetnike o lijekovima, servisima i hranjenju
- **Vibracija**: Za notifikacije
- **Točni alarmi**: Za podsjetnike na točno vrijeme

### Što NE prikupljamo
- ❌ Lokaciju
- ❌ Kontakte
- ❌ Galeriju/slike
- ❌ Tracking/analytics podatke (osim Firebase Analytics ako je uključen)
- ❌ Podatke s drugih aplikacija

## 📍 Gdje Spremamo Podatke

Svi tvoji podaci spremaju se u **Google Firebase Firestore**, što znači:

- Podaci se spremaju u cloud (internet)
- Podaci su povezani s tvojim User ID (anonimni ili email)
- Podaci su zaštićeni Firebase Security Rules
- Podaci se ne dijele s drugim korisnicima

**Lokacija podataka**: Europa (europe-west region), prema Firebase postavkama.

## 🎯 Kako Koristimo Podatke

Koristimo tvoje podatke isključivo za:

1. **Funkcionalnost aplikacije**: Prikazivanje zapisa, statistika, podsjetnika
2. **Sinkronizacija**: Sinkronizacija podataka između uređaja (ako se prijaviš)
3. **Podsjetnici**: Slanje notifikacija za lijekove, servise, hranjenje
4. **Backup**: Automatski backup podataka u cloud-u

**Ne prodajemo, ne dijelimo niti ne koristimo tvoje podatke za reklame.**

## 🔐 Zaštita Podataka

- **Firebase Security Rules**: Samo ti imaš pristup svojim podacima
- **Anonimni login**: Možeš koristiti app bez email adrese
- **Enkripcija**: Firestore koristi enkripciju u tranzitu i u mirovanju
- **Lokalni backup**: Možeš eksportirati sve svoje podatke u JSON/CSV

## 🗑️ Prava Korisnika (GDPR)

Prema GDPR-u, imaš pravo:

1. **Pristup podacima**: Možeš eksportirati sve svoje podatke (Settings → Export)
2. **Ispravak podataka**: Možeš editirati ili obrisati bilo koji zapis u aplikaciji
3. **Brisanje podataka**: 
   - Možeš obrisati sve podatke u aplikaciji (Settings → Advanced → Delete all data)
   - Možeš obrisati Firebase account u Firebase Console
4. **Prenosljivost podataka**: Možeš eksportirati podatke u JSON/CSV format

### Kako obrisati sve podatke:
1. U aplikaciji: Settings → Advanced → "Obriši sve moje podatke"
2. Ili u Firebase Console: Projekti → Firestore Database → Obriši sve dokumente u `users/{userId}`

## 🔗 Treće Stranke

Aplikacija koristi sljedeće servise:

- **Google Firebase**: 
  - Firebase Authentication (anonimni login)
  - Firestore Database (spremanje podataka)
  - Firebase Analytics (opcionalno)
  - [Firebase Privacy Policy](https://firebase.google.com/support/privacy)
  
- **Google Assistant**: Ako koristiš Smart Home funkcionalnost, komande se šalju Google Assistantu. Google ima svoja pravila privatnosti.

## 👶 Zaštita Djece

Ako unosiš podatke o djeci:
- Ti si odgovoran za podatke koje uneseš
- Preporučujemo da ne dijeliš podatke o djeci s trećim stranama
- Podaci su zaštićeni istim mjerama kao i ostali podaci

## 📧 Kontakt

Za pitanja o privatnosti ili zahtjeva za brisanje podataka, kontaktiraj nas:

- **Email**: [Tvoj email ovdje]
- **GitHub**: [Link na repo ako imaš]

## 📝 Izmjene

Možemo povremeno ažurirati ovu Politiku privatnosti. O značajnim promjenama ćemo te obavijestiti kroz aplikaciju ili email.

**Verzija**: 1.0  
**Datum**: 5. prosinca 2025.

---

**Napomena**: Ova aplikacija pruža generalne informacije i preporuke. Ne zamjenjuje profesionalni medicinski savjet. Za zdravstvene probleme, uvijek konzultiraj liječnika.

