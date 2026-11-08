# 03. Modelul FHIR R4 în Kotlin

Deciziile de modelare sunt în [MODEL_DATE_FHIR.md](../MODEL_DATE_FHIR.md). Aici este implementarea: clasele Kotlin, serializarea, exemple JSON complete și regulile de construire a fiecărei resurse.

## 1. Abordarea

| Decizie | Detaliu |
|---|---|
| Clase proprii, nu HAPI FHIR | Doar resursele și câmpurile pe care le folosim, în `shared:fhir/commonMain` (ADR-0012) |
| Versiunea | FHIR **R4 (4.0.1)** |
| Serializare | `kotlinx.serialization`; JSON conform specificației FHIR |
| Corectitudinea | Garantată de testele din CI cu **validatorul oficial HL7** (doc. 09), pe toate exemplele de aici și pe exporturile generate de teste |
| Câmpuri noi | Se adaugă doar când e nevoie, cu test și validare. Nu se copiază „tot standardul” |

## 2. Configurarea JSON

```kotlin
val FhirJson = Json {
    classDiscriminator = "resourceType"   // FHIR identifică tipul prin "resourceType"
    explicitNulls = false                 // câmpurile null nu apar în JSON (cerință FHIR)
    encodeDefaults = false
    ignoreUnknownKeys = false             // strict: un câmp necunoscut indică o eroare
    prettyPrint = false
}
```

| Regulă FHIR | Cum o respectăm |
|---|---|
| Fără `null` și fără liste goale în JSON | `explicitNulls = false`; listele sunt `List<T>?` și se pun `null` când sunt goale |
| Numerele zecimale își păstrează precizia (`5.40` ≠ `5.4`) | `value` este de tip `FhirDecimal`, serializat din textul original, nu din `Double` (§4) |
| Datele pot avea precizie parțială (`1985`, `2026-10`, `2026-10-12`) | `FhirDate` / `FhirDateTime` sunt `String` validate, nu `LocalDate` |
| `resourceType` este primul câmp | Nu este obligatoriu pentru validare; nu ne bazăm pe ordine |

## 3. Tipurile de date

```kotlin
@Serializable sealed class Resource {
    abstract val id: String
    abstract val meta: Meta?
}

@Serializable data class Meta(
    val versionId: String? = null,
    val lastUpdated: String? = null,          // instant FHIR, UTC
    val profile: List<String>? = null,
    val tag: List<Coding>? = null,
)

@Serializable data class Coding(val system: String? = null, val code: String? = null, val display: String? = null)
@Serializable data class CodeableConcept(val coding: List<Coding>? = null, val text: String? = null)
@Serializable data class Reference(val reference: String? = null, val display: String? = null)   // "Observation/<uuid>"
@Serializable data class Period(val start: String? = null, val end: String? = null)
@Serializable data class Annotation(val text: String, val time: String? = null)
@Serializable data class HumanName(val text: String? = null)

@Serializable data class Quantity(
    val value: FhirDecimal? = null,
    val comparator: String? = null,           // "<", "<=", ">=", ">"
    val unit: String? = null,                 // textul din document: "mmol/l"
    val system: String? = null,               // "http://unitsofmeasure.org"
    val code: String? = null,                 // UCUM: "mmol/L"
)
@Serializable data class Ratio(val numerator: Quantity? = null, val denominator: Quantity? = null)

@Serializable data class Attachment(
    val contentType: String? = null,
    val data: String? = null,                 // base64, doar pentru text mic (notițe)
    val url: String? = null,                  // "urn:patient:blob:sha256:<hex>" pentru fișiere
    val size: Long? = null,
    val hash: String? = null,                 // SHA-1 base64 (cerință R4)
    val title: String? = null,
    val creation: String? = null,
    val extension: List<Extension>? = null,
)

@Serializable data class Extension(
    val url: String,
    val valueString: String? = null,
    val valueBase64Binary: String? = null,
    val valueInteger: Int? = null,
    val valueBoolean: Boolean? = null,
)
```

