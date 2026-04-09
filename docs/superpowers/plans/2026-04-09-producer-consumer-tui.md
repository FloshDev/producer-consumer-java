# Producer-Consumer TUI — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Trasformare il progetto in una TUI universitaria che dimostra i problemi classici della programmazione concorrente, con statistiche di dominio e scenari configurabili.

**Architecture:** TUI basata su ANSI escape codes puri (zero dipendenze esterne). La logica di simulazione viene estesa per supportare N producer + M consumer configurabili. Ogni scenario di concorrenza è implementato come una classe separata sotto `logic/`.

**Tech Stack:** Java 11+, Eclipse plain project, ANSI escape codes, `java.util.concurrent.Semaphore`

---

## File Map

| File | Stato | Responsabilità |
|---|---|---|
| `tui/Ansi.java` | **nuovo** | Costanti colori e box-drawing ANSI |
| `tui/TuiRenderer.java` | **nuovo** | Metodi di layout: box, header, footer, separator |
| `logic/SimulationStats.java` | **nuovo** | Raccolta thread-safe di statistiche di dominio |
| `model/SemaphoreOrderBuffer.java` | **nuovo** | Buffer sincronizzato con `java.util.concurrent.Semaphore` |
| `logic/DeadlockSimulation.java` | **nuovo** | Scenario didattico di deadlock |
| `producer/Producer.java` | **modifica** | Delay configurabile + report stats |
| `consumer/Consumer.java` | **modifica** | Delay configurabile + report stats |
| `logic/Simulation.java` | **modifica** | N producer + M consumer, integra stats |
| `logic/SequentialSimulation.java` | **modifica** | Integra stats |
| `cli/Menu.java` | **modifica** | Usa TuiRenderer, validazione input, nuovi parametri |

---

## Phase 1 — TUI Foundation

**Obiettivo:** Sostituire i `System.out.println` grezzi con un layout strutturato usando ANSI.

**Concetto chiave:** Gli ANSI escape codes sono sequenze speciali che il terminale interpreta come comandi di formattazione. La sequenza base è `\033[<codice>m` dove `\033` è il carattere ESC.

### Task 1.1 — Ansi.java

**Files:**
- Create: `ProducerConsumer/src/com/github/floshdev/producerconsumer/tui/Ansi.java`

- [ ] **Step 1: Crea la classe con le costanti ANSI**

```java
package com.github.floshdev.producerconsumer.tui;

public class Ansi {
    // Reset
    public static final String RESET   = "\033[0m";
    public static final String BOLD    = "\033[1m";

    // Colori (true color RGB — rispetta la palette del design system)
    public static final String ACCENT  = "\033[38;2;232;184;75m";  // #E8B84B ambra
    public static final String TEXT    = "\033[38;2;212;212;212m"; // #D4D4D4 testo normale
    public static final String DIM     = "\033[38;2;122;122;122m"; // #7A7A7A testo secondario
    public static final String BORDER  = "\033[38;2;58;58;58m";    // #3A3A3A bordi
    public static final String ERROR   = "\033[38;2;232;91;75m";   // #E85B4B errore
    public static final String SUCCESS = "\033[38;2;75;232;122m";  // #4BE87A successo

    // Box-drawing Unicode
    public static final String H  = "─";
    public static final String V  = "│";
    public static final String TL = "┌";
    public static final String TR = "┐";
    public static final String BL = "└";
    public static final String BR = "┘";

    // Helper: applica colore e poi reset
    public static String color(String colorCode, String text) {
        return colorCode + text + RESET;
    }
}
```

- [ ] **Step 2: Verifica manuale — aggiungi un main temporaneo e lancia**

```java
// In Main.java, sostituisci temporaneamente con:
System.out.println(Ansi.color(Ansi.ACCENT, "Testo ambra"));
System.out.println(Ansi.color(Ansi.ERROR, "Errore rosso"));
System.out.println(Ansi.color(Ansi.SUCCESS, "Successo verde"));
System.out.println(Ansi.TL + Ansi.H.repeat(10) + Ansi.TR);
```

Atteso: testo colorato e borco box visibili nel terminale (non in Eclipse Console — lancia da terminale esterno).

- [ ] **Step 3: Ripristina Main.java**

---

### Task 1.2 — TuiRenderer.java

