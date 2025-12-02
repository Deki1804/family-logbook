# GitHub - Sljedeći Koraci 🚀

Tvoj repozitorij je kreiran! Sada trebaš pushati lokalni kod na GitHub.

---

## 📋 Korak 1: Otvori Terminal u Projektu

1. Otvori **PowerShell** ili **Command Prompt**
2. Navigiraj do projekta:
   ```powershell
   cd "F:\Projekti\Family Logbook"
   ```

---

## 🔍 Korak 2: Provjeri Git Status

Provjeri da li je Git već inicijaliziran:

```powershell
git status
```

**Ako vidiš grešku "not a git repository"**:
- Git nije inicijaliziran → idi na Korak 3

**Ako vidiš nešto poput "On branch main" ili "On branch master"**:
- Git je već inicijaliziran → idi na Korak 4

---

## 🔧 Korak 3: Inicijaliziraj Git (ako nije)

```powershell
# Inicijaliziraj Git
git init

# Promijeni branch u "main" (ako je "master")
git branch -M main
```

---

## 📦 Korak 4: Dodaj Sve Fajlove

```powershell
# Dodaj sve fajlove
git add .

# Provjeri što će biti commitano
git status
```

Trebao bi vidjeti sve fajlove koje će biti dodane (README.md, app/, build.gradle.kts, itd.)

---

## 💾 Korak 5: Napravi Prvi Commit

```powershell
git commit -m "Initial commit: Family Logbook MVP - Phase 1"
```

**Alternativne commit poruke**:
- `"feat: initial commit - Family Logbook MVP"`
- `"🎉 Initial commit: Family Logbook Phase 1"`

---

## 🔗 Korak 6: Poveži s GitHub-om

Koristi URL koji vidiš na GitHub stranici (u tvom slučaju):
```powershell
git remote add origin https://github.com/Deki1804/family-logbook.git
```

**Provjeri da je remote dodan**:
```powershell
git remote -v
```

Trebao bi vidjeti:
```
origin  https://github.com/Deki1804/family-logbook.git (fetch)
origin  https://github.com/Deki1804/family-logbook.git (push)
```

---

## 🚀 Korak 7: Pushaj na GitHub

```powershell
git push -u origin main
```

**Ako te pita za autentifikaciju**:
- **Username**: `Deki1804` (tvoj GitHub username)
- **Password**: **Personal Access Token** (ne tvoj GitHub password!)

### Ako nemaš Personal Access Token:

1. Idi na GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Klikni **"Generate new token"** → **"Generate new token (classic)"**
3. Unesi **Note**: `Family Logbook Development`
4. Odaberi **Expiration**: `90 days` ili `No expiration`
5. Odaberi **Scopes**: ✅ `repo` (sve pod-opcije)
6. Klikni **"Generate token"**
7. **KOPIRAJ TOKEN ODMAH** (nećeš ga moći vidjeti ponovno!)
8. Koristi token kao password pri push-u

---

## ✅ Korak 8: Provjeri na GitHub-u

1. Osvježi GitHub stranicu (`https://github.com/Deki1804/family-logbook`)
2. Trebao bi vidjeti sve fajlove:
   - ✅ README.md
   - ✅ app/
   - ✅ build.gradle.kts
   - ✅ settings.gradle.kts
   - ✅ itd.

3. Klikni na **README.md** - trebao bi se prikazati s markdown formatiranjem

---

## 🎯 Kompletna Sekvenca Komandi

Evo svega u jednom bloku (kopiraj i zalijepi):

```powershell
# Navigiraj do projekta
cd "F:\Projekti\Family Logbook"

# Inicijaliziraj Git (ako nije već)
git init
git branch -M main

# Dodaj sve fajlove
git add .

# Commit
git commit -m "Initial commit: Family Logbook MVP - Phase 1"

# Poveži s GitHub-om
git remote add origin https://github.com/Deki1804/family-logbook.git

# Pushaj
git push -u origin main
```

---

## 🆘 Troubleshooting

### Problem: "remote origin already exists"

```powershell
# Provjeri postojeći remote
git remote -v

# Ako treba promijeniti URL
git remote set-url origin https://github.com/Deki1804/family-logbook.git
```

### Problem: "Authentication failed"

- Provjeri da koristiš **Personal Access Token**, ne password
- Provjeri da token ima `repo` scope
- Provjeri da token nije istekao

### Problem: "failed to push some refs"

Ako GitHub ima README ili drugi fajl koji lokalno nemaš:

```powershell
git pull origin main --allow-unrelated-histories
# Riješi konflikte ako ih ima
git push -u origin main
```

### Problem: "large files" greška

Ako imaš velike fajlove:
- Provjeri `.gitignore` - možda treba dodati pattern
- Ako si već commitao, možda trebaš koristiti [Git LFS](https://git-lfs.github.com/)

---

## 🎉 Gotovo!

Kada vidiš sve fajlove na GitHub-u, uspješno si pushao projekt! 

**Sljedeći korak**: Firebase setup! 🔥

Javi mi kada završiš pa ćemo nastaviti s Firebase-om.

