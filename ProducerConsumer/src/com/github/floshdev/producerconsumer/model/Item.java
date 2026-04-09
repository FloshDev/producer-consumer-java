package com.github.floshdev.producerconsumer.model;

public class Item {

	private final int idItem;
	private final double weight;
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

	public double getWeight() {
		return weight;
	}

	public Coordinate getOrigin() {
		return origin;
	}

	public Coordinate getDestination() {
		return destination;
	}
	
	public double getDistance() {
		return Math.sqrt(Math.pow((origin.getX() - destination.getX()),2) 
				+ Math.pow((origin.getY() - destination.getY()), 2));
	}
	
	@Override
	public String toString() {
		return "ID Item: " + idItem;
	}
	
}
