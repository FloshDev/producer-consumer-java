package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;

public class UnsynchronizedOrderBuffer implements Buffer{
	
	private LinkedList<Item> queue;
	private final int size;
	private int nItem;
	private int totItem;

	public UnsynchronizedOrderBuffer(int size) {
		this.queue = new LinkedList<Item>();
		this.size = size;
		this.nItem = 0;
	}
	
	public int getTotItem() {
		return totItem;
	}

	public void enqueue(Item item) throws InterruptedException {
		if(nItem < size) {
			queue.addLast(item);
			nItem++;
		}
	}
	
	public Item dequeue() throws InterruptedException {
		if(nItem > 0) {
			Item item = queue.removeFirst();
			nItem--;
			return item;
		} else
			return null;
	}

}
