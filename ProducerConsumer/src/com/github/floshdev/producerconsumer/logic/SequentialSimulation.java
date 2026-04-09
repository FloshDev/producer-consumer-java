package com.github.floshdev.producerconsumer.logic;

import com.github.floshdev.producerconsumer.model.Buffer;
import com.github.floshdev.producerconsumer.producer.Producer;
import com.github.floshdev.producerconsumer.consumer.Consumer;

public class SequentialSimulation {
	
	private final Producer producer;
	private final Consumer consumer;
	private final int nItem;
	
	public SequentialSimulation(Buffer buffer, int nItem) {
		this.producer = new Producer(1, buffer, nItem);
		this.consumer = new Consumer(1, buffer);
		this.nItem = nItem;
	}
	
	public void sequentialRun() {
		for(int i = 0; i < nItem; i++) {
			try {
				producer.enqueueItem();
				consumer.dequeueItem();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
