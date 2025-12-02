# GitHub Setup Checklist ✅

Koristi ovu checklistu dok postavljaš GitHub repozitorij.

## GitHub Repozitorij

- [ ] Prijavljen na GitHub.com
- [ ] Kliknut "+" → "New repository"
- [ ] Repository name unesen: `family-logbook` (ili slično)
- [ ] Description dodana (opcionalno)
- [ ] Visibility odabrana (Private/Public)
- [ ] ❌ NE označeno "Add README" (već imamo)
- [ ] ❌ NE označeno "Add .gitignore" (već imamo)
- [ ] ❌ NE označeno "Choose license" (opcionalno kasnije)
- [ ] Repozitorij kreiran

## Lokalni Git Setup

- [ ] Terminal otvoren u projektu folderu
- [ ] Git inicijaliziran (`git init`)
- [ ] Branch promijenjen u `main` (`git branch -M main`)
- [ ] Svi fajlovi dodani (`git add .`)
- [ ] Prvi commit napravljen
- [ ] Remote origin dodan (`git remote add origin ...`)
- [ ] Remote provjeren (`git remote -v`)

## Autentifikacija

- [ ] Personal Access Token kreiran (GitHub → Settings → Developer settings)
- [ ] Token ima `repo` scope
- [ ] Token kopiran/spremljen (nećeš ga moći vidjeti ponovno!)

## Push na GitHub

- [ ] Prvi push pokrenut (`git push -u origin main`)
- [ ] Autentifikacija uspješna (token korišten kao password)
- [ ] Push uspješan (nema grešaka)

## Provjera

- [ ] Repozitorij vidljiv na GitHub.com
- [ ] Svi fajlovi su tamo (README.md, app/, build.gradle.kts, itd.)
- [ ] README.md se prikazuje ispravno
- [ ] .gitignore je aktivan (ne vidi se build/, local.properties, itd.)

## Opcionalno

- [ ] Topics dodani (android, kotlin, jetpack-compose, itd.)
- [ ] Description dodana u "About" sekciji
- [ ] License dodana (MIT, Apache, itd.)
- [ ] Website link dodan (ako ima deployed verziju)

---

**Kada su svi checkboxi označeni, javi mi pa ćemo nastaviti s Firebase setupom!** 🚀

