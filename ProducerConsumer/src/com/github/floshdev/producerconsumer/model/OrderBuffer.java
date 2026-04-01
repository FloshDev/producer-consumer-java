package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;

public class OrderBuffer {
	
	private LinkedList<Item> queue;
	private final int size;
	private int nItem;

	public OrderBuffer(int size) {
		this.queue = new LinkedList<Item>();
		this.size = size;
		this.nItem = 0;
	}
	
	public synchronized void enqueue(Item item) throws InterruptedException {
		while(nItem == size) {
			wait();
		}
		queue.addLast(item);
		nItem++;
		notify();
	}
	
	public synchronized Item dequeue() throws InterruptedException {
		while(nItem == 0) {
			wait();
		}
		Item item = queue.removeFirst();
		nItem--;
		notify();
		return item;
	}
	
}
