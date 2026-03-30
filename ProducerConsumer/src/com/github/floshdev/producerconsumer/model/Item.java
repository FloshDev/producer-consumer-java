package com.github.floshdev.producerconsumer.model;

public class Item {

	private final int idItem;
	private final float weight;
	private final Coordinate origin;
	private final Coordinate destination;
	
	public Item(int idItem, float weight, Coordinate origin, Coordinate destination) {
		this.idItem = idItem;
		this.weight = weight;
		this.origin = origin;
		this.destination = destination;
	}

	public int getId() {
		return idItem;
	}

	public float getWeight() {
		return weight;
	}

	public Coordinate getOrigin() {
		return origin;
	}

	public Coordinate getDestination() {
		return destination;
	}
	
	@Override
	public String toString() {
		return "ID Item: " + idItem;
	}
	
}
