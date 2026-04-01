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
- **Fase 2** — concorrenza con pattern Monitor (`synchronized`, `wait`/`notify` con `while` non `if`), seguendo le dispense di Azzolini/Lavazza. Completata.
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
`private final int size`, `private int nItem`, `private int totItem`.
Costruttore riceve `size`. Metodi `enqueue(Item)` e `dequeue()` sono `synchronized`,
usano `while` + `wait()` + `notify()`. `isEmpty()` rimosso. Entrambi propagano
`throws InterruptedException`. Getter `getTotItem()` è `synchronized`.

### `producer/Producer`
Campi `final int idProducer`, `int itemCounter`, `final OrderBuffer queue`,
`final Random random`, `final int nItem`. Costruttore riceve `idProducer`, `queue`, `nItem`.
Estende `Thread`.
- `generateItem()` — privato, incrementa `itemCounter`, genera peso random tra 0.5 e 50.0,
  coordinate random 0–99, restituisce `Item`.
- `enqueueItem()` — pubblico, chiama `generateItem()`, stampa l'item, chiama `queue.enqueue()`.
  Propaga `throws InterruptedException`.
- `run()` — `for` da 0 a `nItem`, chiama `enqueueItem()`, termina su `InterruptedException`
  con `break`. Il producer si ferma da solo dopo N item.

### `consumer/Consumer`
Campi `final int idConsumer`, `final OrderBuffer queue`. Costruttore riceve `idConsumer`, `queue`.
Estende `Thread`.
- `dequeueItem()` — pubblico, chiama `queue.dequeue()`, salva in `Item item`, stampa.
  Propaga `throws InterruptedException`.
- `run()` — `while(true)`, chiama `dequeueItem()`, termina su `InterruptedException` con `break`.
  Il consumer gira finché `Simulation` lo interrompe con `interrupt()`.

### `logic/Simulation`
Campi `final OrderBuffer buffer`, `final Producer producer`, `final Consumer consumer`.
Costruttore riceve `size` e `nItem`: crea il buffer, istanzia producer (id=1, con `nItem`)
e consumer (id=1).
- `run()` — avvia entrambi i thread con `start()`, aspetta il producer con `producer.join()`,
  interrompe il consumer con `consumer.interrupt()`, aspetta il consumer con `consumer.join()`.
  Propaga `throws InterruptedException`.

### `cli/Main`
Contiene il `main`. Legge `size` e `nItem` dall'utente tramite `Scanner`, istanzia
`Simulation(size, nItem)`, chiama `simulation.run()`, chiude lo `Scanner`.
Propaga `throws InterruptedException`.

## Decisioni di design
- `main` separato da `Simulation` per responsabilità singola e riusabilità in Fase 3.
- `nItem` passato al costruttore di `Simulation` e da lì a `Producer`: il producer si
  ferma da solo, evitando la race condition sulla terminazione.
- `Simulation.run()` senza parametri: N è già nel producer, `run()` si occupa solo del
  ciclo di vita dei thread.
- Buffer illimitato in Fase 1: la capacità massima è rilevante solo con `wait`/`notify`.
- `OrderBuffer` e non `OrderQueue`: il nome riflette il ruolo (buffer condiviso), non
  la struttura interna.
- Un solo producer e un solo consumer per semplicità; più entità sono possibili in futuro.
- `isEmpty()` rimosso da `OrderBuffer`: la guardia sullo stato del buffer appartiene
  al monitor, non va esposta all'esterno.
- `totItem` in `OrderBuffer` conta il totale degli item inseriti dall'inizio, distinto
  da `nItem` che conta solo quelli attualmente nel buffer.

## Prossimo passo
Da decidere. Ipotesi in discussione: aggiungere una modalità rapida/casuale in cui
`size` e `nItem` vengono generati casualmente senza input utente.

## Fonti
- Dispense Azzolini Riccardo 2020 (appunti corso Lavazza, Università degli Studi dell'Insubria)
- Libro: *Dai fondamenti agli oggetti*
- Design system TUI: `CLAUDE.md` nel progetto `pydf-tool`

## Approccio didattico
Il modello AI guida senza produrre codice già pronto,
salvo blocco esplicito dello studente. In quel caso fornisce il codice con spiegazione
riga per riga. Lo studente riscrive sempre a mano in Eclipse — mai copia-incolla.
Le decisioni di design vengono ragionate prima di scrivere codice.
Fonte autoritativa per le scelte del corso: indicazioni esplicite di Lavazza → dispense → libro.