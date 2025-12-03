# Firestore Security Rules - Upute za postavljanje

## Problem

Ako vidiš grešku u logovima:
```
PERMISSION_DENIED: Missing or insufficient permissions.
```

To znači da Firestore security rules nisu pravilno postavljene.

## Rješenje

### Korak 1: Otvori Firebase Console

1. Idi na [Firebase Console](https://console.firebase.google.com/)
2. Odaberi svoj projekt "Family Logbook"
3. U lijevom meniju, klikni na **"Firestore Database"**
4. Klikni na tab **"Rules"** (gore u toolbaru)

### Korak 2: Kopiraj Security Rules

Kopiraj sljedeće pravila u Firebase Console Rules editor:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users mogu čitati/pisati samo svoje podatke
    match /users/{userId} {
      // Korisnik mora biti autentificiran i mora biti vlasnik svojih podataka
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Persons collection
      match /persons/{personId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Entities collection
      match /entities/{entityId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Entries collection
      match /entries/{entryId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Legacy: Children collection (backward compatibility)
      match /children/{childId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### Korak 3: Publish Rules

1. Klikni **"Publish"** gumb (gore desno)
2. Potvrdi da želiš objaviti pravila

### Korak 4: Provjeri

1. Restartaj aplikaciju
2. Greška `PERMISSION_DENIED` bi trebala nestati
3. Ako i dalje vidiš grešku, provjeri:
   - Da li je korisnik autentificiran (provjeri u Settings screenu)
   - Da li je `userId` u Firestore path-u jednak `request.auth.uid`

## Što ova pravila rade?

- ✅ **Samo autentificirani korisnici** mogu pristupiti podacima
- ✅ **Korisnik može čitati/pisati samo svoje podatke** (`users/{userId}/...`)
- ✅ **Korisnik ne može pristupiti tuđim podacima** (drugi `userId`)
- ✅ **Podržava sve kolekcije**: persons, entities, entries, children (legacy)

## Test Mode (NE KORISTI U PRODUCTION!)

Ako vidiš "test mode" u Firebase Console, to znači da su pravila:
```javascript
allow read, write: if true; // DOZVOLJAVA SVE - NESIGURNO!
```

**Ovo je OK za development/testiranje**, ali **NE za production!**

Kada završiš testiranje, **OBVEZNO** promijeni na gore navedena pravila.

---

**Kada postaviš pravila, restartaj aplikaciju i provjeri da li greška nestaje!** ✅

