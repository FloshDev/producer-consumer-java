package com.github.floshdev.producerconsumer.cli;

import java.util.Scanner;
import com.github.floshdev.producerconsumer.logic.Simulation;
import com.github.floshdev.producerconsumer.logic.SequentialSimulation;
import com.github.floshdev.producerconsumer.model.OrderBuffer;
import com.github.floshdev.producerconsumer.model.UnsynchronizedOrderBuffer;


public class Menu {

	public static void launch() throws InterruptedException {
		Scanner in = new Scanner(System.in);
		boolean valid = false;
		int option;
		
		do {
			System.out.println("SELECT ONE OPTION");
			System.out.println("1. Sequential");
			System.out.println("2. Race condition");
			System.out.println("3. Monitor");
			
			System.out.print("Option: ");
			
			option = in.nextInt();
			
			switch(option) {
				case 1:
				case 2:
				case 3:
					valid = true;
					break;
						
				default: System.out.println("Invalid option, retry");
			}
			
		} while(!valid);
		
		System.out.println("How many items do you want to produce?");
		System.out.print("Items: ");
		int nItem = in.nextInt();
		
		System.out.println("What size should the buffer be?");
		System.out.print("Size: ");
		int size = in.nextInt();
		
		switch(option) {
			case 1: UnsynchronizedOrderBuffer sequentialBuffer = new UnsynchronizedOrderBuffer(size);
					SequentialSimulation sequentialSimulation = new SequentialSimulation(sequentialBuffer, nItem);
					sequentialSimulation.sequentialRun();
					break;
			
			case 2: UnsynchronizedOrderBuffer raceconditionBuffer = new UnsynchronizedOrderBuffer(size);
					Simulation raceconditionSimulation = new Simulation(raceconditionBuffer, nItem);
					raceconditionSimulation.run();
					break;
				
			case 3: OrderBuffer monitorBuffer = new OrderBuffer(size);
					Simulation monitorSimulation = new Simulation(monitorBuffer, nItem);
					monitorSimulation.run();
					break;		
		}
		
		in.close();
	}
	
}