## 4. `FhirDecimal`

Valorile de laborator trebuie salvate **exact** cum apar în document (ING-087). `Double` pierde zerourile finale (`5.40` devine `5.4`) și poate introduce erori de rotunjire.

```kotlin
@Serializable(with = FhirDecimalSerializer::class)
@JvmInline value class FhirDecimal(val text: String) {     // "5.40", "1250", "0.003"
    init { require(Regex("""-?(0|[1-9]\d*)(\.\d+)?([eE][+-]?\d+)?""").matches(text)) }
    fun toDouble(): Double = text.toDouble()                // doar pentru grafice și sortare
}
```

Serializatorul scrie `text` ca **număr JSON**, nu ca șir (folosind `JsonUnquotedLiteral`). Separatorul zecimal al documentului (`5,4`) se normalizează la punct **înainte** de a crea `FhirDecimal` (ING-081).

## 5. Sistemele de coduri

```kotlin
object FhirSystems {
    const val BASE = "https://fhir.patient.invalid"          // [Necesită răspuns]: domeniul proiectului
    const val LOINC = "http://loinc.org"
    const val UCUM = "http://unitsofmeasure.org"
    const val ATC = "http://www.whocc.no/atc"
    const val V2_0074 = "http://terminology.hl7.org/CodeSystem/v2-0074"                     // LAB
    const val OBS_CATEGORY = "http://terminology.hl7.org/CodeSystem/observation-category"    // laboratory
    const val V3_INTERPRETATION = "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation"
    const val PROVENANCE_PARTICIPANT = "http://terminology.hl7.org/CodeSystem/provenance-participant-type"
    const val CONSENT_SCOPE = "http://terminology.hl7.org/CodeSystem/consentscope"

    // Coduri proprii
    const val VERIFICATION = "$BASE/fhir/CodeSystem/verification"     // unconfirmed | confirmed | manual
    const val DOCUMENT_CLASS = "$BASE/fhir/CodeSystem/document-class" // lab-report | prescription | note | other
    const val PROVENANCE_ACTIVITY = "$BASE/fhir/CodeSystem/provenance-activity" // extraction | verification | manual-entry
    const val AUDIT_EVENT_TYPE = "$BASE/fhir/CodeSystem/audit-event-type" // export | import | wipe | unlock-failed | auto-export
    const val LAB_LOCAL = "$BASE/fhir/CodeSystem/lab-local"

    // Extensii proprii
    const val EXT_SHA256 = "$BASE/fhir/StructureDefinition/attachment-sha256"
    const val EXT_SOURCE_REGION = "$BASE/fhir/StructureDefinition/source-region"
    const val EXT_HANDWRITTEN = "$BASE/fhir/StructureDefinition/handwritten"
}
```

### Starea de verificare (`meta.tag`)

| Cod | Când |
|---|---|
| `unconfirmed` | Nu se salvează niciodată în `resource` în prima versiune: valorile neconfirmate stau doar în ciornă. Codul există pentru completitudinea sistemului |
| `confirmed` | Valoarea a fost extrasă automat și pacientul a confirmat-o (inclusiv dacă a corectat-o înainte de confirmare). O editare ulterioară păstrează `confirmed` |
| `manual` | Resursa a fost creată de pacient, fără extragere (rând adăugat manual, notiță scrisă în aplicație, medicament adăugat manual) |

## 6. Resursele primei versiuni

