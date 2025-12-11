# 📦 Vodič za kreiranje ZIP arhive projekta

## ✅ Provjera da li ZIP sadrži sve potrebne fajlove

Trenutni ZIP (`FamilyLogbook_Project.zip`) bi trebao sadržavati:

### 🔍 Važni fajlovi koji MORAJU biti uključeni:

1. ✅ **Glasovni unos (Speech Recognition)**
   - `app/src/main/java/com/familylogbook/app/data/speech/SpeechRecognizerHelper.kt`

2. ✅ **Cjepiva (Vaccinations)**
   - `app/src/main/java/com/familylogbook/app/domain/vaccination/VaccinationCalendar.kt`

3. ✅ **Shopping Deals Checker**
   - `app/src/main/java/com/familylogbook/app/data/shopping/ShoppingDealsChecker.kt`

4. ✅ **Smart Home Manager**
   - `app/src/main/java/com/familylogbook/app/data/smarthome/SmartHomeManager.kt`

5. ✅ **AndroidManifest.xml**
   - `app/src/main/AndroidManifest.xml`

6. ✅ **Gradle fajlovi**
   - `app/build.gradle.kts`
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`

7. ✅ **Resursi (res/)**
   - `app/src/main/res/` (svi fajlovi)

## 🛠️ Načini kreiranja ZIP-a

### Metoda 1: Python skripta (preporučeno)

```bash
python create_zip_final.py
```

### Metoda 2: PowerShell komanda

```powershell
$files = @()
Get-ChildItem -Path "app\src" -Recurse -File | Where-Object { $_.FullName -notmatch '\\build\\' -and $_.FullName -notmatch '\\.gradle\\' -and $_.Name -ne "google-services.json" } | ForEach-Object { $files += $_.FullName }
Get-ChildItem -Path "." -File | Where-Object { $_.Extension -in @('.kts','.properties','.md','.txt','.rules','.py','.bat','.ps1') -or $_.Name -in @('.gitignore','.gitattributes','gradlew','gradlew.bat') } | ForEach-Object { $files += $_.FullName }
Get-ChildItem -Path "app" -File | Where-Object { $_.Extension -in @('.kts','.pro') -or $_.Name -like '*template*' } | ForEach-Object { $files += $_.FullName }
Get-ChildItem -Path "gradle\wrapper" -File -ErrorAction SilentlyContinue | ForEach-Object { $files += $_.FullName }
$unique = $files | Where-Object { $_ -and (Test-Path $_) } | Select-Object -Unique
Compress-Archive -Path $unique -DestinationPath "FamilyLogbook_Project.zip" -Force
```

### Metoda 3: Windows Explorer (ručno)

1. Otvorite Windows Explorer
2. Idite u `F:\Projekti\Family Logbook`
3. Selektirajte:
   - `app/src` (cijeli direktorij)
   - `app/build.gradle.kts`
   - `app/proguard-rules.pro`
   - `app/google-services.json.template`
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`
   - `gradlew`, `gradlew.bat`
   - `gradle/wrapper/`
   - `firestore.rules`
   - `.gitignore`, `.gitattributes`
   - Svi `.md`, `.txt`, `.py`, `.bat`, `.ps1` fajlovi
4. Desni klik → "Send to" → "Compressed (zipped) folder"
5. Preimenujte u `FamilyLogbook_Project.zip`

## ❌ Što NE uključivati:

- `app/google-services.json` (osjetljivi podaci)
- `local.properties` (osjetljivi podaci)
- `build/` direktorij
- `.gradle/` direktorij
- `.idea/` direktorij
- `*.apk`, `*.aab` fajlovi
- `*.jks`, `*.keystore` fajlovi

## ✅ Provjera ZIP-a

Nakon kreiranja ZIP-a, provjerite da sadrži:

```bash
# Python provjera
python -c "import zipfile; z = zipfile.ZipFile('FamilyLogbook_Project.zip', 'r'); files = z.namelist(); print('Total:', len(files)); print('SpeechRecognizerHelper:', any('SpeechRecognizerHelper' in f for f in files)); print('VaccinationCalendar:', any('VaccinationCalendar' in f for f in files)); z.close()"
```

## 📝 Napomena

Ako ZIP ne sadrži glasovni unos ili cjepiva, to znači da je kreiran iz starije verzije koda. Provjerite da li su svi fajlovi prisutni u projektu prije kreiranja ZIP-a.
