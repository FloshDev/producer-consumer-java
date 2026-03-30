package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;

public class OrderQueue {
	
	private LinkedList<Item> queue;

	public OrderQueue() {
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
