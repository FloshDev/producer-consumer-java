package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.model.OrderBuffer;
import com.github.floshdev.producerconsumer.producer.Producer;
import com.github.floshdev.producerconsumer.consumer.Consumer;

public class Simulation {
	
	private final OrderBuffer buffer;
	private final Producer producer;
	private final Consumer consumer;
	
	public Simulation(int size, int nItem) {
		this.buffer = new OrderBuffer(size);
		this.producer = new Producer(1, buffer, nItem);
		this.consumer = new Consumer(1, buffer);
	}
	
	public void run() throws InterruptedException {
		producer.start();
		consumer.start();
		
		producer.join();
		
		consumer.interrupt();
		consumer.join();
	}

}
