package com.github.floshdev.producerconsumer.model;

public interface Buffer {

	void enqueue(Item item) throws InterruptedException;
	
	/**
	 * Rimuove e restituisce il prossimo item dal buffer.
	 * OrderBuffer blocca il thread finché un item è disponibile, non restituisce mai null.
	 * UnsynchronizedOrderBuffer restituisce null se il buffer è vuoto.
	 */
	Item dequeue() throws InterruptedException;
	
	boolean isEmpty();
	int getTotItem();
	
}
