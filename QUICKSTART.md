# Quick Start Guide 🚀

## Brzi početak (5 minuta)

### 1. Otvori projekt
```
1. Otvori Android Studio
2. File → Open → odaberi folder "Family Logbook"
3. Sačekaj da se Gradle sinkronizira
```

### 2. Postavi SDK (ako treba)
Ako vidiš grešku o `local.properties`:
```
1. Kopiraj local.properties.template u local.properties
2. Postavi svoj SDK path:
   sdk.dir=C\:\\Users\\TvojeIme\\AppData\\Local\\Android\\Sdk
```

### 3. Pokreni aplikaciju
```
1. Poveži Android uređaj (USB debugging) ILI
2. Pokreni emulator (Tools → Device Manager)
3. Klikni Run (▶️) ili Shift+F10
```

### 4. Testiraj funkcionalnosti
- ✅ Pregledaj sample unose na Home ekranu
- ✅ Dodaj novi unos (FAB +)
- ✅ Provjeri Stats ekran
- ✅ Dodaj dijete u Settings

## Git Setup (opcionalno)

Ako želiš koristiti Git:

```bash
# Inicijaliziraj repo
git init

# Dodaj sve fajlove
git add .

# Prvi commit
git commit -m "Initial commit: Family Logbook MVP"

# Dodaj remote (ako imaš)
git remote add origin <your-repo-url>
git push -u origin main
```

## Troubleshooting

**Problem**: Gradle sinkronizacija ne radi
- **Rješenje**: File → Invalidate Caches / Restart

**Problem**: "SDK not found"
- **Rješenje**: Provjeri `local.properties` i SDK path

**Problem**: Aplikacija se ne kompajlira
- **Rješenje**: Provjeri da imaš JDK 17 instaliran

## Što dalje?

- Pročitaj [README.md](README.md) za detalje o arhitekturi
- Provjeri [CONTRIBUTING.md](CONTRIBUTING.md) za development guidelines

