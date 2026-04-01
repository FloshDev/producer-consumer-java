package com.github.floshdev.producerconsumer.cli;

import java.util.Scanner;
import com.github.floshdev.producerconsumer.logic.Simulation;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		Scanner in = new Scanner(System.in);
		
		System.out.println("Che dimensione deve avere il buffer?");
		System.out.print("Dimensione del buffer: ");
		int size = in.nextInt();
		
		System.out.print("\n");
		
		System.out.println("Quanti Item si vogliono produrre?");
		System.out.print("Item da produrre: ");
		int nItem = in.nextInt();
		
		Simulation simulation = new Simulation(size, nItem);
		
		simulation.run();
		
		in.close();
	}

}
