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
  Architettura definita, implementazione in corso. Tre scenari previsti:
  1. **Sequenziale** — nessun thread, producer e consumer si alternano in ordine.
  2. **Race condition** — producer e consumer girano in parallelo senza sincronizzazione,
     per mostrare il comportamento caotico o corrotto.
  3. **Monitor** — concorrenza corretta con `synchronized`/`wait`/`notify`, buffer
     rispettato, item tracciati.
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
Campi `final int idItem`, `float weight`, `Coordinate origin`, `Coordinate destination`.
Costruttore, getter (`getId()` restituisce `idItem`), `toString()` restituisce `"ID Item: " + idItem`.
Metodo `getDistance()` restituisce `double` — distanza euclidea tra `origin` e `destination`
calcolata con `Math.sqrt` sulle differenze di x e y.

### `model/Buffer`
Interfaccia. Dichiara `void enqueue(Item item) throws InterruptedException` e
`Item dequeue() throws InterruptedException`. Contratto comune per tutte le
implementazioni di buffer — permette a `Producer`, `Consumer` e `Simulation` di
lavorare senza conoscere l'implementazione concreta.

### `model/OrderBuffer`
Implementa `Buffer`. Buffer condiviso con pattern Monitor. Campi: `private LinkedList<Item> queue`,
`private final int size`, `private int nItem`, `private int totItem`.
Costruttore riceve `size`. Metodi `enqueue(Item)` e `dequeue()` sono `synchronized`,
usano `while` + `wait()` + `notify()`. Entrambi propagano `throws InterruptedException`.
Getter `getTotItem()` è `synchronized`.

### `model/UnsynchronizedOrderBuffer`
Implementa `Buffer`. Stessa struttura di `OrderBuffer` ma senza `synchronized`,
`wait`, `notify`. `enqueue` inserisce solo se `nItem < size`, altrimenti ignora.
`dequeue` restituisce `null` se il buffer è vuoto. Usata nello scenario race condition
per mostrare il comportamento caotico senza sincronizzazione.

### `producer/Producer`
Campi `final int idProducer`, `int itemCounter`, `final Buffer queue`,
`final Random random`, `final int nItem`. Costruttore riceve `idProducer`, `queue`, `nItem`.
Estende `Thread`. Il campo `queue` è ora di tipo `Buffer` (interfaccia).
- `generateItem()` — privato, incrementa `itemCounter`, genera peso random tra 0.5 e 50.0,
  coordinate random 0–99, restituisce `Item`.
- `enqueueItem()` — pubblico, chiama `generateItem()`, stampa l'item, chiama `queue.enqueue()`.
  Propaga `throws InterruptedException`.
- `run()` — `for` da 0 a `nItem`, chiama `enqueueItem()`, termina su `InterruptedException`
  con `break`. Il producer si ferma da solo dopo N item.

### `consumer/Consumer`
Campi `final int idConsumer`, `final Buffer queue`. Costruttore riceve `idConsumer`, `queue`.
Estende `Thread`. Il campo `queue` è ora di tipo `Buffer` (interfaccia).
- `dequeueItem()` — pubblico, chiama `queue.dequeue()`, salva in `Item item`, stampa.
  Propaga `throws InterruptedException`.
- `run()` — `while(true)`, chiama `dequeueItem()`, termina su `InterruptedException` con `break`.
  Il consumer gira finché `Simulation` lo interrompe con `interrupt()`.

### `logic/Simulation`
Campi `final Buffer buffer`, `final Producer producer`, `final Consumer consumer`.
Costruttore riceve `Buffer buffer` e `int nItem`: il buffer arriva già costruito
dall'esterno, `Simulation` non sa quale implementazione concreta sta usando.
Istanzia producer (id=1, con `nItem`) e consumer (id=1).
- `run()` — avvia entrambi i thread con `start()`, aspetta il producer con `producer.join()`,
  interrompe il consumer con `consumer.interrupt()`, aspetta il consumer con `consumer.join()`.
  Propaga `throws InterruptedException`.

### `cli/Main`
Da aggiornare nella Fase 3. Attualmente legge `size` e `nItem` tramite `Scanner`
e avvia `Simulation`. Diventerà minimale: istanzia `Menu` e la delega.

## Decisioni di design
- `main` separato da `Simulation` per responsabilità singola e riusabilità in Fase 4.
- `nItem` passato al costruttore di `Simulation` e da lì a `Producer`: il producer si
  ferma da solo, evitando la race condition sulla terminazione.
- `Simulation.run()` senza parametri: N è già nel producer, `run()` si occupa solo del
  ciclo di vita dei thread.
- Buffer illimitato in Fase 1: la capacità massima è rilevante solo con `wait`/`notify`.
- `OrderBuffer` e non `OrderQueue`: il nome riflette il ruolo (buffer condiviso), non
  la struttura interna.
- `isEmpty()` rimosso da `OrderBuffer`: la guardia sullo stato del buffer appartiene
  al monitor, non va esposta all'esterno.
- `totItem` in `OrderBuffer` conta il totale degli item inseriti dall'inizio, distinto
  da `nItem` che conta solo quelli attualmente nel buffer.
- Interfaccia `Buffer` nel package `model`: il contratto è parte del dominio, non
  dell'infrastruttura. Le implementazioni concrete stanno nello stesso package.
- `Simulation` riceve `Buffer` dall'esterno: chi crea il buffer è `Main` (tramite `Menu`),
  in base allo scenario scelto. Principio di sostituzione applicato.
- `Producer` e `Consumer` lavorano su `Buffer` (interfaccia): non conoscono
  l'implementazione concreta, funzionano con qualsiasi buffer.
- `getDistance()` in `Item`: la distanza è una proprietà dell'item stesso, non
  dell'azione di consegna. Il `Consumer` usa il metodo per costruire l'output.
- `Menu` nel package `cli`: gestisce interazione utente, creazione del buffer corretto
  e avvio di `Simulation`. `Main` resta minimale e la delega.

## Prossimo passo — Fase 3
Implementare `Menu` nel package `cli`. Flusso minimo:
1. Mostrare le tre opzioni di scenario.
2. Leggere la scelta dell'utente.
3. Chiedere `size` e `nItem`.
4. Creare il buffer corretto (`OrderBuffer` per monitor, `UnsynchronizedOrderBuffer`
   per race condition).
5. Istanziare `Simulation` e avviarla.
Dopo il flusso minimo funzionante: aggiungere titolo, descrizione teorica, help.

## Fonti
- Dispense Azzolini Riccardo 2020 (appunti corso Lavazza, Università degli Studi dell'Insubria)
- Libro: *Dai fondamenti agli oggetti*
- Design system TUI: `CLAUDE.md` nel progetto `pydf-tool`

## Approccio didattico
Il modello AI guida senza produrre codice già pronto, salvo blocco esplicito dello
studente. In quel caso fornisce il codice con spiegazione riga per riga. Lo studente
riscrive sempre a mano in Eclipse — mai copia-incolla. Le decisioni di design vengono
ragionate prima di scrivere codice. Fonte autoritativa per le scelte del corso:
indicazioni esplicite di Lavazza → dispense → libro.