| Resursă | Folosire | Câmpuri implementate |
|---|---|---|
| `Patient` | Profilul (unul singur) | `id`, `meta`, `name[0].text`, `gender`, `birthDate` (doar anul) |
| `Organization` | Laboratorul din document | `id`, `meta`, `name` |
| `DocumentReference` | Documentul original; notița | `status`, `type`, `category`, `subject`, `date`, `description`, `content[].attachment`, `context.related` |
| `DiagnosticReport` | Buletinul de analize | `status`, `category`, `code`, `subject`, `effectiveDateTime`, `issued`, `performer`, `result` |
| `Observation` | O valoare | `status`, `category`, `code`, `subject`, `effectiveDateTime`, `value[x]` (`valueQuantity`, `valueString`, `valueRatio`), `interpretation`, `referenceRange`, `note`, `extension` |
| `MedicationStatement` | Un medicament | `status`, `medicationCodeableConcept`, `subject`, `effectivePeriod`, `dateAsserted`, `informationSource`, `derivedFrom`, `basedOn`, `dosage` |
| `CarePlan` | Un tratament (grup de medicamente) | `status`, `intent`, `title`, `subject`, `period` |
| `Provenance` | Extragerea și confirmarea | `target`, `recorded`, `activity`, `agent`, `entity` |
| `Device` | Componenta software (OCR, extractor, catalog) | `deviceName`, `version`, `type` |
| `Consent` | Acordurile date în aplicație | `status`, `scope`, `category`, `patient`, `dateTime`, `policy`, `provision` |
| `AuditEvent` | Evenimente de securitate locale | `type`, `recorded`, `outcome`, `agent`, `source` |
| `Bundle` | Doar pentru export / import | `type = collection`, `timestamp`, `entry[].fullUrl`, `entry[].resource` |

`Communication` (conversațiile AI) se adaugă odată cu asistentul AI.

**Corectură față de planul inițial:** în FHIR R4, `CarePlan.activity.reference` nu acceptă `MedicationStatement`. Legătura se face invers: **`MedicationStatement.basedOn` → `CarePlan`** (câmp valid în R4). MODEL_DATE_FHIR.md a fost actualizat.

### Referințele

Toate referințele sunt **relative**: `"Observation/9b7e…"`. În exportul `Bundle`, fiecare intrare are `fullUrl = "urn:uuid:<id>"`, iar referințele rămân relative (validatorul le rezolvă în bundle).

## 7. Exemple complete

Toate exemplele de mai jos se pun în `testdata/fhir/` și trec prin validatorul HL7 în CI. `{BASE}` = `FhirSystems.BASE`.

### 7.1 `Patient`

```json
{
  "resourceType": "Patient",
  "id": "4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11",
  "meta": { "profile": ["{BASE}/fhir/StructureDefinition/patient-self|1.0.0"] },
  "name": [ { "text": "Maria Popescu" } ],
  "gender": "female",
  "birthDate": "1985"
}
```

Fără `identifier`, `address`, `telecom` (decizie 04.11.2026). Toate câmpurile sunt opționale.

### 7.2 `DocumentReference`: documentul original

```json
{
  "resourceType": "DocumentReference",
  "id": "a1d3c6e2-1111-4a8e-9f0b-5e2c3b4d5a6f",
  "meta": { "tag": [ { "system": "{BASE}/fhir/CodeSystem/verification", "code": "confirmed" } ] },
  "status": "current",
  "type": { "coding": [ { "system": "http://loinc.org", "code": "11502-2", "display": "Laboratory report" } ] },
  "category": [ { "coding": [ { "system": "{BASE}/fhir/CodeSystem/document-class", "code": "lab-report" } ] } ],
  "subject": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "date": "2026-10-12T09:15:00Z",
  "description": "Synevo – 12.10.2026",
  "content": [ {
    "attachment": {
      "contentType": "application/pdf",
      "url": "urn:patient:blob:sha256:3f8a…c21",
      "size": 284113,
      "hash": "2jmj7l5rSw0yVb/vlWAYkK/YBwk=",
      "title": "buletin.pdf",
      "extension": [ { "url": "{BASE}/fhir/StructureDefinition/attachment-sha256", "valueString": "3f8a…c21" } ]
    }
  } ],
  "context": { "related": [ { "reference": "DiagnosticReport/0c5b8e1a-2222-4b6f-8a7d-1e2f3a4b5c6d" } ] }
}
```

