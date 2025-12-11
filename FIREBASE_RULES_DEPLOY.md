# 🔥 Firestore Security Rules - DEPLOY INSTRUKCIJE

## ⚠️ KRITIČNO: PERMISSION_DENIED Greške

Ako vidiš u logovima:
```
Error loading entries: PERMISSION_DENIED: Missing or insufficient permissions.
Error loading persons: PERMISSION_DENIED: Missing or insufficient permissions.
```

**To znači da Firestore Security Rules NISU deployani u Firebase Console!**

## 📋 Koraci za Deploy Security Rules

### 1. Otvori Firebase Console
- Idi na [Firebase Console](https://console.firebase.google.com)
- Odaberi svoj projekt **Family Logbook**

### 2. Deploy Security Rules
- Idi na **Firestore Database** → **Rules** tab
- Kopiraj SAV sadržaj iz `firestore.rules` fajla u ovom projektu
- Klikni **Publish** da deployaš pravila

### 3. Provjeri da li su pravila aktivna
- Nakon deploya, pravila bi trebala biti aktivna u roku od nekoliko sekundi
- Ako i dalje vidiš PERMISSION_DENIED greške:
  - Provjeri da li je korisnik autentificiran (vidljivo u logovima: `currentUser: ...`)
  - Provjeri da li su pravila točno kopirana (bez grešaka u sintaksi)
  - Provjeri da li je `request.auth.uid` jednak `userId` u path-u

## 🔍 Provjera da li su pravila deployana

U Firebase Console → Firestore Database → Rules, trebao bi vidjeti:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    match /users/{userId} {
      allow read, write: if isOwner(userId);
      
      match /persons/{personId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      match /entities/{entityId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      match /entries/{entryId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      match /children/{childId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      match /{document=**} {
        allow read, write: if isOwner(userId);
      }
    }
    
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

## 🚨 Ako i dalje imaš probleme

1. **Provjeri autentifikaciju:**
   - Aplikacija automatski kreira anonimni login pri prvom pokretanju
   - Ako vidiš `User not authenticated` u logovima, problem je u AuthManager-u
   - Provjeri da li je `auth.currentUser` null

2. **Provjeri Firebase projekt:**
   - Provjeri da li je `google-services.json` u `app/` folderu
   - Provjeri da li je projekt ID točan

3. **Provjeri Firestore Database:**
   - Provjeri da li je Firestore Database kreiran u Firebase Console
   - Provjeri da li je lokacija postavljena (preporuka: `europe-west`)

4. **Test pravila:**
   - U Firebase Console → Firestore Database → Rules
   - Klikni **Rules Playground** da testiraš pravila
   - Testiraj sa svojim User ID-om

## 📝 Struktura podataka u Firestore

Aplikacija koristi sljedeću strukturu:
```
/users/{userId}/
  /persons/{personId}
  /entities/{entityId}
  /entries/{entryId}
  /children/{childId}  (legacy, backward compatibility)
```

Gdje je `{userId}` = `auth.currentUser.uid`

## ✅ Nakon deploya

Nakon što deployaš pravila:
1. Restart aplikacije
2. Provjeri logove - ne bi trebalo biti PERMISSION_DENIED grešaka
3. Pokušaj dodati novi entry - trebao bi se spremiti
4. Provjeri HomeScreen - trebao bi prikazati podatke
