# GitHub Quick Start - TL;DR ⚡

Brzi vodič za one koji već znaju što rade.

## 5-Minutni Setup

### 1. GitHub Repozitorij
- GitHub.com → New repository
- **Name**: `family-logbook` (kebab-case)
- **Visibility**: Private (preporučeno)
- ❌ NE označavaj README/.gitignore/license (već imamo)

### 2. Lokalno

```bash
cd "F:\Projekti\Family Logbook"

# Ako Git nije inicijaliziran
git init
git branch -M main

# Dodaj sve i commitaj
git add .
git commit -m "Initial commit: Family Logbook MVP - Phase 1"

# Poveži s GitHub-om (zamijeni USERNAME i REPO_NAME)
git remote add origin https://github.com/USERNAME/REPO_NAME.git

# Push
git push -u origin main
```

### 3. Autentifikacija

Ako te pita za password:
- **Username**: tvoj GitHub username
- **Password**: Personal Access Token (ne tvoj password!)

**Kreiraj token**: GitHub → Settings → Developer settings → Personal access tokens → Generate new token (classic) → odaberi `repo` scope

## Provjera

- [ ] Repozitorij vidljiv na GitHub.com?
- [ ] Svi fajlovi su tamo?
- [ ] README se prikazuje?

**Za detaljne upute, vidi [GITHUB_SETUP.md](GITHUB_SETUP.md)**