| Câmp | Regulă |
|---|---|
| `type` | `lab-report` → LOINC `11502-2`; `prescription` → LOINC `57833-6` „Prescription for medication”; `note` → LOINC `51855-5` „Patient Note”; `other` → doar `text` |
| `content.attachment.url` | Schema locală `urn:patient:blob:sha256:<hex>`; trimite la tabelul `blob`. În export, fișierul se pune în arhivă (doc. 07) |
| `hash` | SHA-1 în base64 (cerință R4) + extensia cu SHA-256 (hex), cheia reală a fișierului |
| `date` | Momentul adăugării în aplicație |

### 7.3 `DiagnosticReport`

```json
{
  "resourceType": "DiagnosticReport",
  "id": "0c5b8e1a-2222-4b6f-8a7d-1e2f3a4b5c6d",
  "meta": { "tag": [ { "system": "{BASE}/fhir/CodeSystem/verification", "code": "confirmed" } ] },
  "status": "final",
  "category": [ { "coding": [ { "system": "http://terminology.hl7.org/CodeSystem/v2-0074", "code": "LAB" } ] } ],
  "code": { "coding": [ { "system": "http://loinc.org", "code": "11502-2", "display": "Laboratory report" } ] },
  "subject": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "effectiveDateTime": "2026-10-12",
  "issued": "2026-10-12T14:00:00+03:00",
  "performer": [ { "reference": "Organization/7d1e2f3a-3333-4c5d-9e8f-0a1b2c3d4e5f", "display": "Synevo" } ],
  "result": [ { "reference": "Observation/9b7e5c3a-4444-4d2e-8f1a-6b5c4d3e2f10" } ]
}
```

| Câmp | Regulă |
|---|---|
| `effectiveDateTime` | Data recoltării, cu precizia din document (ING-073) |
| `issued` | Data eliberării, dacă apare. FHIR cere `instant` (dată + oră + fus orar); dacă documentul are doar data, câmpul **se omite** (nu inventăm ora) |
| `performer` | `Organization` deduplicată după numele normalizat |

### 7.4 `Observation`: valoare numerică

```json
{
  "resourceType": "Observation",
  "id": "9b7e5c3a-4444-4d2e-8f1a-6b5c4d3e2f10",
  "meta": { "tag": [ { "system": "{BASE}/fhir/CodeSystem/verification", "code": "confirmed" } ] },
  "extension": [
    { "url": "{BASE}/fhir/StructureDefinition/source-region", "valueString": "page=1;bbox=412,1180,1650,64" }
  ],
  "status": "final",
  "category": [ { "coding": [ { "system": "http://terminology.hl7.org/CodeSystem/observation-category", "code": "laboratory" } ] } ],
  "code": {
    "coding": [ { "system": "http://loinc.org", "code": "2345-7", "display": "Glucose [Mass/volume] in Serum or Plasma" } ],
    "text": "Glucoza"
  },
  "subject": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "effectiveDateTime": "2026-10-12",
  "valueQuantity": { "value": 5.40, "unit": "mmol/l", "system": "http://unitsofmeasure.org", "code": "mmol/L" },
  "referenceRange": [ { "low": { "value": 3.9, "unit": "mmol/l", "system": "http://unitsofmeasure.org", "code": "mmol/L" },
                        "high": { "value": 6.1, "unit": "mmol/l", "system": "http://unitsofmeasure.org", "code": "mmol/L" },
                        "text": "3,9 - 6,1" } ]
}
```

*(Valoarea este în interval și documentul nu are marcaj, deci lipsește `interpretation`. Exemplul LOINC este ilustrativ: codul exact vine din catalog, ales după context; glucoza în mmol/L are alt cod LOINC decât în mg/dL.)*

### 7.5 Regulile pentru `Observation`

