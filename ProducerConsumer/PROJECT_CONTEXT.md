# Producer-Consumer Java — Project Context

## Repository
`github.com/FloshDev/producer-consumer-java`  
Java 21, Eclipse, GitHub Desktop.

## Scopo
Simulatore didattico del problema produttore-consumatore. Obiettivo finale: una TUI
Java con ANSI escape codes puri (zero dipendenze esterne) che rende l'applicazione
visiva e interattiva, dimostrando i problemi classici della concorrenza con statistiche
di dominio (logistica ordini: peso, distanza, item persi).

La TUI Python originariamente pianificata e` stata abbandonata in favore di una TUI
Java nativa con ANSI puri, piu` coerente con il progetto e senza dipendenze esterne.

## Fasi di sviluppo

### Completate
- **Fase 1** — sequenziale, niente thread. Completata.
- **Fase 2** — concorrenza con pattern Monitor (`synchronized`, `wait`/`notify` con
  `while` non `if`), seguendo le dispense di Azzolini/Lavazza. Completata.
- **Fase 3** — CLI Java interattiva con scenari didattici selezionabili dall'utente.
  Completata e corretta (code review aprile 2026 chiusa). Tre scenari implementati:
  1. **Sequenziale** — nessun thread, producer e consumer si alternano in ordine.
  2. **Race condition** — producer e consumer girano in parallelo senza sincronizzazione,
     per mostrare il comportamento caotico o corrotto.
  3. **Monitor** — concorrenza corretta con `synchronized`/`wait`/`notify`, buffer
     rispettato, tutti gli item prodotti vengono consumati.
- **Fase 4 — TUI Foundation** — Completata (aprile 2026).
  - `tui/Ansi.java` — costanti ANSI escape codes: colori true color, stili, box-drawing Unicode.
  - `tui/TuiRenderer.java` — metodi statici per header, box, footer, stats, errori, successi.
  - `cli/Menu.java` — refactor completo: TUI integrata, validazione input robusta con
    `Integer.parseInt` + `nextLine()` (no crash su input non numerici), loop con ritorno
    al menu dopo ogni simulazione, statistiche finali stampate dopo ogni esecuzione.
  - `consumer/Consumer.java` — aggiunto campo `itemConsumed` e getter `getItemConsumed()`.
  - `model/UnsynchronizedOrderBuffer.java` — aggiunto campo `itemLost` e getter `getItemLost()`.
  - `producer/Producer.java` — `Thread.sleep(1000)` spostato prima di `generateItem()`
    (era dopo `enqueue`): corregge l'interleaving delle stampe con buffer size 1.
- **Fase 5 — Multi-actor** — Completata (aprile 2026).
  - `model/OrderBuffer.java` — `notify()` sostituito con `notifyAll()` in `enqueue` e
    `dequeue`: necessario con N producer e M consumer per evitare che un thread svegli
    un altro thread dello stesso tipo invece di quello complementare.
  - `logic/Simulation.java` — refactor completo. Costruttore ora riceve
    `(Buffer buffer, int nProducer, int nConsumer, int nItem)`. Crea internamente
    `ArrayList<Producer>` e `ArrayList<Consumer>`. Distribuzione di `nItem` tra i
    producer: il primo prende `(nItem / nProducer) + (nItem % nProducer)`, gli altri
    `nItem / nProducer`. `execute()` usa for-each per start/join su tutti i producer
    e interrupt/join su tutti i consumer.
  - `cli/Menu.java` — nei case 2 e 3, `readInt` aggiuntivo per `nProducer` e `nConsumer`.
    `clearScreen` + `printHeader` spostati dentro ogni case, dopo le domande di input.
    Aggiunto metodo privato `getTotalConsumed(ArrayList<Consumer>)` che somma
    `getItemConsumed()` su tutti i consumer.

### Problemi aperti (da risolvere in Fase 5 fix)
- **ID item non globale** — `itemCounter` e` locale a ogni producer, quindi producer
  diversi producono item con lo stesso ID. Soluzione pianificata: contatore globale
  thread-safe con `AtomicInteger` passato a ogni producer.
- **Race condition blocca l'applicazione** — con `UnsynchronizedOrderBuffer` e thread
  multipli, `NoSuchElementException` in `dequeue` causa la morte dei consumer thread.
  `execute()` rimane bloccato nel `while(!buffer.isEmpty())` perche` nessun consumer
  e` piu` vivo. Soluzione pianificata: catturare `NoSuchElementException` in
  `UnsynchronizedOrderBuffer.dequeue()` e restituire `null` invece di crashare.

