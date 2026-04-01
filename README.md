# producer-consumer-java

A didactic simulator of the producer-consumer problem, built for the
Concurrent Programming course at Università degli Studi dell'Insubria.

## What is this

The producer-consumer problem is a classic concurrency challenge: one or more
producers generate data and place it in a shared buffer, while one or more
consumers retrieve and process it. The difficulty lies in coordinating access
to the buffer — preventing overwrites, avoiding reads from an empty buffer,
and ensuring no data is lost.

This application makes that problem tangible. It simulates a shipping order
system where producers generate orders and consumers process them. Users can
select different scenarios — sequential execution, unsynchronized concurrency,
and monitor-based concurrency — and directly observe how each behaves.

## Goals

- Show the difference between sequential and concurrent execution
- Demonstrate what goes wrong without synchronization
- Show how the Monitor pattern solves the problem correctly
- Make abstract concepts observable through a concrete, running simulation

## Usage

Run `Main` in Eclipse or from the command line and select a scenario from
the interactive menu.

## Requirements

- Java 21
- Eclipse (recommended)

## License

MIT