| Situație din document (ING-070…075) | Cum se scrie |
|---|---|
| Numeric `5,4 mmol/l` | `valueQuantity.value = 5.4`, `unit = "mmol/l"`, `code = "mmol/L"` |
| Cu comparator `<0.5` | `valueQuantity.comparator = "<"`, `value = 0.5` |
| Unitate necunoscută în UCUM | `valueQuantity` fără `system` și `code`, doar `unit` (textul original) |
| Calitativ `Negativ`, `++` | `valueString = "Negativ"` (exact ca în document) |
| Titru `1:160` | `valueRatio` (numărător 1, numitor 160) |
| Interval `3-5 în câmp`, text liber | `valueString` cu textul din document |
| Fără cod LOINC sigur (ING-083) | Doar `code.text` + `code.coding` cu sistemul `lab-local` (denumirea normalizată) |
| Interval de referință numeric | `referenceRange[0].low/high` + `text` (textul exact) |
| Interval pe sex/vârstă, prag, text (ING-071) | **Doar** `referenceRange[0].text` cu tot textul. `low/high` se completează doar dacă pacientul și-a completat sexul și componenta este neambiguă |
| Marcaj H/L în document (ING-072) | `interpretation[0].text` = marcajul exact (`H`, `↑`, `Crescut`). Opțional `coding` cu `v3-ObservationInterpretation` (`H`, `L`, `A`) dacă maparea e directă. **Fără marcaj în document → fără `interpretation`** |
| Comentariu atașat rezultatului | `note[0].text` |
| Valoare scrisă de mână | Extensia `handwritten` cu `valueBoolean = true` |
| Poziția în document | Extensia `source-region`: `page=<n>;bbox=x,y,w,h` (pixeli, pagina randată la rezoluția de procesare) |

### 7.6 `DocumentReference`: notiță

```json
{
  "resourceType": "DocumentReference",
  "id": "c2e4f6a8-5555-4b1d-8c3e-7f9a0b1c2d3e",
  "meta": { "tag": [ { "system": "{BASE}/fhir/CodeSystem/verification", "code": "manual" } ] },
  "status": "current",
  "type": { "coding": [ { "system": "http://loinc.org", "code": "51855-5", "display": "Patient Note" } ] },
  "category": [ { "coding": [ { "system": "{BASE}/fhir/CodeSystem/document-class", "code": "note" } ] } ],
  "subject": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "date": "2026-11-05T18:20:00Z",
  "content": [ { "attachment": { "contentType": "text/plain; charset=utf-8", "data": "QW0gYXZ1dCBkdXJlcmkgZGUgY2FwLi4u" } } ]
}
```

O notiță scrisă de mână și citită prin OCR are **două** `content`: textul (ca mai sus) și imaginea originală (`url` către `blob`), iar eticheta este `confirmed`.

### 7.7 `MedicationStatement`

```json
{
  "resourceType": "MedicationStatement",
  "id": "e8f0a2b4-6666-4c7d-9e1f-3a5b7c9d1e2f",
  "meta": { "tag": [ { "system": "{BASE}/fhir/CodeSystem/verification", "code": "confirmed" } ] },
  "basedOn": [ { "reference": "CarePlan/f1a3b5c7-7777-4d8e-8f2a-4b6c8d0e1f3a" } ],
  "status": "active",
  "medicationCodeableConcept": {
    "coding": [ { "system": "http://www.whocc.no/atc", "code": "A10BA02", "display": "metformin" } ],
    "text": "Metformin 500 mg"
  },
  "subject": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "effectivePeriod": { "start": "2026-11-01", "end": "2026-11-30" },
  "dateAsserted": "2026-11-01T10:00:00Z",
  "informationSource": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "derivedFrom": [ { "reference": "DocumentReference/a1d3c6e2-1111-4a8e-9f0b-5e2c3b4d5a6f" } ],
  "dosage": [ {
    "text": "1 tablet twice daily after meals",
    "timing": { "repeat": { "frequency": 2, "period": 1, "periodUnit": "d", "timeOfDay": ["08:00:00", "20:00:00"], "when": ["PC"] } },
    "doseAndRate": [ { "doseQuantity": { "value": 1, "unit": "tablet" } } ]
  } ]
}
```