### In sviluppo
- **Fase 6 — Statistics** — `logic/SimulationStats.java` thread-safe. Raccoglie item
  prodotti/consumati/persi, peso totale, distanza media/max/min, tempo di esecuzione.
  Report finale con framing logistico ("Orders dispatched", "Lost in transit").
- **Fase 7 — Semaphore** — `model/SemaphoreOrderBuffer.java` usando
  `java.util.concurrent.Semaphore`. Tre semafori: `emptySlots`, `fullSlots`, `mutex`.
  Opzione 4 nel menu. Comportamento identico a Monitor, primitiva diversa.
- **Fase 8 — Deadlock** — `logic/DeadlockSimulation.java`. Scenario con due lock
  acquisiti in ordine inverso da due thread. Timeout 3s, rilevamento automatico,
  spiegazione delle condizioni di Coffman e strategie di prevenzione.

## Struttura package
```
com.github.floshdev.producerconsumer.model      -> Item, Coordinate, Buffer, OrderBuffer,
                                                   UnsynchronizedOrderBuffer,
                                                   SemaphoreOrderBuffer (da creare)
com.github.floshdev.producerconsumer.producer   -> Producer
com.github.floshdev.producerconsumer.consumer   -> Consumer
com.github.floshdev.producerconsumer.cli        -> Main, Menu
com.github.floshdev.producerconsumer.logic      -> Simulation, SequentialSimulation,
                                                   SimulationStats (da creare),
                                                   DeadlockSimulation (da creare)
com.github.floshdev.producerconsumer.tui        -> Ansi, TuiRenderer
```

## Classi completate

### `model/Coordinate`
Campi `final int x, y`, costruttore, getter, `toString()` restituisce `Coordinate[x=N, y=N]`.

### `model/Item`
Campi `final int idItem`, `double weight`, `Coordinate origin`, `Coordinate destination`.
Costruttore, getter (`getId()` restituisce `idItem`), `toString()` restituisce `"ID Item: " + idItem`.
Metodo `getDistance()` restituisce `double` — distanza euclidea tra `origin` e `destination`
calcolata con `Math.sqrt` sulle differenze di x e y.

### `model/Buffer`
Interfaccia. Dichiara:
- `void enqueue(Item item) throws InterruptedException`
- `Item dequeue() throws InterruptedException` — Javadoc specifica che `OrderBuffer` non
  restituisce mai `null` (blocca con `wait()`), mentre `UnsynchronizedOrderBuffer`
  restituisce `null` se il buffer e` vuoto.
- `boolean isEmpty()`
- `int getTotItem()`
Contratto comune per tutte le implementazioni di buffer.

### `model/OrderBuffer`
Implementa `Buffer`. Buffer condiviso con pattern Monitor. Campi: `private final LinkedList<Item> queue`,
`private final int size`, `private int count`, `private int totItem`.
Costruttore riceve `size`. Metodi `enqueue(Item)`, `dequeue()`, `isEmpty()` e `getTotItem()`
sono tutti `synchronized`. `enqueue` e `dequeue` usano `while` + `wait()` + `notifyAll()`.
`isEmpty()` restituisce `count == 0`.

### `model/UnsynchronizedOrderBuffer`
Implementa `Buffer`. Stessa struttura di `OrderBuffer` ma senza `synchronized`,
`wait`, `notify`. Campi: `count` (item attuali), `totItem` (totale inseriti), `itemLost`
(item scartati perche` il buffer era pieno).
`enqueue` inserisce solo se `count < size`, altrimenti incrementa `itemLost`.
`dequeue` restituisce `null` se il buffer e` vuoto.
`isEmpty()` restituisce `count == 0`.
Getter: `getTotItem()`, `getItemLost()`.
Usata negli scenari sequenziale e race condition.
PROBLEMA APERTO: `dequeue` lancia `NoSuchElementException` con thread multipli —
da correggere catturando l'eccezione e restituendo `null`.

### `producer/Producer`
Campi `final int idProducer`, `int itemCounter`, `final Buffer buffer`,
`final Random random`, `final int nItem`. Costruttore riceve `idProducer`, `buffer`, `nItem`.
Estende `Thread`. Il campo `buffer` e` di tipo `Buffer` (interfaccia).
- `generateItem()` — privato, genera item con peso random tra 0.5 e 50.0 e coordinate random 0-99.
- `enqueueItem()` — pubblico. `Thread.sleep(1000)` prima di `generateItem()` (non dopo
  `enqueue`): garantisce che le stampe rispettino visivamente il buffer size.
  Chiama `buffer.enqueue(item)`, poi stampa "Producer N produces: ID Item: X".
  Propaga `throws InterruptedException`.
