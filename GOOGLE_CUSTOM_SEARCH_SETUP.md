# Google Custom Search API Setup za Shopping Deals

## Pregled

Shopping deals feature koristi Google Custom Search API za pretraživanje akcija u hrvatskim trgovinama. Ovo je legalno, skalabilno i besplatno do 100 requesta dnevno.

## Koraci za Setup

### 1. Kreiraj Google Custom Search Engine

1. Idite na: https://programmablesearchengine.google.com/
2. Kliknite "Add" da kreirate novi Search Engine
3. **Sites to search**: Unesite:
   ```
   kaufland.hr
   konzum.hr
   spar.hr
   lidl.hr
   plodine.hr
   ```
4. **Name**: "FamilyOS Shopping Deals" (ili bilo koji naziv)
5. Kliknite "Create"

### 2. Dobij Engine ID

1. Nakon kreiranja, otvorite svoj Search Engine
2. Idite u "Setup" → "Basics"
3. Kopirajte **Search engine ID** (npr. `012345678901234567890:abcdefghijk`)

### 3. Enable Custom Search API u Google Cloud Console

1. Idite na: https://console.cloud.google.com/
2. Odaberite svoj projekt (ili kreirajte novi)
3. Idite u "APIs & Services" → "Library"
4. Pretražite "Custom Search API"
5. Kliknite "Enable"

### 4. Kreiraj API Key

1. Idite u "APIs & Services" → "Credentials"
2. Kliknite "Create Credentials" → "API Key"
3. Kopirajte API key
4. (Opcijalno) Ograničite API key na "Custom Search API" za sigurnost

### 5. Dodaj u gradle.properties

Otvorite `gradle.properties` i dodajte:

```properties
GOOGLE_CSE_API_KEY=your_api_key_here
GOOGLE_CSE_ENGINE_ID=your_engine_id_here
```

**VAŽNO**: Ne commitajte `gradle.properties` sa API key-jevima u Git!

## Kako radi

1. Korisnik napiše shopping listu: "jaja, kruh, mlijeko"
2. App ekstraktira proizvode: ["jaja", "kruh", "mlijeko"]
3. Za svaki proizvod, app poziva Google Custom Search API:
   - Query: "jaja akcija site:kaufland.hr OR site:konzum.hr OR ..."
4. API vraća rezultate sa web stranica trgovina
5. App parsira rezultate i ekstraktira:
   - Store name (iz URL-a)
   - Price (ako je u snippet-u)
   - Discount (ako je u snippet-u)
6. Ako se nađu akcije, prikaže se AdviceCard sa deal-ovima
7. Ako se ne nađu akcije, ne prikazuje se ništa

## Limiti

- **Besplatno**: 100 requesta dnevno
- **Paid**: $5 per 1000 dodatnih requesta

Za MVP, 100 requesta dnevno je dovoljno (ako korisnik ima 5 proizvoda u listi, to je 5 requesta po entry-ju).

## ⚠️ PRODUCTION SECURITY

### API Key Restrictions

**Trenutno (za testiranje):**
- API Key restriction: **None** (OK za development)

**Prije production release-a:**
1. Idite u Google Cloud Console → APIs & Services → Credentials
2. Odaberite API key: `AIzaSyAN7eRinnKIjNO6D44b_3UObIr1r5sJTR0`
3. Kliknite "Edit"
4. **API restrictions**:
   - Odaberite "Restrict key"
   - Odaberite samo "Custom Search API"
5. **Application restrictions**:
   - Odaberite "Android apps"
   - Dodajte package name: `com.familylogbook.app`
   - Dodajte SHA-1 certificate fingerprint (release keystore)
6. Spremite promjene

**Zašto je ovo važno:**
- Sprječava zlouporabu API key-ja
- Ograničava pristup samo na vašu aplikaciju
- Smanjuje rizik od neovlaštenog korištenja

**Vidi**: `PRODUCTION_CHECKLIST.md` za detaljne instrukcije.

## Troubleshooting

### "API key or Engine ID not configured"
- Provjerite da li su `GOOGLE_CSE_API_KEY` i `GOOGLE_CSE_ENGINE_ID` postavljeni u `gradle.properties`
- Provjerite da li je API key validan u Google Cloud Console
- Provjerite da li je Custom Search API enabled

### "API call failed: 403"
- Provjerite da li je Custom Search API enabled u Google Cloud Console
- Provjerite da li je API key validan
- Provjerite da li je API key ograničen na Custom Search API (ako jeste, uklonite ograničenje ili dodajte Custom Search API)

### "Empty response body"
- Provjerite da li je Engine ID ispravan
- Provjerite da li Search Engine pretražuje ispravne domene

### Nema rezultata
- Provjerite da li Search Engine pretražuje ispravne domene
- Provjerite da li trgovine imaju akcije na svojim web stranicama
- Provjerite da li query string sadrži "akcija" (možda treba dodati više keyword-ova)

## Napredne opcije

### Dodaj više trgovina

U `GoogleCustomSearchService.kt`, dodaj u `storeDomains`:
```kotlin
private val storeDomains = listOf(
    "kaufland.hr",
    "konzum.hr",
    "spar.hr",
    "lidl.hr",
    "plodine.hr",
    "nova-trgovina.hr" // Dodaj novu trgovinu
)
```

### Optimiziraj query

Možete modificirati query u `GoogleCustomSearchService.kt`:
```kotlin
val query = "$product akcija popust ($storeSiteFilter)"
```

### Dodaj caching

Za bolje performanse, možete dodati caching (24h) u `GoogleCustomSearchService.kt`.
