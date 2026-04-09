package com.github.floshdev.producerconsumer.consumer;

import com.github.floshdev.producerconsumer.model.*;

public class Consumer extends Thread {
	
	private final int idConsumer;
	private final Buffer buffer;
	
	public Consumer(int idConsumer, Buffer queue) {
		this.idConsumer = idConsumer;
		this.buffer = queue;
	}
	
	public void dequeueItem() throws InterruptedException {
		Thread.sleep(1000);
		Item item = buffer.dequeue();
		if (item == null) {
			System.out.println("Consumer " + idConsumer + " not consumed, because buffer is empty");
		} else {
			System.out.println("Consumer " + idConsumer + " consumed: " + item + " | Distance: "
					+ String.format("%.2f", item.getDistance()));
		}
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