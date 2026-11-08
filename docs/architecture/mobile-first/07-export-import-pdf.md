# 07. Export complet, export automat, import și PDF pentru medic

Deciziile: [ADR-0006](../adr/0006-backup-si-transfer-intre-dispozitive.md) (backup), [ADR-0011](../adr/0011-export-pentru-medic.md) (PDF), cerințele DAT-20…28.

## 1. Exportul complet (DAT-20)

### 1.1 Ce conține

Un fișier `.patient` care, **după decriptare**, este o arhivă ZIP:

```
manifest.json            ← versiuni, număr de resurse, hash-ul fiecărei intrări
fhir/bundle.json         ← FHIR Bundle "collection" cu toate resursele
blobs/<sha256>           ← fișierele originale, decriptate (doar în interiorul exportului criptat)
app/reminders.json       ← programul mementourilor (nu face parte din FHIR)
app/settings.json        ← setările care au sens pe un telefon nou (blocare, limbă, capturi); fără dosarul de export
```

```json
{
  "format": "patient-export",
  "format_version": 1,
  "created_at": "2026-11-06T20:00:00Z",
  "app_version": "1.0.0",
  "fhir_version": "4.0.1",
  "ig_version": "1.0.0",
  "db_schema_version": 1,
  "counts": { "Observation": 312, "DocumentReference": 41, "MedicationStatement": 9 },
  "entries": { "fhir/bundle.json": "<sha256>", "blobs/3f8a…": "<sha256>", "app/reminders.json": "<sha256>" }
}
```

ZIP-ul se scrie **fără compresie** pentru imagini și PDF-uri (sunt deja comprimate) și cu `DEFLATE` pentru JSON.

### 1.2 Criptarea

Exportul se poate deschide **doar cu cele 24 de cuvinte** (decizie 04.11.2026). Telefonul are doar cheia publică de export, deci poate crea exporturi, dar nu le poate citi fără cuvinte.

| Pas | Implementare |
|---|---|
| 1. Cheia conținutului | `CEK` = 32 de octeți aleatori |
| 2. Împachetarea CEK | **HPKE** (RFC 9180), mod Base, suita `DHKEM(X25519, HKDF-SHA256)`, `HKDF-SHA256`, `AES-256-GCM`, către `export_public_key`. `info = "patient/export/v1"`. Rezultat: `enc` (32 de octeți) + `wrapped_cek` |
| 3. Criptarea conținutului | Tink `StreamingAead` **`AES256_GCM_HKDF_1MB`**, cu cheia creată din `CEK`. Datele asociate = octeții antetului (§1.3) |
| 4. Implementarea | API-ul HPKE din Tink (`HpkeParameters`, `HpkePublicKey`, `HpkePrivateKey`) și cheile streaming create din octeți bruți. **Test cu vectori ficși** (cuvinte cunoscute → cheie publică așteptată → export care se deschide), care va fi refolosit pe iOS |

### 1.3 Formatul fișierului

```
"PATIENTX"                      8 octeți (magic)
versiune                        1 octet  (= 1)
lungimea antetului              4 octeți (big-endian)
antet JSON                      { "suite": "hpke-x25519-sha256-aes256gcm/streaming-aes256gcmhkdf1mb",
                                  "enc": "<base64>", "wrapped_cek": "<base64>",
                                  "created_at": "…", "app_version": "…" }
conținut criptat în flux        restul fișierului
```

Antetul nu conține date medicale. Numele fișierului: `patient-export-YYYYMMDD-HHmm.patient`.

### 1.4 Exportul manual

1. Setări → „Exportă toate datele”.
2. Explicație: „Fișierul se poate deschide doar cu cele 24 de cuvinte de recuperare. Îl poți păstra oriunde, inclusiv în Google Drive.”
3. Destinație: `ACTION_CREATE_DOCUMENT` (salvare) sau meniul de partajare.
4. Scrierea se face în flux, direct în destinație: datele decriptate **nu** ajung pe disc în clar.
5. Bară de progres; posibilitate de anulare.
6. `AuditEvent` `export`.

### 1.5 Exportul automat (DAT-24)

| Element | Implementare |
|---|---|
| Configurare | Setări → „Export automat”: frecvența (`off` / zilnic / săptămânal / lunar), dosarul (`ACTION_OPEN_DOCUMENT_TREE` + `takePersistableUriPermission`), câte exporturi se păstrează (implicit 3) |
| Declanșare | La fiecare deblocare și o dată pe oră cât aplicația este deschisă și deblocată: dacă `acum − export.last_success_at ≥ frecvența`, exportul pornește în fundal (coroutine), cu prioritate mică |
| Fără WorkManager | Exportul are nevoie de date decriptate, deci rulează doar cu aplicația deblocată. La blocare se anulează; fișierul parțial se șterge |
| Rotația | După succes: în dosar se păstrează ultimele N fișiere `patient-export-*.patient`; cele mai vechi se șterg (doar fișierele cu acest model de nume) |
| Fără mementouri | Nicio notificare de tip „fă un export” (decizie 04.11.2026) |
| Starea | Setările arată data ultimului export reușit și, dacă e cazul, ultima eroare (de exemplu „Dosarul nu mai este accesibil”) |
| Audit | `AuditEvent` `auto-export` |

## 2. Importul (DAT-22)

