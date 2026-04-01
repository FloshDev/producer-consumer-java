package com.github.floshdev.producerconsumer.producer;

import com.github.floshdev.producerconsumer.model.*;
import java.util.Random;

public class Producer extends Thread {
	
	private final int idProducer;
	private int itemCounter;
	private final OrderBuffer queue;
	private final Random random;
	
	public Producer(int idProducer, OrderBuffer queue) {
		this.idProducer = idProducer;
		this.itemCounter = 0;
		this.queue = queue;
		this.random = new Random();
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
		System.out.println("Producer " + idProducer + " ha prodotto: " + item);
		queue.enqueue(item);
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				enqueueItem();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
