# 🔥 Firebase Setup - KRITIČNO!

## ⚠️ PERMISSION_DENIED Greške

Ako vidiš `PERMISSION_DENIED: Missing or insufficient permissions` greške u logovima, to znači da **Firestore Security Rules nisu deployani** u Firebase Console.

## 📋 Koraci za Deploy Security Rules

### 1. Otvori Firebase Console
- Idi na [Firebase Console](https://console.firebase.google.com)
- Odaberi svoj projekt

### 2. Deploy Security Rules
- Idi na **Firestore Database** → **Rules** tab
- Kopiraj SAV sadržaj iz `firestore.rules` fajla u ovom projektu
- Klikni **Publish** da deployaš pravila

### 3. Provjeri da li su pravila aktivna
- Nakon deploya, pravila bi trebala biti aktivna u roku od nekoliko sekundi
- Ako i dalje vidiš PERMISSION_DENIED greške:
  - Provjeri da li je korisnik autentificiran (anonimni login)
  - Provjeri da li su pravila točno kopirana (bez grešaka u sintaksi)

## 🔍 Provjera da li su pravila deployana

U Firebase Console → Firestore Database → Rules, trebao bi vidjeti:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if authenticated user owns this user document
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Users collection structure: /users/{userId}/...
    match /users/{userId} {
      allow read, write: if isOwner(userId);
      
      // Persons subcollection: /users/{userId}/persons/{personId}
      match /persons/{personId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      // Entities subcollection: /users/{userId}/entities/{entityId}
      match /entities/{entityId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      // Entries subcollection: /users/{userId}/entries/{entryId}
      match /entries/{entryId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      // Legacy: Children subcollection (backward compatibility)
      match /children/{childId} {
        allow read, write: if isOwner(userId);
        allow list: if isOwner(userId);
      }
      
      // Catch-all for any other subcollections
      match /{document=**} {
        allow read, write: if isOwner(userId);
      }
    }
    
    // Default deny rule for any other paths
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

2. **Provjeri Firebase projekt:**
   - Provjeri da li je `google-services.json` u `app/` folderu
   - Provjeri da li je `applicationId` u `build.gradle.kts` isti kao u Firebase projektu

3. **Testiraj pravila:**
   - U Firebase Console → Firestore Database → Rules
   - Klikni **Rules Playground** da testiraš pravila

## 📝 Napomena

Security rules su **kritične** za sigurnost aplikacije. Bez njih, korisnici ne mogu pristupiti svojim podacima, što uzrokuje PERMISSION_DENIED greške.

**Nakon svakog deploya security rules, restartaj aplikaciju!**