În prima versiune, importul **restaurează** datele pe un telefon nou sau după reinstalare. Nu combină datele cu cele existente.

### 2.1 Unde apare

- La prima pornire: „Am deja un export” (alternativă la „Încep de la zero”).
- În setări: „Restaurează dintr-un export”, care **înlocuiește** toate datele curente, după confirmare dublă și oferta de a face întâi un export.

### 2.2 Pașii

1. Pacientul alege fișierul (`ACTION_OPEN_DOCUMENT`).
2. Se citește antetul; se verifică `magic`, versiunea și suita.
3. Pacientul introduce cele 24 de cuvinte (doc. 04 §5.2) → RS → cheia privată de export → HPKE → `CEK`.
4. Decriptare în flux și verificare:
   - hash-ul fiecărei intrări față de `manifest.json`;
   - `format_version` cunoscut (dacă exportul e dintr-o versiune mai nouă a aplicației: mesaj „Actualizează aplicația”);
   - validarea fiecărei resurse FHIR (doc. 03 §8).
5. Dacă ceva eșuează: nimic nu se scrie; mesaj clar.
6. Configurare nouă a seifului: **MK nou**, cheie HW nouă, parolă nouă, dar **aceleași 24 de cuvinte** (RS vine din cuvintele introduse). Astfel, exporturile vechi și cele noi se deschid cu aceleași cuvinte.
7. Scriere într-o singură tranzacție: resursele, fișierele (recriptate cu chei noi), mementourile, setările.
8. `RebuildIndex`, reprogramarea alarmelor, `AuditEvent` `import`.

### 2.3 Testul obligatoriu

**Round-trip:** set de date de test → export → ștergere totală → import → comparație resursă cu resursă și fișier cu fișier (doc. 09).

## 3. PDF-ul pentru medic (DAT-21, DAT-25…28)

### 3.1 Selecția (DAT-25)

Ecranul „Trimite medicului”. Pacientul alege una sau mai multe opțiuni:

| Opțiune | Detaliu |
|---|---|
| Analizele dintr-o perioadă | De la … până la … (scurtături: ultimele 3 / 6 / 12 luni) |
| Anumite buletine | Listă cu bifare |
| Anumite teste, cu evoluția lor | Alegerea testelor (de exemplu glicemia), într-o perioadă |
| Lista medicamentelor | Active; opțional și cele încheiate într-o perioadă |
| **Documentele originale** | **Nebifat implicit** (DAT-27). Dacă e bifat: pozele / paginile buletinelor selectate |

Previzualizare înainte de trimitere (numărul de pagini și lista secțiunilor).

### 3.2 Conținutul (DAT-26, DAT-28)

| Secțiune | Conținut |
|---|---|
| Antet (fiecare pagină) | „Rezumat generat de pacient în aplicația Patient” · data generării · numărul paginii |
| Prima pagină | Numele și anul nașterii, **doar dacă sunt completate în profil**. Textul: „Valorile au fost transcrise din documentele originale și verificate de pacient. Acesta nu este un document medical oficial.” |
| Buletine | Pentru fiecare: laboratorul, data recoltării, apoi tabel: denumirea **ca în document**, valoarea **ca în document**, unitatea, intervalul de referință **ca în document**, marcajul **doar dacă apare în document**, sursa („transcris din buletinul din 12.10.2026, verificat de pacient” / „introdus manual”) |
| Evoluția testelor | Tabel cronologic: data, valoarea, unitatea, intervalul, laboratorul. Opțional, un grafic simplu (linii, fără culori de tip „anormal”). Dacă unitățile diferă, valorile **nu** se convertesc în tabel; se afișează cum sunt |
| Medicamente | Denumirea, concentrația, schema (textul), perioada, sursa |
| Anexe | Doar dacă au fost bifate: paginile originale, câte una pe pagină |
| **Excluse** | Răspunsuri AI (DAT-28), marcaje calculate, note interne ale aplicației, ID-uri, date de securitate |

Limba PDF-ului: limba aplicației (EN sau RO).

### 3.3 Generarea

| Element | Implementare |
|---|---|
| API | `android.graphics.pdf.PdfDocument` (A4: 595 × 842 puncte), desen pe `Canvas` cu `StaticLayout` pentru text |
| Fonturi | Fonturile sistemului (suportă diacriticele RO și chirilica); nimic descărcat |
| Imagini | Originalele decriptate în memorie, scalate la pagină. PDF-urile originale: paginile randate cu `PdfRenderer` |
| Fișierul | Scris în `cacheDir/share/<uuid>.pdf`, **necriptat** (trebuie citit de alte aplicații). Partajat prin `FileProvider` cu permisiune temporară de citire |
| Curățare | Fișierul se șterge după închiderea meniului de partajare, la blocare și la fiecare pornire. Este singura excepție documentată de la SEC-05 |
| Fără parolă | Decizie 04.11.2026 |

### 3.4 Trimiterea

1. Avertisment obligatoriu înainte de meniul de partajare: **„Fișierul va ieși din aplicație și nu mai este protejat de ea.”** (cu „Continuă” / „Renunță”).
2. Meniul de partajare al sistemului (`Intent.createChooser`), sau „Arată pe ecran” (vizualizare în aplicație, fără fișier).
3. `AuditEvent` `pdf-shared` (fără conținut).
