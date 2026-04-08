package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.model.Buffer;
import com.github.floshdev.producerconsumer.producer.Producer;
import com.github.floshdev.producerconsumer.consumer.Consumer;

public class Simulation {
	
	private final Buffer buffer;
	private final Producer producer;
	private final Consumer consumer;
	
	public Simulation(Buffer buffer, int nItem) {
		this.buffer = buffer;
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
