# GitHub Setup - Detaljne Upute 🐙

Vodič za kreiranje i postavljanje GitHub repozitorija za Family Logbook projekt.

---

## 📋 Korak 1: Kreiranje GitHub Repozitorija

### 1.1. Prijava na GitHub

1. Idi na [GitHub.com](https://github.com)
2. Prijavi se sa svojim računom (ili kreiraj novi ako nemaš)

### 1.2. Kreiranje Novog Repozitorija

1. Klikni na **"+"** ikonu u gornjem desnom kutu
2. Odaberi **"New repository"**

### 1.3. Postavke Repozitorija

**Repository name**: 
- Preporučeno: `family-logbook` ili `FamilyLogbook`
- ⚠️ **VAŽNO**: Koristi `-` umjesto razmaka (GitHub preferira kebab-case)
- Alternativa: `familylogbook` (bez crtica)

**Description** (opcionalno):
```
📖 Family Logbook - Android app for parents to track important events about their kids and family life
```

**Visibility**:
- **Private** - preporučeno za početak (možeš promijeniti kasnije)
- **Public** - ako želiš da drugi vide kod

**Initialize repository**:
- ❌ **NE** označavaj "Add a README file" (već imamo README.md)
- ❌ **NE** označavaj "Add .gitignore" (već imamo .gitignore)
- ❌ **NE** označavaj "Choose a license" (možeš dodati kasnije ako želiš)

3. Klikni **"Create repository"**

---

## 🔗 Korak 2: Povezivanje Lokalnog Projekta s GitHub-om

### 2.1. Provjeri da li već postoji Git u projektu

Otvori terminal/command prompt u folderu projekta i provjeri:

```bash
cd "F:\Projekti\Family Logbook"
git status
```

**Ako vidiš grešku "not a git repository"**:
- Git još nije inicijaliziran → idi na Korak 2.2

**Ako vidiš nešto poput "On branch main" ili "On branch master"**:
- Git je već inicijaliziran → idi na Korak 2.3

### 2.2. Inicijaliziraj Git (ako nije već)

```bash
# U folderu projekta
git init

# Provjeri status
git status
```

### 2.3. Dodaj sve fajlove

```bash
# Dodaj sve fajlove u staging
git add .

# Provjeri što će biti commitano
git status
```

### 2.4. Napravi prvi commit

```bash
git commit -m "Initial commit: Family Logbook MVP - Phase 1"
```

**Alternativne commit poruke**:
- `"feat: initial commit - Family Logbook MVP"`
- `"Initial commit: Android app with Jetpack Compose and clean architecture"`
- `"🎉 Initial commit: Family Logbook Phase 1"`

### 2.5. Poveži s GitHub repozitorijem

GitHub će ti pokazati upute nakon kreiranja repozitorija. Evo što trebaš:

```bash
# Dodaj remote origin (zamijeni USERNAME i REPO_NAME)
git remote add origin https://github.com/USERNAME/REPO_NAME.git

# Provjeri da je remote dodan
git remote -v
```

**Primjer**:
```bash
git remote add origin https://github.com/ahmet/family-logbook.git
```

### 2.6. Promijeni glavnu granu u "main" (ako je "master")

```bash
# Provjeri trenutnu granu
git branch

# Ako vidiš "master", promijeni u "main"
git branch -M main
```

### 2.7. Pushaj na GitHub

```bash
# Prvi push
git push -u origin main
```

**Ako vidiš grešku o autentifikaciji**:
- GitHub više ne koristi password, koristi Personal Access Token
- Vidi [Korak 3: Autentifikacija](#korak-3-autentifikacija)

---

## 🔐 Korak 3: Autentifikacija

### 3.1. Personal Access Token (PAT)

GitHub više ne dozvoljava password autentifikaciju. Trebaš Personal Access Token:

1. Idi na GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Klikni **"Generate new token"** → **"Generate new token (classic)"**
3. Unesi **Note**: `Family Logbook Development`
4. Odaberi **Expiration**: `90 days` ili `No expiration` (za development)
5. Odaberi **Scopes**:
   - ✅ `repo` (sve pod-opcije)
6. Klikni **"Generate token"**
7. **KOPIRAJ TOKEN ODMAH** (nećeš ga moći vidjeti ponovno!)

### 3.2. Korištenje Tokena

**Prilikom push-a**, kada te pita za password:
- **Username**: tvoj GitHub username
- **Password**: zalijepi Personal Access Token (ne tvoj GitHub password!)

**Ili koristi Git Credential Manager** (preporučeno):
```bash
# Windows - Git Credential Manager će automatski spremiti token
git push -u origin main
# Kada te pita, unesi token kao password
```

---

## ✅ Korak 4: Provjera

### 4.1. Provjeri na GitHub-u

1. Otvori svoj repozitorij na GitHub.com
2. Provjeri da vidiš sve fajlove:
   - README.md
   - app/
   - build.gradle.kts
   - itd.

### 4.2. Provjeri lokalno

```bash
# Provjeri remote
git remote -v

# Provjeri status
git status

# Provjeri commit history
git log --oneline
```

---

## 📝 Korak 5: Dodatne Postavke (Opcionalno)

### 5.1. Dodaj License

Ako želiš dodati licencu:

1. Na GitHub-u, idi u repozitorij
2. Klikni **"Add file"** → **"Create new file"**
3. Ime fajla: `LICENSE`
4. GitHub će ti ponuditi template - odaberi jedan (npr. MIT License)
5. Commit

**Ili lokalno**:
```bash
# Kreiraj LICENSE fajl
# Zatim:
git add LICENSE
git commit -m "docs: add MIT license"
git push
```

### 5.2. Dodaj Topics (Tags)

Na GitHub repozitoriju:
1. Klikni na ⚙️ (Settings) pored "About"
2. U "Topics" dodaj:
   - `android`
   - `kotlin`
   - `jetpack-compose`
   - `firebase`
   - `family-logbook`
   - `mvp`

### 5.3. Dodaj Description i Website

U "About" sekciji repozitorija:
- **Description**: `📖 Android app for parents to track family events`
- **Website** (opcionalno): ako imaš deployed verziju

---

## 🔄 Korak 6: Rad s Git-om (Best Practices)

### 6.1. Commit Poruke

Koristi konvencionalne commit poruke:

```bash
# Feature
git commit -m "feat: add Firestore repository implementation"

# Bug fix
git commit -m "fix: resolve navigation issue in AddEntry screen"

# Refactoring
git commit -m "refactor: improve EntryClassifier keyword matching"

# Documentation
git commit -m "docs: update README with Firebase setup instructions"

# Style/Formatting
git commit -m "style: format code according to Kotlin conventions"
```

### 6.2. Česti Git Komandi

```bash
# Provjeri status
git status

# Dodaj sve promjene
git add .

# Dodaj specifičan fajl
git add app/src/main/java/com/familylogbook/app/MainActivity.kt

# Commit
git commit -m "tvoja poruka"

# Push na GitHub
git push

# Pull s GitHub-a (ako radiš na više računala)
git pull

# Provjeri razlike
git diff

# Povijest commitova
git log --oneline --graph
```

### 6.3. Branching (Za kasnije)

```bash
# Kreiraj novi branch
git checkout -b feature/firestore-integration

# Promijeni branch
git checkout main

# Merge branch u main
git checkout main
git merge feature/firestore-integration
```

---

## 🆘 Troubleshooting

### Problem: "remote origin already exists"

```bash
# Provjeri postojeće remote
git remote -v

# Ako treba promijeniti URL
git remote set-url origin https://github.com/USERNAME/REPO_NAME.git

# Ili obriši i dodaj ponovno
git remote remove origin
git remote add origin https://github.com/USERNAME/REPO_NAME.git
```

### Problem: "Authentication failed"

- Provjeri da koristiš Personal Access Token, ne password
- Provjeri da token ima `repo` scope
- Provjeri da token nije istekao

### Problem: "failed to push some refs"

```bash
# Ako GitHub ima README ili drugi fajl koji lokalno nemaš
git pull origin main --allow-unrelated-histories
# Riješi konflikte ako ih ima
git push -u origin main
```

### Problem: "large files" greška

Ako imaš velike fajlove koje ne želiš commitati:
- Provjeri `.gitignore` - možda treba dodati pattern
- Ako si već commitao, vidi [Git LFS](https://git-lfs.github.com/) ili obriši iz historije

---

## 📚 Korisni Linkovi

- [GitHub Docs](https://docs.github.com/)
- [Git SCM Dokumentacija](https://git-scm.com/doc)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub CLI](https://cli.github.com/) (alternativa za command line)

---

## ✅ Checklist

- [ ] GitHub račun kreiran/prijavljen
- [ ] Novi repozitorij kreiran (`family-logbook` ili slično)
- [ ] Repozitorij postavljen kao Private/Public
- [ ] Git inicijaliziran lokalno (`git init`)
- [ ] Svi fajlovi dodani (`git add .`)
- [ ] Prvi commit napravljen
- [ ] Remote origin dodan
- [ ] Personal Access Token kreiran (ako treba)
- [ ] Prvi push uspješan (`git push -u origin main`)
- [ ] Repozitorij vidljiv na GitHub.com sa svim fajlovima
- [ ] Topics dodani (opcionalno)
- [ ] License dodana (opcionalno)

---

**Kada završiš sve korake, javi mi pa ćemo nastaviti s Firebase setupom!** 🚀

