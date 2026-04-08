package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.model.Buffer;
import com.github.floshdev.producerconsumer.producer.Producer;
import com.github.floshdev.producerconsumer.consumer.Consumer;

public class Simulation {
	
	private final Producer producer;
	private final Consumer consumer;
	private final Buffer buffer;
	
	public Simulation(Buffer buffer, int nItem) {
		this.producer = new Producer(1, buffer, nItem);
		this.consumer = new Consumer(1, buffer);
		this.buffer = buffer;
	}
	
	public void run() throws InterruptedException {
		producer.start();
		consumer.start();
		
		producer.join();
		
		while(!buffer.isEmpty()) {
			Thread.sleep(100);
		}
		
		consumer.interrupt();
		consumer.join();
	}

}
