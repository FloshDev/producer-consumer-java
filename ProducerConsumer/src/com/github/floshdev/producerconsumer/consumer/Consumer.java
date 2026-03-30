package com.github.floshdev.producerconsumer.consumer;

import com.github.floshdev.producerconsumer.model.*;

public class Consumer{
	
	private final int idConsumer;
	private final OrderBuffer queue;
	
	public Consumer(int idConsumer, OrderBuffer queue) {
		this.idConsumer = idConsumer;
		this.queue = queue;
	}
	
	public void dequeueItem() {
		if(!queue.isEmpty()) {
			Item item = queue.dequeue();
			System.out.println("Consumer " + idConsumer + " ha consumato: " + item);
		} else {
			System.out.println("Consumer " + idConsumer + " : buffer vuoto");
		}
	}
	
}