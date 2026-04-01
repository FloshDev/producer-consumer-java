# producer-consumer-java

A didactic Java simulator of the producer-consumer problem, built for the
Concurrent Programming course at Università degli Studi dell'Insubria.

## Overview

The application simulates a shipping order system where producers generate
orders and consumers process them through a shared buffer. The project is
structured in three phases of increasing complexity.

## Phases

**Phase 1 — Sequential**  
Single-threaded simulation. Producer fills the buffer, consumer empties it.
No concurrency. Completed.

**Phase 2 — Concurrent**  
Producer and consumer run as parallel threads. The shared buffer is implemented
as a Monitor: all access is synchronized, and threads block and wake via
`wait()`/`notify()`. The buffer has a configurable maximum capacity. Completed.

**Phase 3 — Interactive TUI**  
A Python wrapper built with `prompt_toolkit` and `rich` will provide an
interactive terminal interface to configure and visualize the simulation.
In progress.

## Usage

Run `Main` in Eclipse or from the command line. The program will ask for:
- Buffer capacity
- Number of items to produce

## Structure
```
com.github.floshdev.producerconsumer.model      — Item, Coordinate, OrderBuffer
com.github.floshdev.producerconsumer.producer   — Producer
com.github.floshdev.producerconsumer.consumer   — Consumer
com.github.floshdev.producerconsumer.logic      — Simulation
com.github.floshdev.producerconsumer.cli        — Main
```

## Requirements

- Java 21
- Eclipse (recommended)

## License

MIT
