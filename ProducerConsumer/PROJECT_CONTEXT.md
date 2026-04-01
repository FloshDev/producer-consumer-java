# Producer-Consumer Java — Project Context

## Repository
`github.com/FloshDev/producer-consumer-java`  
Java 21, Eclipse, GitHub Desktop.

## Scopo
Simulatore didattico del problema produttore-consumatore. Obiettivo finale: una TUI
(Python, `prompt_toolkit` + `rich`, seguendo il design system in `CLAUDE.md`) che wrappa
l'applicazione Java e la rende utilizzabile a scopo didattico per spiegare il problema
in modo visivo e interattivo.

## Fasi di sviluppo
- **Fase 1** — sequenziale, niente thread. Completata.
- **Fase 2** — concorrenza con pattern Monitor (`synchronized`, `wait`/`notify` con `while` non `if`), seguendo le dispense di Azzolini/Lavazza. In corso.
- **Fase 3** — TUI Python che wrappa l'output Java.

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

### `model/OrderBuffer`
Buffer condiviso, implementato come monitor. Campi: `private LinkedList<Item> queue`,
`private final int size`, `private int nItem`. Costruttore riceve `size`.
Metodi `enqueue(Item)` e `dequeue()` sono `synchronized`, usano `while` + `wait()` + `notify()`.
`isEmpty()` rimosso. Entrambi i metodi propagano `throws InterruptedException`.

### `producer/Producer`
Campi `final int idProducer`, `int itemCounter`, `final OrderBuffer queue`, `final Random random`.
Costruttore riceve `idProducer` e `queue`. Estende `Thread`.
- `generateItem()` — privato, incrementa `itemCounter`, genera peso random tra 0.5 e 50.0,
  coordinate random 0–99, restituisce `Item`.
- `enqueueItem()` — pubblico, chiama `generateItem()`, stampa l'item, chiama `queue.enqueue()`.
  Propaga `throws InterruptedException`.
- `run()` — chiama `enqueueItem()` in `while(true)`, termina su `InterruptedException` con `break`.

### `consumer/Consumer`
Campi `final int idConsumer`, `final OrderBuffer queue`. Costruttore riceve `idConsumer` e `queue`.
Estende `Thread`.
- `dequeueItem()` — pubblico, chiama `queue.dequeue()`, salva in `Item item`, stampa.
  Propaga `throws InterruptedException`.
- `run()` — chiama `dequeueItem()` in `while(true)`, termina su `InterruptedException` con `break`.

### `logic/Simulation`
Da riscrivere per Fase 2. Attualmente ancora in forma Fase 1.

### `cli/Main`
Contiene il `main`. Legge N da input utente tramite `Scanner`, istanzia `Simulation`,
chiama `simulation.run(n)`. Da aggiornare per leggere anche la dimensione del buffer.

## Decisioni di design
- `main` separato da `Simulation` per responsabilità singola e riusabilità in Fase 3.
- `run(int n)` invece di N nel costruttore: `Simulation` è un motore riutilizzabile.
- Buffer illimitato in Fase 1: la capacità massima è rilevante solo con `wait`/`notify`.
- `OrderBuffer` e non `OrderQueue`: il nome riflette il ruolo (buffer condiviso), non la struttura interna.
- Un solo producer e un solo consumer in Fase 1: più entità hanno senso solo in Fase 2
  quando l'accesso concorrente al buffer diventa il problema da risolvere.
- `isEmpty()` rimosso da `OrderBuffer`: la guardia sullo stato del buffer appartiene
  al monitor, non va esposta all'esterno.

## Prossimo passo
Riscrivere `Simulation` per Fase 2:
- Il costruttore riceve `size` del buffer.
- `run(int n)` avvia i thread con `start()`, aspetta che il producer abbia prodotto N item,
  interrompe entrambi i thread con `interrupt()`, li aspetta con `join()`.
- Aggiornare `Main` per leggere anche la dimensione del buffer dall'utente.

## Fonti
- Dispense Azzolini Riccardo 2020 (appunti corso Lavazza, Università degli Studi dell'Insubria)
- Libro: *Dai fondamenti agli oggetti*
- Design system TUI: `CLAUDE.md` nel progetto `pydf-tool`

## Approccio didattico
Il modello AI guida senza produrre codice già pronto, salvo blocco esplicito dello studente.
In quel caso fornisce il codice con spiegazione riga per riga. Lo studente riscrive sempre
a mano in Eclipse — mai copia-incolla. Le decisioni di design vengono ragionate prima di
scrivere codice. Fonte autoritativa per le scelte del corso: indicazioni esplicite di
Lavazza → dispense → libro.