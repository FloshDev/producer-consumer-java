package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.model.OrderBuffer;
import com.github.floshdev.producerconsumer.producer.Producer;
import com.github.floshdev.producerconsumer.consumer.Consumer;

public class Simulation {
	
	private final OrderBuffer buffer;
	private final Producer producer;
	private final Consumer consumer;
	
	public Simulation() {
		this.buffer = new OrderBuffer();
		this.producer = new Producer(1, buffer);
		this.consumer = new Consumer(1, buffer);
	}
	
	public void run(int nItem) {
		for(int i = 0; i < nItem; i++) {
			producer.enqueueItem();
		}
		for(int i = 0; i < nItem; i++) {
			consumer.dequeueItem();
		}
	}

}
