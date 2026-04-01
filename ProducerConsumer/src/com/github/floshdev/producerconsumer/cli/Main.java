package com.github.floshdev.producerconsumer.cli;

import java.util.Scanner;
import com.github.floshdev.producerconsumer.logic.Simulation;

public class Main {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		Simulation simulation = new Simulation();
		
		System.out.println("Quanti Item si vogliono produrre?");
		System.out.print("Item da produrre: ");
		int nItem = in.nextInt();
		
		simulation.run(nItem);
	}

}