| Regulă | Cerință |
|---|---|
| `dosage.text` = textul exact din document sau scris de pacient; **întotdeauna prezent** | ING-090 |
| `timing` se completează doar dacă textul este neambiguu sau pacientul l-a completat. Din `timeOfDay` se creează mementourile | ING-091 |
| `doseQuantity` doar cum scrie în document sau cum a introdus pacientul. **Aplicația nu calculează și nu modifică doze** | ADR-0005 |
| Codul ATC este o **sugestie** din catalog, confirmată de pacient; `text` rămâne denumirea din document | ING-092 |
| Încheierea unui tratament: `status = completed`, `effectivePeriod.end` = data | §7.2 din plan |

### 7.8 `CarePlan`

```json
{
  "resourceType": "CarePlan",
  "id": "f1a3b5c7-7777-4d8e-8f2a-4b6c8d0e1f3a",
  "meta": { "tag": [ { "system": "{BASE}/fhir/CodeSystem/verification", "code": "manual" } ] },
  "status": "active",
  "intent": "plan",
  "title": "Tratament diabet – noiembrie 2026",
  "subject": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "period": { "start": "2026-11-01" }
}
```

### 7.9 `Provenance`: extragerea și confirmarea

La confirmarea unei ingestii se creează **două** resurse `Provenance`, fiecare cu **toate** resursele create ca `target`.

```json
{
  "resourceType": "Provenance",
  "id": "1b2c3d4e-8888-4f5a-9b6c-7d8e9f0a1b2c",
  "target": [
    { "reference": "DiagnosticReport/0c5b8e1a-2222-4b6f-8a7d-1e2f3a4b5c6d" },
    { "reference": "Observation/9b7e5c3a-4444-4d2e-8f1a-6b5c4d3e2f10" }
  ],
  "recorded": "2026-10-12T09:16:02Z",
  "activity": { "coding": [ { "system": "{BASE}/fhir/CodeSystem/provenance-activity", "code": "extraction" } ] },
  "agent": [ {
    "type": { "coding": [ { "system": "http://terminology.hl7.org/CodeSystem/provenance-participant-type", "code": "assembler" } ] },
    "who": { "reference": "Device/2c3d4e5f-9999-4a6b-8c7d-9e0f1a2b3c4d" }
  } ],
  "entity": [ { "role": "source", "what": { "reference": "DocumentReference/a1d3c6e2-1111-4a8e-9f0b-5e2c3b4d5a6f" } } ]
}
```

A doua `Provenance` are `activity = verification`, `agent.type = verifier`, `agent.who = Patient/…` și `recorded` = momentul confirmării.

Resursele create manual au o singură `Provenance`, cu `activity = manual-entry`, `agent.type = author`, `agent.who = Patient/…`.

### 7.10 `Device`: componentele software

```json
{
  "resourceType": "Device",
  "id": "2c3d4e5f-9999-4a6b-8c7d-9e0f1a2b3c4d",
  "deviceName": [ { "name": "Patient ingestion pipeline", "type": "model-name" } ],
  "type": { "text": "software" },
  "version": [
    { "type": { "text": "app" }, "value": "1.0.0" },
    { "type": { "text": "ocr-engine" }, "value": "mlkit-text-recognition <versiunea>" },
    { "type": { "text": "extractor" }, "value": "0.9.0" },
    { "type": { "text": "catalog" }, "value": "2026.11.1" }
  ]
}
```

Se creează un `Device` nou doar când se schimbă combinația de versiuni (ING-140); altfel se refolosește.

### 7.11 `Consent`