**Files:**
- Create: `ProducerConsumer/src/com/github/floshdev/producerconsumer/tui/TuiRenderer.java`

- [ ] **Step 1: Crea la struttura base della classe**

```java
package com.github.floshdev.producerconsumer.tui;

public class TuiRenderer {
    private static final int WIDTH = 58; // larghezza interna del box (esclusi i bordi │)

    // Ripete un carattere n volte
    private static String repeat(String s, int n) {
        return s.repeat(n);
    }

    // Ritorna una stringa allineata a sinistra nella larghezza del box
    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
```

- [ ] **Step 2: Aggiungi appHeader()**

Il layout target è:
```
┌─ ProducerConsumer ──────────────────────────────────┐
│ Concurrent programming simulator                     │
└──────────────────────────────────────────────────────┘
```

```java
public static void appHeader() {
    String title = " ProducerConsumer ";
    int remaining = WIDTH - title.length();
    System.out.println(
        Ansi.BORDER + Ansi.TL + Ansi.H + Ansi.RESET +
        Ansi.BOLD + Ansi.ACCENT + title + Ansi.RESET +
        Ansi.BORDER + repeat(Ansi.H, remaining) + Ansi.TR + Ansi.RESET
    );
    System.out.println(
        Ansi.BORDER + Ansi.V + Ansi.RESET +
        Ansi.DIM + " Concurrent programming simulator" + Ansi.RESET +
        " ".repeat(WIDTH - 33) +
        Ansi.BORDER + Ansi.V + Ansi.RESET
    );
    System.out.println(
        Ansi.BORDER + Ansi.BL + repeat(Ansi.H, WIDTH) + Ansi.BR + Ansi.RESET
    );
    System.out.println();
}
```

- [ ] **Step 3: Aggiungi boxHeader(String title), boxLine(String content), boxFooter()**

```java
public static void boxHeader(String title) {
    String inner = " " + title + " ";
    int remaining = WIDTH - inner.length();
    System.out.println(
        Ansi.BORDER + Ansi.TL + Ansi.H + Ansi.RESET +
        Ansi.ACCENT + inner + Ansi.RESET +
        Ansi.BORDER + repeat(Ansi.H, remaining) + Ansi.TR + Ansi.RESET
    );
}

public static void boxLine(String content) {
    System.out.println(
        Ansi.BORDER + Ansi.V + Ansi.RESET +
        Ansi.TEXT + " " + padRight(content, WIDTH - 1) + Ansi.RESET +
        Ansi.BORDER + Ansi.V + Ansi.RESET
    );
}

public static void boxFooter() {
    System.out.println(
        Ansi.BORDER + Ansi.BL + repeat(Ansi.H, WIDTH) + Ansi.BR + Ansi.RESET
    );
}
```

- [ ] **Step 4: Aggiungi separator() e footer(String hints)**

```java
public static void separator() {
    System.out.println(Ansi.BORDER + repeat(Ansi.H, WIDTH + 2) + Ansi.RESET);
}

public static void footer(String hints) {
    separator();
    System.out.println(Ansi.DIM + hints + Ansi.RESET);
}

public static void error(String msg) {
    System.out.println(Ansi.ERROR + "  ✗ " + msg + Ansi.RESET);
}

public static void success(String msg) {
    System.out.println(Ansi.SUCCESS + "  ✓ " + msg + Ansi.RESET);
}
```

- [ ] **Step 5: Verifica manuale — stampa un box completo**

```java
TuiRenderer.appHeader();
TuiRenderer.boxHeader("Select Mode");
TuiRenderer.boxLine("1. Sequential");
TuiRenderer.boxLine("2. Race Condition");
TuiRenderer.boxLine("3. Monitor (synchronized)");
TuiRenderer.boxFooter();
TuiRenderer.footer("Enter number   Q quit");
```

Atteso: layout come da target qui sopra, con colori corretti nel terminale.

- [ ] **Step 6: Commit**

```bash
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/tui/
git commit -m "feat: add TUI foundation (Ansi constants + TuiRenderer)"
```

---

### Task 1.3 — Menu.java refactor

**Files:**
- Modify: `ProducerConsumer/src/com/github/floshdev/producerconsumer/cli/Menu.java`

- [ ] **Step 1: Sostituisci i println grezzi con TuiRenderer**

Il menu deve mostrare 5 opzioni (aggiungere Semaphore e Deadlock al posto delle opzioni future):

