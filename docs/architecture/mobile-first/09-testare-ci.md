# 09. Testare, CI și ordinea de implementare

## 1. Tipurile de teste

| Tip | Unde | Ce acoperă |
|---|---|---|
| Unitare comune | `commonTest`, rulate pe JVM (`jvmTest`) | Toată logica din `fhir`, `domain`, `ingestion`: reguli, normalizare, scor, construirea resurselor |
| Bază de date | `jvmTest` cu `BundledSQLiteDriver` | DAO-uri, `ResourceIndexer`, cascadele de ștergere, `RebuildIndex`, migrările |
| Criptografie | `androidUnitTest` / teste pe dispozitiv | Vectorii de test (§3), formatul `vault.json`, exportul |
| Pe dispozitiv | `androidInstrumentedTest` | Keystore, biometrie (simulată), SQLCipher, OCR pe imagini de test, PDF, alarme |
| Interfață | Teste Compose | Fluxurile critice: configurare, deblocare, revizuire („Confirmă tot” blocat corect), ștergere totală |
| Conformitate ingestie | `jvmTest` + dispozitiv | Suita de fișiere de referință (§2) |

## 2. Fișierele de referință (`testdata/`)

```
testdata/
├── fhir/                      ← exemplele din doc. 03, validate în CI
├── ingestion/
│   └── <caz>/                 ← de exemplu "lab-ro-printed-synevo-01"
│       ├── input/             ← paginile (imagini / PDF), FĂRĂ date reale ale unor persoane
│       ├── layout.json        ← TextLayout așteptat (opțional, pentru testarea etapelor fără OCR)
│       ├── candidates.json    ← adevărul de referință (format LabCandidate / MedicationCandidate)
│       ├── fhir.json          ← resursele FHIR așteptate după confirmare
│       └── meta.json          ← limba, tipărit / de mână, sursa, sursa documentului (sintetic / anonimizat)
└── crypto/                    ← vectorii de test (§3)
```

| Regulă | Cerință |
|---|---|
| Fără date reale ale unor persoane; documentele reale doar anonimizate verificat | ARH-32, ING-130 |
| Acoperire: EN și RO; tipărit și scris de mână; PDF digital, PDF scanat, foto bună, foto slabă; ≥ 10 formate de buletine; rețete; notițe | ING-131 |
| Etapele de după OCR se testează pornind de la `layout.json`, ca să fie deterministe pe JVM | — |
| Măsurătorile (PIPELINE_INGESTIE §15.2) se calculează automat și se compară cu pragurile | ING-133 |
| **O valoare greșită marcată `OK` este eroarea cea mai gravă** și blochează build-ul | §15.2 |

## 3. Vectorii de test criptografici

Fișiere JSON fixe, generate o singură dată și păstrate în Git. Aceleași fișiere se vor folosi pe iOS.

| Vector | Intrare → ieșire așteptată |
|---|---|
| BIP-39 | Vectorii oficiali BIP-39 (entropie ↔ cuvinte) |
| Derivări HKDF | RS fix → RWK, cheia privată și cheia publică de export |
| Argon2id | Parolă + sare + parametri → PK (vectori RFC 9106) |
| Împachetare | MK + PK + RWK fixe → decriptarea unor `wrapped_*` fixe |
| Export | Cuvinte fixe + fișier `.patient` mic, fix → conținut decriptat așteptat |

## 4. CI (GitHub Actions)

Rulează la fiecare pull request:

| Pas | Unealtă | Blochează dacă |
|---|---|---|
| Build | Gradle | Eroare de compilare |
| Teste JVM | `./gradlew jvmTest` | Orice test eșuat |
| Teste Android unitare | `./gradlew testDebugUnitTest` | Orice test eșuat |
| Analiză statică | Android Lint, detekt | Erori; avertismentele de securitate |
| **Verificarea manifestului** | Script pe manifestul final (`processReleaseManifest`) | Apare `android.permission.INTERNET` sau altă permisiune nepermisă (doc. 01 §5); `allowBackup` nu e `false` |
| **Dependențe** | Scanare de vulnerabilități (OSV-Scanner) + raport de licențe | Vulnerabilitate critică; licență în afara listei (Apache 2.0, MIT, BSD) |
| Dependențe interzise | Script pe graful de dependențe | Apare Firebase, `play-services-mlkit-*`, iText, biblioteci de rețea |
| Secrete | gitleaks | Secret găsit |
| FHIR | SUSHI (compilează `fhir-ig/`) + validatorul HL7 (`validator_cli.jar -version 4.0.1 -ig …`) pe `testdata/fhir/` și pe exporturile generate de teste | Orice eroare de validare |
| Ingestie | Suita de conformitate pe `layout.json` | Scădere sub prag |
| SBOM | CycloneDX | — (artefact păstrat la fiecare versiune) |

Înainte de fiecare versiune (manual sau programat): testele pe dispozitive N1 / N2 / N3, suita de ingestie cu OCR real, scanarea MobSF a APK-ului, lista din `SECURITY_NOTES.md`.

## 5. Când este gata o funcție (Definition of Done)

- [ ] Cerințele (`ING-`, `SEC-`, `DAT-`…) sunt menționate în cod și în pull request.
- [ ] Teste unitare pentru logica nouă; teste pe dispozitiv pentru partea de platformă.
- [ ] Nicio dată medicală în loguri (verificare în revizuirea codului).
- [ ] Textele în EN și RO.
- [ ] Accesibilitate verificată (TalkBack, text mărit).
- [ ] CI verde, inclusiv verificarea manifestului.
- [ ] Documentația actualizată, dacă implementarea a cerut o schimbare (aprobată de arhitectură).

## 6. Ordinea de implementare (propunere)

Ordinea pune **siguranța datelor** înaintea funcțiilor: exportul vine devreme, ca orice versiune de test să aibă backup. Etapele F0–F7 din plan se ajustează după decizia pe întrebările de la pasul 14 (export în F1, lansare fără AI, beta).

| # | Pas | Conținut | Documente |
|---|---|---|---|
| 1 | Scheletul | Proiectul KMP, modulele, CI cu toate verificările din §4, manifestul fără Internet | 01, 09 |
| 2 | Seiful | Configurarea, parola, cheia HW, cuvintele de recuperare, deblocarea, blocarea automată, ștergerea totală | 04 |
| 3 | Datele | Room + SQLCipher, modelul FHIR, `RecordStore`, indexarea, validarea | 02, 03 |
| 4 | Funcțiile manuale | Profil, notițe, medicamente, tratamente, valori introduse manual, grafice | 03, 06, 08 |
| 5 | Mementourile | Alarme, `alarms.json`, notificări, repornire | 06 |
| 6 | Export și import | Exportul complet, exportul automat, restaurarea, testul round-trip | 07 |
| 7 | Documentele | Captură, import, fișiere criptate, vizualizare, căutare | 05 §3.1–3.2, 02 |
| 8 | OCR și extragerea | OCR, clasificare, layout, extragere, normalizare, validare, scor; benchmark-ul scrisului de mână | 05 |
| 9 | Revizuirea și confirmarea | Ecranul de revizuire, `ConfirmIngestion`, proveniența | 05 §4–5 |
| 10 | PDF pentru medic | Selecția, generarea, partajarea | 07 §3 |
| 11 | Finisare | Izolarea parserului, accesibilitate, texte juridice, `SECURITY_NOTES.md`, pregătirea testului de penetrare | 04, 08 |
