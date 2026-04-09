# Producer-Consumer Java — Project Context

## Repository
`github.com/FloshDev/producer-consumer-java`  
Java 21, Eclipse, GitHub Desktop.

## Scopo
Simulatore didattico del problema produttore-consumatore. Obiettivo finale: una TUI
(Python, `prompt_toolkit` + `rich`) che wrappa l'applicazione Java e la rende
utilizzabile a scopo didattico per spiegare il problema in modo visivo e interattivo.

## Fasi di sviluppo
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
- **Fase 4** — TUI Python (`prompt_toolkit` + `rich`, seguendo il design system in
  `CLAUDE.md` nel progetto `pydf-tool`) che wrappa e visualizza l'output Java.

## Struttura package
```
com.github.floshdev.producerconsumer.model
com.github.floshdev.producerconsumer.producer
com.github.floshdev.producerconsumer.consumer
com.github.floshdev.producerconsumer.cli
com.github.floshdev.producerconsumer.logic
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
sono tutti `synchronized`. `enqueue` e `dequeue` usano `while` + `wait()` + `notify()`.
`isEmpty()` restituisce `count == 0`.

### `model/UnsynchronizedOrderBuffer`
Implementa `Buffer`. Stessa struttura di `OrderBuffer` ma senza `synchronized`,
`wait`, `notify`. Campo `count` per gli item attuali nel buffer.
`enqueue` inserisce solo se `count < size`, incrementa sia `count` che `totItem`, altrimenti ignora.
`dequeue` restituisce `null` se il buffer e` vuoto.
`isEmpty()` restituisce `count == 0`.
Usata negli scenari sequenziale e race condition.

### `producer/Producer`
Campi `final int idProducer`, `int itemCounter`, `final Buffer buffer`,
`final Random random`, `final int nItem`. Costruttore riceve `idProducer`, `buffer`, `nItem`.
Estende `Thread`. Il campo `buffer` e` di tipo `Buffer` (interfaccia).
- `generateItem()` — privato, genera item con peso random tra 0.5 e 50.0 e coordinate random 0-99.
- `enqueueItem()` — pubblico, chiama `buffer.enqueue(item)`, poi stampa "Producer N produces: ID Item: X".
  La stampa avviene dopo `enqueue` per riflettere il momento in cui l'item e` effettivamente
  entrato nel buffer. `Thread.sleep(500)` dopo la stampa per rallentare la produzione
  e rendere visibile il riempimento del buffer. Propaga `throws InterruptedException`.
- `run()` — `for` da 0 a `nItem`, chiama `enqueueItem()`, termina su `InterruptedException` con `break`.

### `consumer/Consumer`
Campi `final int idConsumer`, `final Buffer buffer`. Costruttore riceve `idConsumer`, `buffer`.
Estende `Thread`. Il campo `buffer` e` di tipo `Buffer` (interfaccia).
- `dequeueItem()` — pubblico, `Thread.sleep(1000)` prima di `buffer.dequeue()`.
  Se `item` e` `null` (buffer vuoto in scenario non sincronizzato) stampa
  "Consumer N: buffer vuoto, nessun item consumato" e ritorna.
  Altrimenti stampa "Consumer N consumed: ID Item: X | Distance: Y" con distanza a due decimali
  tramite `String.format("%.2f", item.getDistance())`. Propaga `throws InterruptedException`.
- `run()` — `while(true)`, chiama `dequeueItem()`, termina su `InterruptedException` con `break`.

### `logic/Simulation`
Campi `final Buffer buffer`, `final Producer producer`, `final Consumer consumer`.
Costruttore riceve `Buffer buffer` e `int nItem`.
- `execute()` — avvia entrambi i thread, aspetta il producer con `producer.join()`,
  poi aspetta che il buffer sia vuoto con `while(!buffer.isEmpty()) { Thread.sleep(100); }`,
  poi interrompe il consumer con `consumer.interrupt()`, poi aspetta con `consumer.join()`.
  Questo garantisce che tutti gli item prodotti vengano consumati prima della terminazione.
  Propaga `throws InterruptedException`.

### `logic/SequentialSimulation`
Campi `final Producer producer`, `final Consumer consumer`, `final int nItem`.
Costruttore riceve `Buffer buffer` e `int nItem`.
- `sequentialRun()` — `for` da 0 a `nItem`, chiama `producer.enqueueItem()` poi
  `consumer.dequeueItem()` in sequenza. `InterruptedException` catturata internamente
  con `catch(InterruptedException e) { Thread.currentThread().interrupt(); }` per
  ripristinare il flag di interruzione.

