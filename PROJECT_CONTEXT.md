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
- **Fase 1** — sequenziale, niente thread. In corso.
- **Fase 2** — concorrenza con pattern Monitor (`synchronized`, `wait`/`notify` con `while` non `if`), seguendo le dispense di Azzolini/Lavazza.
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
Rinominata da `OrderQueue` per chiarire il ruolo: è il buffer condiviso tra producer e
consumer, non la coda dei thread.
Campo `private LinkedList<Item> queue`. Costruttore senza parametri che inizializza la
lista vuota. Metodi: `enqueue(Item)`, `dequeue()` restituisce `Item`, `isEmpty()` restituisce `boolean`.
In Fase 2 diventerà un monitor: metodi `synchronized`, `wait()`/`notify()` dentro `while`.

### `producer/Producer`
Campi `final int idProducer`, `int itemCounter`, `final OrderBuffer buffer`, `final Random random`.
Costruttore riceve `idProducer` e `buffer`.
- `generateItem()` — privato, incrementa `itemCounter`, genera peso random tra 0.5 e 50.0,
  coordinate random 0–99, restituisce `Item`.
- `enqueueItem()` — pubblico, chiama `generateItem()` e passa il risultato a `buffer.enqueue()`.

### `consumer/Consumer`
Campi `final int idConsumer`, `final OrderBuffer buffer`.
Costruttore riceve `idConsumer` e `buffer`.
- `dequeueItem()` — pubblico, controlla `buffer.isEmpty()` prima di estrarre. Se il buffer
  non è vuoto estrae e stampa l'item. Se è vuoto stampa avviso. In Fase 2 il ramo
  "buffer vuoto" diventerà un `wait()` dentro `while`.

## Prossimo passo
Package `logic` — classe `Simulation` che nel `main` crea il buffer, istanzia producer
e consumer, e orchestra i cicli di produzione e consumo in sequenza.

## Fonti
- Dispense Azzolini Riccardo 2020 (appunti corso Lavazza)
- Libro: *Dai fondamenti agli oggetti* (Z-Library)
- Design system TUI: `CLAUDE.md` nel progetto `pydf-tool`

## Come lavorare con Claudio
- Fonte autoritativa per le scelte del corso: indicazioni esplicite di Lavazza → dispense → libro.
- Approccio: Claudio mostra il codice con spiegazione riga per riga, tu lo riscrivi a mano in Eclipse — mai copia-incolla.
- Quando una classe è completata e pushata, aggiorna questo file.