```json
{
  "resourceType": "Consent",
  "id": "3d4e5f6a-aaaa-4b7c-8d9e-0f1a2b3c4d5e",
  "status": "active",
  "scope": { "coding": [ { "system": "http://terminology.hl7.org/CodeSystem/consentscope", "code": "patient-privacy" } ] },
  "category": [ { "coding": [ { "system": "http://loinc.org", "code": "59284-0", "display": "Patient Consent" } ] } ],
  "patient": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" },
  "dateTime": "2026-11-01T09:00:00Z",
  "policy": [ { "uri": "{BASE}/policy/privacy-notice/1.0.0/ro" } ],
  "provision": { "type": "permit" }
}
```

Câte un `Consent` pe fiecare acord: prelucrarea datelor de sănătate, confirmarea vârstei de 18 ani, termenii. La retragere: `status = inactive`, iar aplicația oprește prelucrarea (doc. 08).

### 7.12 `AuditEvent`

```json
{
  "resourceType": "AuditEvent",
  "id": "4e5f6a7b-bbbb-4c8d-9e0f-1a2b3c4d5e6f",
  "type": { "system": "{BASE}/fhir/CodeSystem/audit-event-type", "code": "export" },
  "recorded": "2026-11-05T20:00:00Z",
  "outcome": "0",
  "agent": [ { "who": { "reference": "Patient/4f0c2d7e-8a51-4c1e-9d33-2b7f9b1e0a11" }, "requestor": true } ],
  "source": { "observer": { "display": "Patient app" } }
}
```

Evenimente: `export`, `auto-export`, `import`, `unlock-failed`, `pdf-shared`. Fără date medicale în eveniment. Ștergerea totală nu se înregistrează (se șterge totul).

## 8. Validarea pe dispozitiv

Înainte de orice scriere (`RecordStore.putAll`), funcția `FhirValidator.validate(resource)` verifică:

| Regulă | Exemplu de eroare |
|---|---|
| Câmpurile obligatorii din R4 și din profilurile noastre | `Observation` fără `code` sau fără `status` |
| Referințele țintesc resurse existente (sau din aceeași tranzacție) | `DiagnosticReport.result` către un ID inexistent |
| Unitățile marcate UCUM sunt în catalogul UCUM local | `code = "mmol/l"` (trebuie `mmol/L`) |
| Datele au format FHIR valid | `effectiveDateTime = "12.10.2026"` |
| `FhirDecimal` valid | `"5,4"` |
| Fără `interpretation` fără sursă în document | Verificat la construire, în `ingestion` |

Validatorul complet HL7 nu rulează pe telefon; rulează în CI (doc. 09).

## 9. Profilurile și versionarea

| Element | Regulă |
|---|---|
| Ghidul de implementare | Fișiere FSH în `fhir-ig/`, compilate cu SUSHI în CI. Profiluri pentru: `Patient`, `DocumentReference`, `DiagnosticReport`, `Observation`, `MedicationStatement`, `Provenance` |
| `meta.profile` | Canonical cu versiune: `{BASE}/fhir/StructureDefinition/<nume>|1.0.0` |
| Schimbarea unui profil | Versiune nouă a ghidului + migrare de date testată (doc. 02 §9) |
| Extensiile proprii | Definite în ghid (`attachment-sha256`, `source-region`, `handwritten`) |

## 10. Corespondența ciornă → FHIR (rezumat)

Detaliile complete vin în doc. 05. Pe scurt, `ConfirmIngestion` construiește într-o singură tranzacție:

| Din ciornă | Resurse create |
|---|---|
| Documentul | `DocumentReference` (+ `blob` trecut de la `ingestion` la `document`) + textul în `fts_document_text` |
| Laboratorul | `Organization` (existentă sau nouă) |
| Raportul și rândurile | `DiagnosticReport` + câte un `Observation` pe rând confirmat |
| Medicamentele confirmate | `MedicationStatement` (+ `CarePlan`, dacă pacientul le grupează) |
| Notița | `DocumentReference` de tip notă |
| Proveniența | `Device` (refolosit sau nou) + 2 × `Provenance` |
