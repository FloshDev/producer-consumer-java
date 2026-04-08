package com.github.floshdev.producerconsumer.model;

public interface Buffer {

	void enqueue(Item item) throws InterruptedException;
	Item dequeue() throws InterruptedException;
	boolean isEmpty();
	
}
