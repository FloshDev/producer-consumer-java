package com.github.floshdev.producerconsumer.consumer;

import com.github.floshdev.producerconsumer.model.*;

public class Consumer extends Thread {
	
	private final int idConsumer;
	private final OrderBuffer queue;
	
	public Consumer(int idConsumer, OrderBuffer queue) {
		this.idConsumer = idConsumer;
		this.queue = queue;
	}
	
	public void dequeueItem() throws InterruptedException {
		Item item = queue.dequeue();
		System.out.println("Consumer " + idConsumer + " ha consumato: " + item);
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				dequeueItem();
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
}