### `cli/Menu`
Classe con metodo statico `launch()`. `Scanner` interno, non chiuso alla fine
(rimuovere `in.close()` garantisce la riesecuzione senza crash).
Flusso: `do-while` con `switch` accorpato sui casi 1/2/3 per la validazione della scelta,
lettura di `nItem` e `size`, secondo `switch` che crea il buffer corretto e avvia
la simulation giusta:
- Scenario 1: `UnsynchronizedOrderBuffer` + `SequentialSimulation.sequentialRun()`
- Scenario 2: `UnsynchronizedOrderBuffer` + `Simulation.execute()`
- Scenario 3: `OrderBuffer` + `Simulation.execute()`
Nessun `default` nel secondo `switch` — la validazione nel `do-while` garantisce
che `option` sia sempre 1, 2 o 3. Dichiara `throws InterruptedException`.

### `cli/Main`
Minimale. Chiama solo `Menu.launch()`. Dichiara `throws InterruptedException`.

## Decisioni di design
- `main` separato da `Simulation` per responsabilita` singola e riusabilita` in Fase 4.
- `nItem` passato al costruttore di `Simulation` e da li` a `Producer`: il producer si
  ferma da solo, evitando la race condition sulla terminazione.
- `Simulation.execute()` senza parametri: N e` gia` nel producer, `execute()` si occupa
  solo del ciclo di vita dei thread. Rinominato da `run()` per evitare confusione con
  `Thread.run()` — `Simulation` non e` un thread.
- Buffer illimitato in Fase 1: la capacita` massima e` rilevante solo con `wait`/`notify`.
- `OrderBuffer` e non `OrderQueue`: il nome riflette il ruolo (buffer condiviso), non
  la struttura interna.
- `isEmpty()` nell'interfaccia `Buffer`: serve a `Simulation` per sapere quando il
  buffer e` svuotato prima di interrompere il consumer. La guardia interna del monitor
  (`while(count == 0)`) rimane separata — `isEmpty()` e` esposto per coordinazione
  esterna, non per logica interna.
- `count` in `OrderBuffer` e `UnsynchronizedOrderBuffer`: rinominato da `nItem` per
  evitare ambiguita` con il campo `nItem` di `Producer`.
- `totItem` in entrambi i buffer conta il totale degli item inseriti dall'inizio,
  distinto da `count` che conta solo quelli attualmente nel buffer.
- Interfaccia `Buffer` nel package `model`: il contratto e` parte del dominio.
- `getTotItem()` nell'interfaccia `Buffer`: necessario per il riepilogo finale, che
  lavora su riferimento `Buffer` e non sulle implementazioni concrete.
- `Simulation` riceve `Buffer` dall'esterno e lo mantiene come campo: necessario per
  il controllo `isEmpty()` in `execute()`. Principio di sostituzione applicato.
- `Producer` e `Consumer` lavorano su `Buffer` (interfaccia). Campo rinominato
  da `queue` a `buffer` per coerenza con il tipo.
- `getDistance()` in `Item`: proprieta` dell'item, usata dal `Consumer` per l'output.
- Stampa del producer dopo `enqueue`: riflette il momento reale di inserimento nel buffer.
- `Thread.sleep(500)` in `Producer`, `Thread.sleep(1000)` in `Consumer`: il consumer
  e` piu` lento del doppio, rendendo visibile il riempimento del buffer e l'attesa del producer.
- `while(!buffer.isEmpty()) { Thread.sleep(100); }` in `Simulation.execute()`: garantisce
  che tutti gli item vengano consumati prima della terminazione del consumer.
- `SequentialSimulation` usa `UnsynchronizedOrderBuffer`: `OrderBuffer` con `wait()`
  in assenza di thread causerebbe un deadlock immediato.
- `Menu` nel package `cli`: metodo statico `launch()`, gestisce tutto il flusso.
- `do-while` in `Menu`: garantisce che le opzioni vengano mostrate almeno una volta.
- `Main` minimale: chiama solo `Menu.launch()`.
- `weight` in `Item` e` `double` (non `float`): coerente con `getDistance()` che
  restituisce `double`.
- `queue` dichiarato `final` in entrambi i buffer: assegnato nel costruttore e mai
  riassegnato.

## Prossimo passo
Fase 3 completa e corretta. Migliorie opzionali valutabili prima della Fase 4:
- Gestione input non numerico (attualmente crasha con `InputMismatchException`).
- Descrizione testuale degli scenari prima dell'avvio.
- Riepilogo finale (totale item prodotti/consumati, tempo di esecuzione).
- Possibilita` di rieseguire senza riavviare il programma.

## Fonti
- Dispense Azzolini Riccardo 2020 (appunti corso Lavazza, Universita` degli Studi dell'Insubria)
- Libro: *Dai fondamenti agli oggetti*
- Design system TUI: `CLAUDE.md` nel progetto `pydf-tool`

## Approccio didattico
Il modello AI guida senza produrre codice gia` pronto, salvo blocco esplicito dello
studente. In quel caso fornisce il codice con spiegazione riga per riga. Lo studente
riscrive sempre a mano in Eclipse — mai copia-incolla. Le decisioni di design vengono
ragionate prima di scrivere codice. Fonte autoritativa per le scelte del corso:
indicazioni esplicite di Lavazza -> dispense -> libro.