# 06. Medicamente, tratamente și mementouri

## 1. Ce face și ce nu face aplicația

| Face | Nu face (ADR-0005) |
|---|---|
| Păstrează lista medicamentelor, cu schema scrisă de medic sau de pacient | Nu calculează doze |
| Grupează medicamentele în tratamente | Nu verifică interacțiuni între medicamente |
| Amintește la orele stabilite de pacient | Nu recomandă începerea, oprirea sau schimbarea unui medicament |
| Arată medicamentele active și istoricul tratamentelor încheiate | Nu ține jurnalul dozelor luate (decizie 06.11.2026: fără „am luat doza” în prima versiune) |

## 2. Modelul

- Medicament = `MedicationStatement` (doc. 03 §7.7). Tratament = `CarePlan` (doc. 03 §7.8), legat prin `MedicationStatement.basedOn`.
- Index: `idx_medication` (doc. 02 §4.4). Mementouri: tabelul `reminder` + fișierul `alarms.json` (doc. 02 §6.5, §8).

## 3. Operațiile

| Operație | Ce se întâmplă |
|---|---|
| **Adăugare manuală** | Formular: denumire (cu sugestii din catalog, ING-092), concentrație, formă, schema ca text (obligatoriu), orele mementourilor (opțional), data de început, data de sfârșit (opțional), tratamentul (opțional). Etichetă `manual` |
| **Din document** | După confirmarea fiecărui medicament în revizuire (ING-103). Etichetă `confirmed`, `derivedFrom` → documentul |
| **Editare** | Suprascrie resursa (fără istoric). Mementourile se recalculează |
| **Încheiere** | `status = completed`, `effectivePeriod.end` = data aleasă. Mementourile se dezactivează. Medicamentul trece în „Istoric” |
| **Schimbare de tratament** | Pacientul încheie medicamentul vechi și adaugă unul nou (§7.2 din plan). Aplicația oferă scurtătura „Încheie și adaugă altul”, cu câmpurile precompletate |
| **Ștergere** | Definitivă, cu mementourile (doc. 02 §7) |
| **Tratament** | Creare (titlu, perioadă), adăugare de medicamente, încheiere (încheie, la alegere, și medicamentele din el) |

## 4. Mementourile

### 4.1 Programul

- Fiecare medicament activ poate avea 0…6 ore pe zi (`time_of_day`) și zilele săptămânii (`days_mask`).
- Orele vin din `dosage.timing.repeat.timeOfDay`; tabelul `reminder` este sursa pentru programare, iar `MedicationStatement` rămâne sursa pentru export.
- Ora este **ora locală**: mementoul de la 08:00 sună la 08:00 și după schimbarea fusului orar sau a orei de vară.

### 4.2 `alarms.json`

Copia programului, citibilă fără deblocare (doc. 02 §8):

```json
{ "format": 1, "alarms": [ { "id": "b1c2…", "time_of_day": 480, "days_mask": 127 }, { "id": "d3e4…", "time_of_day": 1200, "days_mask": 127 } ] }
```

- **Fără nume de medicamente, fără ID-uri FHIR.** `id` = `reminder.id` (UUID aleator).
- Se rescrie atomic la fiecare schimbare a tabelului `reminder`.

### 4.3 Programarea alarmelor

| Element | Implementare |
|---|---|
| Alarma | `AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, …)` pentru **următoarea** apariție a fiecărei ore distincte. Mai multe medicamente la aceeași oră = o singură alarmă |
| Permisiunea | `SCHEDULE_EXACT_ALARM`. Pe Android 14+ nu este acordată implicit: aplicația explică și deschide `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`. Verificare cu `canScheduleExactAlarms()` |
| Fără permisiune | Revenire la `setWindow` (fereastră de 10 minute), cu mesaj în setări: „Mementourile pot întârzia câteva minute” |
| `USE_EXACT_ALARM` | **Nu se folosește**: politica Google Play o permite doar aplicațiilor de tip ceas sau calendar |
| La declanșare | `BroadcastReceiver` → afișează notificarea → programează următoarea apariție, din `alarms.json` |
| Repornirea telefonului | `RECEIVE_BOOT_COMPLETED` → reprogramare din `alarms.json` (baza este blocată) |
| Schimbarea orei / fusului orar | `ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED` → reprogramare |
| Actualizarea aplicației | `ACTION_MY_PACKAGE_REPLACED` → reprogramare |
| Baterie | Ecran de ajutor pentru telefoanele care opresc aplicațiile în fundal (setările producătorului), cu explicații, fără cereri automate de excepții |

### 4.4 Notificarea (SEC-52)

| Element | Valoare |
|---|---|
| Canal | „Mementouri” / „Reminders”, importanță ridicată |
| Text | **Mereu generic**: titlu „Patient”, text „Ai un memento” / „You have a reminder”. Fără numele medicamentului, nici pe ecranul blocat, nici pe telefonul deblocat |
| Vizibilitate | `VISIBILITY_PRIVATE`, cu versiune publică identică (generică) |
| Atingere | Deschide aplicația → deblocare → ecranul „Mementoul de la 08:00”, cu medicamentele de la acea oră și schema lor |
| Acțiuni | Niciuna în prima versiune (fără jurnal de doze) |
| Permisiune | `POST_NOTIFICATIONS` (Android 13+), cerută când pacientul activează primul memento, nu la pornire |

### 4.5 Cazuri speciale

| Caz | Comportament |
|---|---|
| Medicamentul s-a încheiat | Alarmele lui se elimină din `alarms.json` la încheiere; la deblocare, aplicația verifică și curăță programul (de exemplu pentru data de sfârșit atinsă) |
| Ștergerea totală | Toate alarmele și notificările se anulează (doc. 04 §9) |
| Aplicația blocată la declanșare | Notificarea apare normal (este generică); detaliile apar după deblocare |
| „Când e nevoie” | Fără mementouri |
