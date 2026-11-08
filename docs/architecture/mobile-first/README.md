# Documentația pentru dezvoltator: aplicația Patient (Kotlin Multiplatform, prima versiune Android)

Acest dosar transformă deciziile de arhitectură în instrucțiuni concrete de implementare. Este scris pentru dezvoltatorul care construiește aplicația. Deciziile în sine și motivele lor sunt în documentele din dosarul părinte și în [DECIZII_ARHITECTURA.html](../DECIZII_ARHITECTURA.html).

**Dacă documentația și codul nu se potrivesc, codul se oprește și se întreabă arhitectura.** Nu se „rezolvă” local o contradicție.

## Cuprins

| Fișier | Conținut | Stare |
|---|---|---|
| [01-structura-proiect.md](01-structura-proiect.md) | Modulele Gradle, pachetele, dependențele permise, regulile de cod | Gata pentru revizuire |
| [02-baza-de-date.md](02-baza-de-date.md) | Schema completă Room KMP: tabele, coloane, indexuri, căutare, ștergere, migrări | Gata pentru revizuire |
| [03-model-fhir.md](03-model-fhir.md) | Clasele Kotlin pentru FHIR R4, serializarea JSON, exemple complete, reguli de corespondență | Gata pentru revizuire |
| [04-securitate-criptare.md](04-securitate-criptare.md) | Ierarhia cheilor, Keystore, parola, cheia de recuperare, formatele criptate, blocarea | Gata pentru revizuire |
| [05-ingestie.md](05-ingestie.md) | Implementarea pipeline-ului document → OCR → extragere → revizuire → salvare | Gata pentru revizuire |
| [06-medicamente-mementouri.md](06-medicamente-mementouri.md) | Medicamente, tratamente, alarme, notificări | Gata pentru revizuire |
| [07-export-import-pdf.md](07-export-import-pdf.md) | Exportul complet criptat, exportul automat, importul, PDF-ul pentru medic | Gata pentru revizuire |
| [08-ecrane-fluxuri.md](08-ecrane-fluxuri.md) | Ecranele și fluxurile de utilizare | Gata pentru revizuire |
| [09-testare-ci.md](09-testare-ci.md) | Teste, fișiere de referință, CI, analiză statică, ordinea de implementare | Gata pentru revizuire |

## Ce construim în prima versiune (rezumat)

- Aplicație pentru **o singură persoană, 18+**, doar pe telefon, **fără server, fără cont, fără Internet**.
- **Fără asistent AI** în prima versiune (ADR-0010 amânat, ADR-0012).
- Funcții: documente medicale, extragerea valorilor din analize (tipărit și scris de mână, EN și RO), notițe, medicamente cu mementouri, grafice de evoluție, export complet criptat, export automat, import, PDF pentru medic, ștergere totală.
- Date modelate în **HL7 FHIR R4** cu LOINC, UCUM și ATC.

## Regulile de aur (nu se încalcă niciodată)

1. **Fără permisiunea `INTERNET`** în manifest și fără nicio bibliotecă care o adaugă. CI verifică manifestul final (ARH-20, ARH-21).
2. **Nicio dată medicală în loguri,** în mesaje de excepție, în rapoarte de crash, în clipboard sau în notificări (SEC-07, SEC-52).
3. **Nicio dependență nouă fără aprobare.** Licențe acceptate: Apache 2.0, MIT, BSD. Fără GPL / AGPL. Fără SDK-uri de analytics, crash reporting sau reclame (ARH-22).
4. **Nicio valoare extrasă automat nu se salvează fără confirmarea pacientului** (ARH-06).
5. **Aplicația nu calculează nimic medical:** fără marcaje „valoare mare/mică” calculate, fără doze, fără interacțiuni (ADR-0005).
6. **Nu scriem criptografie proprie.** Folosim doar Tink, Bouncy Castle (Argon2id) și Android Keystore, exact cum descrie documentul 04 (SEC-20).
7. **Nicio dată în afara zonelor criptate:** nici fișiere temporare, nici miniaturi, nici cache de imagini (SEC-01, SEC-05).
8. **Ștergerea este reală,** nu un marcaj (ARH-09).

## Convenții ale documentației

- Identificatorii de cerințe (`ARH-`, `SEC-`, `DAT-`, `ING-`, `LEG-`) trimit la documentele din dosarul părinte. Codul care implementează o cerință o menționează într-un comentariu scurt, de exemplu `// SEC-04`.
- Numele din cod (clase, tabele, coloane) sunt în **engleză**. Explicațiile sunt în română.
- Valorile marcate **[Necesită răspuns]** depind de decizii încă deschise. Ele sunt definite într-un singur loc în cod, ca să poată fi schimbate ușor.

## Valori provizorii [Necesită răspuns]

| Valoare | Folosită pentru | Valoare provizorie | Termen |
|---|---|---|---|
| Domeniul proiectului `{BASE}` | Sistemele de coduri FHIR proprii, profiluri | `https://fhir.patient.invalid` (constanta `FhirSystems.BASE`) | **Înainte de prima versiune publică.** După lansare, schimbarea cere migrarea datelor tuturor utilizatorilor |
| `applicationId` și pachetul Kotlin | Identitatea aplicației în Google Play | `app.patient` | Înainte de prima încărcare în Google Play; nu se mai poate schimba după aceea |
| Operatorul de date (ORG-2) | Nota de informare, ecranul „Despre” | — | Înainte de beta |
