package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;

public class OrderBuffer {
	
	private LinkedList<Item> queue;

	public OrderBuffer() {
		this.queue = new LinkedList<Item>();
	}
	
	public void enqueue(Item item) {
		queue.addLast(item);
	}
	
	public Item dequeue() {
		return queue.removeFirst();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
}