- `run()` — `for` da 0 a `nItem`, chiama `enqueueItem()`, termina su `InterruptedException` con `break`.
PROBLEMA APERTO: `itemCounter` e` locale a ogni producer — ID non globale. Da correggere
con `AtomicInteger` condiviso passato nel costruttore.

### `consumer/Consumer`
Campi `final int idConsumer`, `final Buffer buffer`, `int itemConsumed`.
Costruttore riceve `idConsumer`, `buffer`.
Estende `Thread`. Il campo `buffer` e` di tipo `Buffer` (interfaccia).
- `dequeueItem()` — pubblico, `Thread.sleep(1000)` prima di `buffer.dequeue()`.
  Se `item` e` `null` stampa "Consumer N: buffer vuoto, nessun item consumato" e ritorna.
  Altrimenti incrementa `itemConsumed` e stampa con distanza a due decimali.
  Propaga `throws InterruptedException`.
- `run()` — `while(true)`, chiama `dequeueItem()`, termina su `InterruptedException` con `break`.
- Getter: `getItemConsumed()`.

### `logic/Simulation`
Campi `final Buffer buffer`, `final ArrayList<Producer> producer`,
`final ArrayList<Consumer> consumer`.
Costruttore riceve `(Buffer buffer, int nProducer, int nConsumer, int nItem)`.
Distribuisce `nItem` tra i producer: primo prende `(nItem/nProducer) + (nItem%nProducer)`,
gli altri `nItem/nProducer`.
- `execute()` — for-each start su tutti i producer, for-each start su tutti i consumer,
  for-each join su tutti i producer, `while(!buffer.isEmpty())`, for-each interrupt su
  tutti i consumer, for-each join su tutti i consumer.
  Propaga `throws InterruptedException`.
- Getter: `getConsumer()` restituisce `ArrayList<Consumer>`.

### `logic/SequentialSimulation`
Campi `final Producer producer`, `final Consumer consumer`, `final int nItem`.
Costruttore riceve `Buffer buffer` e `int nItem`.
- `sequentialRun()` — `for` da 0 a `nItem`, chiama `producer.enqueueItem()` poi
  `consumer.dequeueItem()` in sequenza. `InterruptedException` catturata internamente.
- Getter: `getConsumer()`, `getProducer()`.

### `tui/Ansi`
Classe non istanziabile (costruttore `private`). Costanti `public static final String`:
- Stili: `RESET`, `BOLD`
- Colori true color (`\033[38;2;R;G;Bm`): `ACCENT` (#E8B84B), `TEXT` (#D4D4D4),
  `DIM` (#7A7A7A), `BORDER` (#3A3A3A), `ERROR` (#E85B4B), `SUCCESS` (#4BE87A)
- Box-drawing Unicode: `H` (─), `V` (│), `TL` (┌), `TR` (┐), `BL` (└), `BR` (┘)

### `tui/TuiRenderer`
Classe non istanziabile (costruttore `private`). Costante `WIDTH = 60`.
Metodi statici pubblici:
- `clearScreen()` — `\033[2J\033[H`
- `printHeader(String title)` — riga orizzontale + titolo centrato + riga orizzontale, in ACCENT+BOLD
- `printBox(String title, String[] lines)` — box con bordi Unicode, titolo nel bordo superiore,
  righe in TEXT
- `printFooter()` — riga orizzontale DIM
- `printError(String message)` — prefisso ✖ in ERROR
- `printSuccess(String message)` — prefisso ✔ in SUCCESS
- `printStats(int produced, int consumed, int lost, long elapsedMs)` — box con riepilogo
  simulazione: item prodotti, consumati, persi, tempo elapsed in ms

### `cli/Menu`
Classe con metodo statico `launch()`. Loop `while(true)` con ritorno al menu dopo ogni
simulazione. `Scanner` interno non chiuso.
Input gestito da metodo privato `readInt(Scanner, String[], String, int, int)`:
usa `nextLine()` + `Integer.parseInt` per evitare crash su input non numerici,
valida il range e stampa errore con `TuiRenderer.printError` in caso di input invalido.
Metodo privato `getTotalConsumed(ArrayList<Consumer>)` somma `getItemConsumed()` su
tutti i consumer della lista.
Statistiche misurate per ogni scenario: `itemLost` da buffer, `itemConsumed` da
`getTotalConsumed`, `elapsed` con `System.currentTimeMillis()`.
- Scenario 1: `UnsynchronizedOrderBuffer` + `SequentialSimulation.sequentialRun()`
- Scenario 2: `UnsynchronizedOrderBuffer` + `Simulation.execute()`, chiede `nProducer`
  e `nConsumer` prima di avviare
- Scenario 3: `OrderBuffer` + `Simulation.execute()`, chiede `nProducer` e `nConsumer`
  prima di avviare
`clearScreen` + `printHeader` dentro ogni case, dopo le domande di input.
Dichiara `throws InterruptedException`.

### `cli/Main`
Minimale. Chiama solo `Menu.launch()`. Dichiara `throws InterruptedException`.

## Decisioni di design
- `main` separato da `Simulation` per responsabilita` singola e riusabilita`.
- `nItem` distribuito da `Simulation` tra i producer: resto assegnato al primo producer
  con divisione intera. `Menu` passa solo il totale, non si occupa della distribuzione.
- `Simulation.execute()` senza parametri: N e` gia` nei producer, `execute()` si occupa
  solo del ciclo di vita dei thread.
- Buffer illimitato in Fase 1: la capacita` massima e` rilevante solo con `wait`/`notify`.
- `OrderBuffer` e non `OrderQueue`: il nome riflette il ruolo (buffer condiviso), non
  la struttura interna.
- `isEmpty()` nell'interfaccia `Buffer`: serve a `Simulation` per sapere quando il
  buffer e` svuotato prima di interrompere i consumer.
- `notifyAll()` invece di `notify()` in `OrderBuffer`: con N producer e M consumer,
  `notify` rischia di svegliare un thread dello stesso tipo invece di quello complementare,
  causando starvation non deterministica. `notifyAll` sveglia tutti, ognuno ricontrolla
  la propria condizione nel `while` e torna in `wait` se non puo` procedere.
- `count` in entrambi i buffer: rinominato da `nItem` per evitare ambiguita` con il
  campo `nItem` di `Producer`.
- `totItem` conta il totale degli item inseriti dall'inizio, distinto da `count`.
- `getTotItem()` nell'interfaccia `Buffer`: necessario per il riepilogo finale su
  riferimento `Buffer`.
- Principio di sostituzione applicato: `Simulation`, `Producer`, `Consumer` lavorano
  su `Buffer` (interfaccia), non sulle implementazioni concrete.
- `getDistance()` in `Item`: proprieta` dell'item, usata dal `Consumer` per l'output.
- `Thread.sleep(1000)` in `Producer` prima di `generateItem()`: garantisce che le stampe
  rispettino visivamente il buffer size.
- `SequentialSimulation` usa `UnsynchronizedOrderBuffer`: `OrderBuffer` con `wait()`
  in assenza di thread causerebbe deadlock immediato.
- `readInt` in `Menu` usa `nextLine()` + `Integer.parseInt`: robusto su input non numerici.
- `Ansi` e `TuiRenderer` non istanziabili: collezioni di costanti e metodi statici.
- Testare la TUI dal terminale esterno — Eclipse Console non interpreta ANSI escape codes.

## Osservazione didattica — limite con 1 producer / 1 consumer
Con un solo producer e un solo consumer, aumentare il buffer size non migliora il
throughput: il collo di bottiglia e` sempre il thread piu` lento. Il tempo di esecuzione
rimane costante indipendentemente da `size`. Con N producer il throughput scala: testato
con 27 producer e 15 consumer su 500 item, il parallelismo e` visibile nell'output e
nel tempo di esecuzione.

## Fonti
- Dispense Azzolini Riccardo 2020 (appunti corso Lavazza, Universita` degli Studi dell'Insubria)
- Libro: *Dai fondamenti agli oggetti*
- Design system TUI: `CLAUDE.md` globale in `~/.claude/CLAUDE.md` (palette colori, regole layout)

## Approccio didattico
Il modello AI guida senza produrre codice gia` pronto, salvo blocco esplicito dello
studente. In quel caso fornisce il codice con spiegazione riga per riga. Lo studente
riscrive sempre a mano in Eclipse — mai copia-incolla. Le decisioni di design vengono
ragionate prima di scrivere codice. Fonte autoritativa per le scelte del corso:
indicazioni esplicite di Lavazza -> dispense -> libro.