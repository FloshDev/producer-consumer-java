package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;

public class OrderBuffer implements Buffer{
	
	private LinkedList<Item> queue;
	private final int size;
	private int nItem;
	private int totItem;

	public OrderBuffer(int size) {
		this.queue = new LinkedList<Item>();
		this.size = size;
		this.nItem = 0;
	}
	
	public synchronized int getTotItem() {
		return totItem;
	}

	public synchronized void enqueue(Item item) throws InterruptedException {
		while(nItem == size) {
			wait();
		}
		totItem++;
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
