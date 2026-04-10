package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.model.Buffer;
import java.util.ArrayList;
import com.github.floshdev.producerconsumer.producer.Producer;
import com.github.floshdev.producerconsumer.consumer.Consumer;

public class Simulation {
	
	private final Buffer buffer;
	private final ArrayList<Producer> producer;
	private final ArrayList<Consumer> consumer;
	
	public Simulation(Buffer buffer, int nProducer, int nConsumer, int nItem) {
	    this.buffer = buffer;
	    this.producer = new ArrayList<Producer>();
	    this.consumer = new ArrayList<Consumer>();

	    for (int i = 0; i < nProducer; i++) {
	        int itemForProducer;
	        if (i == 0) {
	            itemForProducer = (nItem / nProducer) + (nItem % nProducer);
	        } else {
	            itemForProducer = nItem / nProducer;
	        }
	        this.producer.add(new Producer(i + 1, buffer, itemForProducer));
	    }

	    for (int i = 0; i < nConsumer; i++) {
	        this.consumer.add(new Consumer(i + 1, buffer));
	    }
	}
	
	public ArrayList<Consumer> getConsumer() {
		return consumer;
	}

	public void execute() throws InterruptedException {
	    for (Producer p : producer) {
	        p.start();
	    }
	    for (Consumer c : consumer) {
	        c.start();
	    }

	    for (Producer p : producer) {
	        p.join();
	    }

	    while (!buffer.isEmpty()) {
	        Thread.sleep(100);
	    }

	    for (Consumer c : consumer) {
	        c.interrupt();
	    }
	    for (Consumer c : consumer) {
	        c.join();
	    }
	}

}