```
┌─ Select Mode ────────────────────────────────────────┐
│  1. Sequential        (single thread, no concurrency)│
│  2. Race Condition    (unsynchronized, shows bugs)   │
│  3. Monitor           (synchronized wait/notify)     │
│  4. Semaphore         (java.util.concurrent)         │
│  5. Deadlock          (educational deadlock demo)    │
└──────────────────────────────────────────────────────┘
```

- [ ] **Step 2: Aggiungi validazione input robusta per Scanner**

Il problema: se l'utente digita una stringa invece di un numero, `in.nextInt()` lancia `InputMismatchException`.

```java
private static int readInt(Scanner in, String prompt) {
    while (true) {
        System.out.print(Ansi.DIM + prompt + Ansi.RESET);
        if (in.hasNextInt()) {
            return in.nextInt();
        }
        in.next(); // scarta il token non valido
        TuiRenderer.error("Please enter a valid number");
    }
}
```

- [ ] **Step 3: Aggiungi richiesta parametri N producer + M consumer + delay**

```java
int nProducers = readInt(in, "Number of producers: ");
int nConsumers = readInt(in, "Number of consumers: ");
int produceDelay = readInt(in, "Production delay (ms): ");
int consumeDelay = readInt(in, "Consumption delay (ms): ");
```

Nota: per ora questi parametri vengono letti ma non ancora usati — li collegherai in Phase 2.

- [ ] **Step 4: Commit**

```bash
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/cli/Menu.java
git commit -m "feat: refactor Menu with TUI layout and input validation"
```

---

## Phase 2 — Multi-actor Simulation

**Obiettivo:** Supportare N producer e M consumer, ciascuno configurabile con delay diverso.

**Concetto chiave:** Con N > 1 producer che scrivono sullo stesso buffer concorrentemente, la necessità di sincronizzazione diventa immediatamente evidente. Con M > 1 consumer puoi osservare la distribuzione del carico.

### Task 2.1 — Producer.java — delay configurabile

**Files:**
- Modify: `ProducerConsumer/src/com/github/floshdev/producerconsumer/producer/Producer.java`

- [ ] **Step 1: Aggiungi campo `produceDelayMs` e aggiorna il costruttore**

```java
private final int produceDelayMs;

public Producer(int idProducer, Buffer queue, int nItem, int produceDelayMs) {
    // ... inizializza produceDelayMs
}
```

Aggiorna `enqueueItem()` a usare `Thread.sleep(produceDelayMs)` invece di 500 hardcoded.

- [ ] **Step 2: Mantieni compatibilità — aggiungi costruttore senza delay (default 500ms)**

```java
public Producer(int idProducer, Buffer queue, int nItem) {
    this(idProducer, queue, nItem, 500);
}
```

- [ ] **Step 3: Aggiorna SequentialSimulation.java** — usa il costruttore esistente (nessuna modifica necessaria).

---

### Task 2.2 — Consumer.java — delay configurabile

**Files:**
- Modify: `ProducerConsumer/src/com/github/floshdev/producerconsumer/consumer/Consumer.java`

- [ ] **Step 1: Stessa struttura di Producer** — aggiungi `consumeDelayMs`, aggiorna costruttore e `dequeueItem()`.

---

### Task 2.3 — Simulation.java — N+M attori

**Files:**
- Modify: `ProducerConsumer/src/com/github/floshdev/producerconsumer/logic/Simulation.java`

- [ ] **Step 1: Sostituisci i campi singoli con liste**

```java
import java.util.ArrayList;
import java.util.List;

public class Simulation {
    private final List<Producer> producers;
    private final List<Consumer> consumers;
    private final Buffer buffer;

    public Simulation(Buffer buffer, int nItem, int nProducers, int nConsumers,
                      int produceDelayMs, int consumeDelayMs) {
        this.buffer = buffer;
        this.producers = new ArrayList<>();
        this.consumers = new ArrayList<>();

        int itemsPerProducer = nItem / nProducers;
        for (int i = 1; i <= nProducers; i++)
            producers.add(new Producer(i, buffer, itemsPerProducer, produceDelayMs));
        for (int i = 1; i <= nConsumers; i++)
            consumers.add(new Consumer(i, buffer, consumeDelayMs));
    }
}
```

- [ ] **Step 2: Aggiorna execute() per avviare e terminare tutte le liste**

