package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;

public class OrderBuffer implements Buffer{
	
	private final LinkedList<Item> queue;
	private final int size;
	private int count;
	private int totItem;

	public OrderBuffer(int size) {
		this.queue = new LinkedList<Item>();
		this.size = size;
		this.count = 0;
	}
	
	public synchronized int getTotItem() {
		return totItem;
	}

	public synchronized void enqueue(Item item) throws InterruptedException {
		while(count == size) {
			wait();
		}
		totItem++;
		queue.addLast(item);
		count++;
		notify();
	}
	
	public synchronized Item dequeue() throws InterruptedException {
		while(count == 0) {
			wait();
		}
		Item item = queue.removeFirst();
		count--;
		notify();
		return item;
	}
	
	public synchronized boolean isEmpty() {
		return count == 0;
	}
	
}
