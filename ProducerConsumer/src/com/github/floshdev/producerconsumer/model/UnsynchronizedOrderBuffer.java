package com.github.floshdev.producerconsumer.model;

import java.util.LinkedList;

public class UnsynchronizedOrderBuffer implements Buffer{
	
	private final LinkedList<Item> queue;
	private final int size;
	private int count;
	private int totItem;
	private int itemLost;

	public UnsynchronizedOrderBuffer(int size) {
		this.queue = new LinkedList<Item>();
		this.size = size;
		this.count = 0;
		this.itemLost = 0;
	}
	
	public int getTotItem() {
		return totItem;
	}

	public int getItemLost() {
		return itemLost;
	}

	public void enqueue(Item item) throws InterruptedException {
		if(count < size) {
			queue.addLast(item);
			totItem++;
			count++;
		} else {
			itemLost++;
		}
	}
	
	public Item dequeue() throws InterruptedException {
		if(count > 0) {
			Item item = queue.removeFirst();
			count--;
			return item;
		} else
			return null;
	}
	
	public boolean isEmpty() {
		return count == 0;
	}

}
