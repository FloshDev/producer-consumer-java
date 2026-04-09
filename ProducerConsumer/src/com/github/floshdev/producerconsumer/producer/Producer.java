package com.github.floshdev.producerconsumer.producer;

import com.github.floshdev.producerconsumer.model.*;
import java.util.Random;

public class Producer extends Thread {
	
	private final int idProducer;
	private int itemCounter;
	private final Buffer buffer;
	private final Random random;
	private final int nItem;
	
	public Producer(int idProducer, Buffer queue, int nItem) {
		this.idProducer = idProducer;
		this.itemCounter = 0;
		this.buffer = queue;
		this.random = new Random();
		this.nItem = nItem;
	}
	
	private Item generateItem() {
		itemCounter++;
		Coordinate origin = new Coordinate(random.nextInt(100), random.nextInt(100));
		Coordinate destination = new Coordinate(random.nextInt(100), random.nextInt(100));
		return new Item(itemCounter, origin, destination);
	}
	
	public void enqueueItem() throws InterruptedException {
		Thread.sleep(1000);
		Item item = generateItem();
		buffer.enqueue(item);
		System.out.println("Producer " + idProducer + " produces: " + item);
	}
	
	@Override
	public void run() {
		for(int i = 0; i < nItem; i++) {
			try {
				enqueueItem();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