```java
public void execute() throws InterruptedException {
    consumers.forEach(Thread::start);
    producers.forEach(Thread::start);

    for (Producer p : producers) p.join();

    // Aspetta che il buffer si svuoti prima di interrompere i consumer
    while (!buffer.isEmpty()) Thread.sleep(50);

    consumers.forEach(Thread::interrupt);
    for (Consumer c : consumers) c.join();
}
```

- [ ] **Step 3: Esperimento didattico** — prova queste combinazioni e osserva l'output:
  - 3 producer, 1 consumer, buffer=5 → buffer sempre pieno, consumer fatica
  - 1 producer, 3 consumer, buffer=5 → consumer spesso idle
  - 2 producer, 2 consumer, buffer=2 → equilibrio con monitor buffer

- [ ] **Step 4: Commit**

```bash
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/
git commit -m "feat: support N producers + M consumers with configurable delays"
```

---

## Phase 3 — Statistics System

**Obiettivo:** Raccogliere dati durante la simulazione e mostrarli come un report logistico strutturato.

**Concetto chiave:** Le statistiche devono essere thread-safe — più thread scrivono concorrentemente. Usa `synchronized` sui metodi di aggiornamento, o `AtomicInteger`/`AtomicLong` per i contatori.

### Task 3.1 — SimulationStats.java

**Files:**
- Create: `ProducerConsumer/src/com/github/floshdev/producerconsumer/logic/SimulationStats.java`

- [ ] **Step 1: Crea la classe con i campi di accumulo**

```java
package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.model.Item;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationStats {
    private final AtomicInteger itemsProduced = new AtomicInteger(0);
    private final AtomicInteger itemsConsumed = new AtomicInteger(0);
    private final AtomicInteger itemsLost     = new AtomicInteger(0);

    // Somme cumulative — synchronized perché double non è atomico in Java
    private double totalWeight   = 0;
    private double totalDistance = 0;
    private double maxDistance   = Double.MIN_VALUE;
    private double minDistance   = Double.MAX_VALUE;

    private long startTime;
    private long endTime;
}
```

- [ ] **Step 2: Aggiungi i metodi di registrazione**

```java
public void start() {
    startTime = System.currentTimeMillis();
}

public void end() {
    endTime = System.currentTimeMillis();
}

public synchronized void recordProduced(Item item) {
    itemsProduced.incrementAndGet();
    totalWeight   += item.getWeight();
    double dist    = item.getDistance();
    totalDistance += dist;
    if (dist > maxDistance) maxDistance = dist;
    if (dist < minDistance) minDistance = dist;
}

public synchronized void recordConsumed(Item item) {
    itemsConsumed.incrementAndGet();
}

// Per race condition: item enqueued fallisce silenziosamente
public void recordLost(Item item) {
    itemsLost.incrementAndGet();
}
```

- [ ] **Step 3: Aggiungi printSummary() con layout TUI**

```java
public void printSummary() {
    int produced = itemsProduced.get();
    int consumed = itemsConsumed.get();
    int lost     = itemsLost.get();
    long elapsed = endTime - startTime;

    System.out.println();
    TuiRenderer.boxHeader("Simulation Report");
    TuiRenderer.boxLine(String.format("Elapsed time:     %d ms", elapsed));
    TuiRenderer.boxLine("");
    TuiRenderer.boxLine(String.format("Orders dispatched: %d", produced));
    TuiRenderer.boxLine(String.format("Deliveries done:   %d", consumed));
    if (lost > 0)
        TuiRenderer.boxLine(Ansi.ERROR + String.format("Lost in transit:   %d  ← race condition!", lost) + Ansi.RESET);
    TuiRenderer.boxLine("");
    TuiRenderer.boxLine(String.format("Total cargo:      %.2f kg", totalWeight));
    if (produced > 0) {
        TuiRenderer.boxLine(String.format("Avg distance:     %.2f units", totalDistance / produced));
        TuiRenderer.boxLine(String.format("Max distance:     %.2f units", maxDistance));
        TuiRenderer.boxLine(String.format("Min distance:     %.2f units", minDistance));
        TuiRenderer.boxLine(String.format("Total route:      %.2f units", totalDistance));
    }
    TuiRenderer.boxFooter();
}
```

- [ ] **Step 4: Passa `SimulationStats` a Producer e Consumer**

