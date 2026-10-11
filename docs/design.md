# Producer-Consumer didattico — Design

Requisiti e decisioni di progettazione. Ogni decisione ha la sua motivazione.

## Requisiti

### Obiettivo

App da terminale didattica per il corso di Programmazione Concorrente (Insubria).
Uno studente la apre, sceglie uno scenario dal menu, lo esegue e vede — numericamente —
cosa succede al problema produttore/consumatore con diverse strategie di sincronizzazione,
comprese quelle sbagliate.

### Scenari richiesti

1. **Sequenziale** — caso base, nessuna concorrenza.
2. **Non sincronizzato** — mostra il problema: elementi persi o duplicati.
3. **Monitor** — `synchronized` + `wait`/`notifyAll`.
4. **Lock e Condition** — `ReentrantLock` + `Condition`.
5. **Coda bloccante** — struttura della libreria standard.
6. **Deadlock** — mostra il deadlock, poi la sua cura.

### Criteri di successo

- I sei scenari si avviano dal menu.
- Gli scenari corretti mostrano prodotti == consumati.
- Lo scenario non sincronizzato mostra elementi persi/duplicati **senza crashare**.
- Il deadlock viene rilevato e poi curato; l'app torna sempre al menu.
- Funziona con qualsiasi numero di producer e consumer (anche più consumer che producer).
- La suite di test passa in modo affidabile (non "a volte").

### Vincoli

- Java 21, Maven, un solo modulo. Package base `producerconsumer`.
- Unica dipendenza: JUnit 5 (solo test). Nessuna dipendenza runtime.
- Codice e identificatori in inglese, testi visibili all'utente in italiano.
- Il nucleo della simulazione non stampa nulla: l'I/O sta solo nella parte TUI.
- Un test concorrente non deve mai poter bloccare la build.
- Il vecchio progetto Eclipse resta recuperabile dal tag `v0-eclipse-tui`.

### TUI

- Java puro con codici ANSI, nessuna libreria.
- Design system personale: accento `#E8B84B`, testo `#D4D4D4`, secondario `#7A7A7A`,
  bordi `#3A3A3A`, errore `#E85B4B`, successo `#4BE87A`. Nessuno sfondo esplicito.
  Box-drawing Unicode, header con nome app, footer con scorciatoie sempre presente.
- Parametri della simulazione modificabili dall'utente.
- Input non valido: messaggio breve in rosso e nuova richiesta.

### Fuori scope

Benchmark, GUI, salvataggio risultati su file.

## Fase 1 — Analisi dei requisiti

### D1. Identificazione degli elementi: contatore globale

**Decisione:** ogni elemento prodotto ha un id unico, generato da un contatore condiviso da tutti
i producer (non un contatore per producer).

**Motivazione:** per misurare persi e duplicati devo distinguere ogni elemento. Con un contatore
per producer due producer generano lo stesso id e un "duplicato" diventa indistinguibile da due
elementi diversi.

**Conseguenze:**
- A fine simulazione si contano tre valori: prodotti, consumati, consumati distinti.
  - persi = prodotti − distinti
  - duplicati = consumati − distinti
- Il contatore è condiviso tra thread → è anch'esso una risorsa concorrente (da risolvere in Fase 5).

### D2. Rilevamento dei blocchi: rilevamento deadlock + timeout

**Decisione:** due meccanismi complementari.
1. `ThreadMXBean.findDeadlockedThreads()` per riconoscere un deadlock vero e dirlo all'utente.
2. Un timeout per ogni scenario come rete di sicurezza: scaduto, i thread vengono interrotti.

**Motivazione:** il rilevamento JVM vede solo l'attesa circolare sui lock. Altri blocchi (es.
consumer che attende per sempre un elemento perso nello scenario non sincronizzato) non sono
deadlock e non verrebbero visti. Il timeout garantisce che l'app torni sempre al menu.

### D3. Test affidabili: invarianti e timeout

**Decisione:**
- Ogni test concorrente ha un timeout: un blocco fa fallire il test, non blocca la build.
- Si testano invarianti (proprietà vere in qualunque interleaving), mai un ordine specifico
  di esecuzione dei thread.

**Motivazione:** l'ordine dei thread lo decide lo scheduler e cambia a ogni esecuzione.
Un test che dipende dall'ordine è flaky. Esempi di invarianti: prodotti == consumati,
nessun id consumato due volte.

**Questione aperta:** come testare comportamenti che *richiedono* un ordine
(es. "take si blocca se il buffer è vuoto")? → Fase 6.
