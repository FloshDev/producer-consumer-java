package com.github.floshdev.producerconsumer.producer;

import com.github.floshdev.producerconsumer.model.*;
import java.util.Random;

public class Producer extends Thread {
	
	private final int idProducer;
	private int itemCounter;
	private final Buffer queue;
	private final Random random;
	private final int nItem;
	
	public Producer(int idProducer, Buffer queue, int nItem) {
		this.idProducer = idProducer;
		this.itemCounter = 0;
		this.queue = queue;
		this.random = new Random();
		this.nItem = nItem;
	}
	
	private Item generateItem() {
		itemCounter++;
		float weight = 0.5f + random.nextFloat() * 49.5f;
		Coordinate origin = new Coordinate(random.nextInt(100), random.nextInt(100));
		Coordinate destination = new Coordinate(random.nextInt(100), random.nextInt(100));
		return new Item(itemCounter, weight, origin, destination);
	}
	
	public void enqueueItem() throws InterruptedException {
		Item item = generateItem();
		queue.enqueue(item);
		System.out.println("Producer " + idProducer + " produces: " + item);
		Thread.sleep(500);
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