In `Producer.java`: ricevi `SimulationStats stats` nel costruttore, chiama `stats.recordProduced(item)` in `enqueueItem()`.

In `Consumer.java`: chiama `stats.recordConsumed(item)` in `dequeueItem()`.

In `Simulation.java`: crea `SimulationStats stats = new SimulationStats()`, passala a tutti i producer/consumer, chiama `stats.start()` prima di `execute()` e `stats.end()` + `stats.printSummary()` dopo.

- [ ] **Step 5: Aggiungi rilevamento item persi in UnsynchronizedOrderBuffer**

In `UnsynchronizedOrderBuffer.enqueue()`: quando il buffer è pieno e l'item viene scartato, il chiamante non lo sa. Soluzione: fai tornare un `boolean` e aggiorna l'interfaccia `Buffer`, oppure registra direttamente nella stats.

Approccio più semplice (senza rompere l'interfaccia): aggiungi un contatore `lostItems` in `UnsynchronizedOrderBuffer` con getter pubblico, e leggilo dalla `Simulation` alla fine.

- [ ] **Step 6: Commit**

```bash
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/logic/SimulationStats.java
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/
git commit -m "feat: add SimulationStats with domain-rich report"
```

---

## Phase 4 — Semaphore Buffer

**Obiettivo:** Implementare la sincronizzazione con `java.util.concurrent.Semaphore` come alternativa al `synchronized` + `wait/notify` del Monitor.

**Concetto chiave:** Un semaforo è un contatore con due operazioni atomiche:
- `acquire()` — decrementa il contatore; se è 0, il thread si blocca
- `release()` — incrementa il contatore e sveglia un thread in attesa

Per il bounded buffer servono **tre semafori**:
- `emptySlots` — inizializzato a `size` (quanti slot liberi ci sono)
- `fullSlots` — inizializzato a `0` (quanti item ci sono da consumare)
- `mutex` — inizializzato a `1` (semaforo binario per proteggere la LinkedList)

### Task 4.1 — SemaphoreOrderBuffer.java

**Files:**
- Create: `ProducerConsumer/src/com/github/floshdev/producerconsumer/model/SemaphoreOrderBuffer.java`

- [ ] **Step 1: Struttura base con i tre semafori**

```java
package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class SemaphoreOrderBuffer implements Buffer {
    private final LinkedList<Item> queue;
    private final Semaphore emptySlots;
    private final Semaphore fullSlots;
    private final Semaphore mutex;
    private int totItem;

    public SemaphoreOrderBuffer(int size) {
        this.queue      = new LinkedList<>();
        this.emptySlots = new Semaphore(size);
        this.fullSlots  = new Semaphore(0);
        this.mutex      = new Semaphore(1);
    }
}
```

- [ ] **Step 2: Implementa enqueue()**

```java
@Override
public void enqueue(Item item) throws InterruptedException {
    emptySlots.acquire(); // aspetta uno slot libero
    mutex.acquire();      // entra nella sezione critica
    queue.addLast(item);
    totItem++;
    mutex.release();      // esce dalla sezione critica
    fullSlots.release();  // segnala: c'è un item in più
}
```

- [ ] **Step 3: Implementa dequeue() — stesso schema in ordine inverso**

```java
@Override
public Item dequeue() throws InterruptedException {
    fullSlots.acquire(); // aspetta che ci sia almeno un item
    mutex.acquire();
    Item item = queue.removeFirst();
    mutex.release();
    emptySlots.release(); // segnala: c'è uno slot libero in più
    return item;
}
```

- [ ] **Step 4: Implementa isEmpty() e getTotItem()**

Nota: `isEmpty()` richiede il mutex per essere consistente con gli altri thread.

```java
@Override
public boolean isEmpty() {
    mutex.acquireUninterruptibly();
    boolean empty = queue.isEmpty();
    mutex.release();
    return empty;
}
```

- [ ] **Step 5: Aggiungi opzione 4 in Menu.java**

```java
case 4:
    SemaphoreOrderBuffer semaphoreBuffer = new SemaphoreOrderBuffer(size);
    Simulation semaphoreSimulation = new Simulation(semaphoreBuffer, nItem,
        nProducers, nConsumers, produceDelay, consumeDelay);
    semaphoreSimulation.execute();
    break;
```

- [ ] **Step 6: Confronto didattico — osserva nel terminale**

Lancia con gli stessi parametri opzione 3 (Monitor) e opzione 4 (Semaphore). L'output dovrebbe essere identico — ma i meccanismi interni sono diversi. Scrivi nei commenti della classe la differenza concettuale.

- [ ] **Step 7: Commit**

```bash
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/model/SemaphoreOrderBuffer.java
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/cli/Menu.java
git commit -m "feat: add SemaphoreOrderBuffer, add option 4 to menu"
```

---

## Phase 5 — Deadlock Simulation

**Obiettivo:** Dimostrare un deadlock classico nel contesto producer-consumer e mostrare come detectarlo e evitarlo.

**Concetto chiave:** Un deadlock accade quando due (o più) thread si bloccano a vicenda aspettando risorse che l'altro tiene. Condizioni necessarie (Coffman):
1. **Mutual exclusion** — una risorsa può essere tenuta da un solo thread
2. **Hold and wait** — un thread tiene una risorsa mentre aspettane un'altra
3. **No preemption** — le risorse non possono essere forzatamente rilasciate
4. **Circular wait** — c'è un ciclo di dipendenze tra i thread

### Task 5.1 — DeadlockSimulation.java

**Files:**
- Create: `ProducerConsumer/src/com/github/floshdev/producerconsumer/logic/DeadlockSimulation.java`

- [ ] **Step 1: Setup con due buffer come "risorse"**

Scenario: un thread vuole trasferire item da `bufferA` a `bufferB`, un altro da `bufferB` a `bufferA`. Entrambi acquisiscono il lock del buffer sorgente e poi tentano quello destinazione.

```java
package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.tui.TuiRenderer;
import com.github.floshdev.producerconsumer.tui.Ansi;

public class DeadlockSimulation {
    private final Object lockA = new Object();
    private final Object lockB = new Object();

    public void run() throws InterruptedException {
        TuiRenderer.boxHeader("Deadlock Simulation");
        TuiRenderer.boxLine("Thread A will acquire lockA then lockB");
        TuiRenderer.boxLine("Thread B will acquire lockB then lockA");
        TuiRenderer.boxLine("→ circular wait = deadlock");
        TuiRenderer.boxFooter();
        System.out.println();

        Thread threadA = buildThreadA();
        Thread threadB = buildThreadB();

        threadA.start();
        threadB.start();

        // Timeout: dopo 3 secondi il deadlock è evidente
        threadA.join(3000);
        threadB.join(3000);

        if (threadA.isAlive() || threadB.isAlive()) {
            System.out.println(Ansi.ERROR + "DEADLOCK DETECTED — threads are stuck forever" + Ansi.RESET);
            System.out.println(Ansi.DIM + "Interrupting threads to recover..." + Ansi.RESET);
            threadA.interrupt();
            threadB.interrupt();
            threadA.join();
            threadB.join();
            printDeadlockExplanation();
        }
    }
}
```

- [ ] **Step 2: Implementa i due thread**

```java
private Thread buildThreadA() {
    return new Thread(() -> {
        synchronized (lockA) {
            System.out.println(Ansi.ACCENT + "[Thread A] holds lockA — waiting for lockB..." + Ansi.RESET);
            try { Thread.sleep(100); } catch (InterruptedException e) {
                System.out.println(Ansi.DIM + "[Thread A] interrupted" + Ansi.RESET);
                return;
            }
            synchronized (lockB) {
                System.out.println("[Thread A] acquired both locks — DONE");
            }
        }
    }, "Thread-A");
}

private Thread buildThreadB() {
    return new Thread(() -> {
        synchronized (lockB) {
            System.out.println(Ansi.ACCENT + "[Thread B] holds lockB — waiting for lockA..." + Ansi.RESET);
            try { Thread.sleep(100); } catch (InterruptedException e) {
                System.out.println(Ansi.DIM + "[Thread B] interrupted" + Ansi.RESET);
                return;
            }
            synchronized (lockA) {
                System.out.println("[Thread B] acquired both locks — DONE");
            }
        }
    }, "Thread-B");
}
```

- [ ] **Step 3: Aggiungi spiegazione e soluzione**

```java
private void printDeadlockExplanation() {
    System.out.println();
    TuiRenderer.boxHeader("How to prevent this deadlock");
    TuiRenderer.boxLine("Rule: always acquire locks in the SAME ORDER");
    TuiRenderer.boxLine("");
    TuiRenderer.boxLine("Fix: Thread B should acquire lockA before lockB");
    TuiRenderer.boxLine("     Same order as Thread A → no circular wait");
    TuiRenderer.boxLine("");
    TuiRenderer.boxLine("Other strategies:");
    TuiRenderer.boxLine("  • tryLock() with timeout (java.util.concurrent.locks)");
    TuiRenderer.boxLine("  • avoid holding multiple locks simultaneously");
    TuiRenderer.boxLine("  • use a single ordered resource manager");
    TuiRenderer.boxFooter();
}
```

- [ ] **Step 4: Aggiungi opzione 5 in Menu.java**

```java
case 5:
    DeadlockSimulation deadlock = new DeadlockSimulation();
    deadlock.run();
    break;
```

- [ ] **Step 5: Commit**

```bash
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/logic/DeadlockSimulation.java
git add ProducerConsumer/src/com/github/floshdev/producerconsumer/cli/Menu.java
git commit -m "feat: add DeadlockSimulation with prevention explanation"
```

---

## Phase 6 — Polish e Integration

**Obiettivo:** Tying everything together — output coerente, UX del menu, banner post-simulazione.

### Task 6.1 — Output di simulazione strutturato

- [ ] **Step 1: Standardizza il formato di ogni riga di log**

Invece di `System.out.println("Producer 1 produces: " + item)`, usa:

```java
// In Producer.java
System.out.println(
    Ansi.DIM + "[" + Thread.currentThread().getName() + "] " + Ansi.RESET +
    Ansi.ACCENT + "→ produced " + Ansi.RESET +
    Ansi.TEXT + item + Ansi.RESET
);
```

In Consumer.java il formato analogo ma con `←` e colore TEXT (non accent — un solo accent per schermata).

- [ ] **Step 2: Thread naming — rinomina i thread per il log**

In `Producer.java`, aggiungi nel costruttore:
```java
setName("Producer-" + idProducer);
```

In `Consumer.java`:
```java
setName("Consumer-" + idConsumer);
```

- [ ] **Step 3: Separatore prima e dopo la simulazione**

In `Simulation.execute()`, prima di avviare i thread:
```java
System.out.println();
TuiRenderer.separator();
System.out.println(Ansi.DIM + "  Simulation running..." + Ansi.RESET);
TuiRenderer.separator();
System.out.println();
```

- [ ] **Step 4: Chiedi all'utente se vuole fare un'altra simulazione**

Alla fine di `Menu.launch()`, dopo la simulazione:
```java
System.out.println();
System.out.print(Ansi.DIM + "Run another simulation? (y/n): " + Ansi.RESET);
String again = in.next();
if (again.equalsIgnoreCase("y")) {
    Menu.launch(); // ricorsione — semplice, va bene per scopo didattico
}
```

- [ ] **Step 5: Commit finale**

```bash
git add ProducerConsumer/src/
git commit -m "feat: polish output format, thread naming, simulation loop"
```

---

## Riepilogo scenari finali

| Opzione | Buffer | Sync | Cosa osservare |
|---|---|---|---|
| 1. Sequential | Unsynchronized | Nessuna | Ordine perfetto, un thread |
| 2. Race Condition | Unsynchronized | Nessuna | Item persi, count corrotto con N>1 |
| 3. Monitor | OrderBuffer | `synchronized` + `wait/notify` | Correttezza garantita, blocco su full/empty |
| 4. Semaphore | SemaphoreOrderBuffer | `Semaphore` | Identico a Monitor, primitiva diversa |
| 5. Deadlock | — | `synchronized` | Stallo, spiegazione e prevenzione |

---

## Note per sessioni future

- Il progetto è un Eclipse plain project (no Maven/Gradle) — non aggiungere dipendenze senza prima aggiungere un `pom.xml`.
- L'output ANSI non si vede in Eclipse Console — sempre testare da terminale esterno.
- `SimulationStats` usa `synchronized` sui metodi di update: semplice e corretto per questo dominio. Non introdurre `AtomicLong` con `Double.doubleToLongBits` a meno che le stats non diventino un bottleneck (non accadrà).
- L'utente vuole scrivere il codice da solo: questo piano fornisce schemi e interfacce, non implementazioni complete da copiare.
