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
- **Fase 2** — concorrenza con pattern Monitor (`synchronized`, `wait`/`notify` con `while` non `if`), seguendo le dispense di Azzolini/Lavazza. Prossima.
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
Buffer condiviso tra producer e consumer. Campo `private LinkedList<Item> queue`.
Costruttore senza parametri. Metodi: `enqueue(Item)`, `dequeue()` restituisce `Item`,
`isEmpty()` restituisce `boolean`. In Fase 2 diventerà un monitor con metodi
`synchronized` e `wait()`/`notify()` dentro `while`.

### `producer/Producer`
Campi `final int idProducer`, `int itemCounter`, `final OrderBuffer buffer`, `final Random random`.
Costruttore riceve `idProducer` e `buffer`.
- `generateItem()` — privato, incrementa `itemCounter`, genera peso random tra 0.5 e 50.0,
  coordinate random 0–99, restituisce `Item`.
- `enqueueItem()` — pubblico, chiama `generateItem()`, passa il risultato a `buffer.enqueue()`,
  stampa l'item prodotto.

### `consumer/Consumer`
Campi `final int idConsumer`, `final OrderBuffer buffer`.
Costruttore riceve `idConsumer` e `buffer`.
- `dequeueItem()` — pubblico, controlla `isEmpty()` prima di estrarre. Se non vuoto estrae
  e stampa. Se vuoto stampa avviso. In Fase 2 il ramo "buffer vuoto" diventerà `wait()`
  dentro `while`.

### `logic/Simulation`
Campi `final OrderBuffer buffer`, `final Producer producer`, `final Consumer consumer`.
Costruttore senza parametri: crea il buffer, istanzia producer (id=1) e consumer (id=1)
passando il buffer a entrambi.
- `run(int n)` — ciclo di N produzioni seguito da ciclo di N consumi.

### `cli/Main`
Contiene il `main`. Legge N da input utente tramite `Scanner`, istanzia `Simulation`,
chiama `simulation.run(n)`.

## Decisioni di design
- `main` separato da `Simulation` per responsabilità singola e riusabilità in Fase 3.
- `run(int n)` invece di N nel costruttore: `Simulation` è un motore riutilizzabile.
- Buffer illimitato in Fase 1: la capacità massima è rilevante solo con `wait`/`notify`.
- `OrderBuffer` e non `OrderQueue`: il nome riflette il ruolo (buffer condiviso), non la struttura interna.
- Un solo producer e un solo consumer in Fase 1: più entità hanno senso solo in Fase 2
  quando l'accesso concorrente al buffer diventa il problema da risolvere.

## Prossimo passo
Fase 2 — introdurre la concorrenza con il pattern Monitor.
- `Producer` e `Consumer` estendono `Thread` (o implementano `Runnable`).
- `OrderBuffer` diventa un monitor: metodi `synchronized`, `wait()`/`notify()` dentro `while`.
- Il buffer avrà una capacità massima configurabile dall'utente.
- `Simulation` gestirà l'avvio e il join dei thread.

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