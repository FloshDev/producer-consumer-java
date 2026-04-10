package com.github.floshdev.producerconsumer.cli;

import java.util.Scanner;
import java.util.ArrayList;
import com.github.floshdev.producerconsumer.logic.Simulation;
import com.github.floshdev.producerconsumer.consumer.Consumer;
import com.github.floshdev.producerconsumer.logic.SequentialSimulation;
import com.github.floshdev.producerconsumer.model.OrderBuffer;
import com.github.floshdev.producerconsumer.model.UnsynchronizedOrderBuffer;
import com.github.floshdev.producerconsumer.tui.TuiRenderer;

public class Menu {

    public static void launch() throws InterruptedException {
        Scanner in = new Scanner(System.in);

        while (true) {
            TuiRenderer.clearScreen();
            TuiRenderer.printHeader("PRODUCER CONSUMER SIMULATOR");

            int option = readInt(in, new String[]{
                "1. Sequential",
                "2. Race condition",
                "3. Monitor"
            }, "Select scenario:", 1, 3);

            int nItem = readInt(in, new String[]{
                "How many items do you want to produce?"
            }, "Items:", 1, Integer.MAX_VALUE);

            int size = readInt(in, new String[]{
                "What size should the buffer be?"
            }, "Size:", 1, Integer.MAX_VALUE);

            int itemLost = 0;
            int itemConsumed = 0;
            long elapsed = 0;
            long start = System.currentTimeMillis();

            switch (option) {
                case 1:
                    TuiRenderer.clearScreen();
                    TuiRenderer.printHeader("PRODUCER CONSUMER SIMULATOR");
                    UnsynchronizedOrderBuffer sequentialBuffer = new UnsynchronizedOrderBuffer(size);
                    SequentialSimulation sequentialSimulation = new SequentialSimulation(sequentialBuffer, nItem);
                    sequentialSimulation.sequentialRun();
                    elapsed = System.currentTimeMillis() - start;
                    itemLost = sequentialBuffer.getItemLost();
                    itemConsumed = sequentialSimulation.getConsumer().getItemConsumed();
                    break;
                case 2:
                    int nProducerRace = readInt(in, new String[]{"How many producers?"}, "Producers:", 1, Integer.MAX_VALUE);
                    int nConsumerRace = readInt(in, new String[]{"How many consumers?"}, "Consumers:", 1, Integer.MAX_VALUE);
                    TuiRenderer.clearScreen();
                    TuiRenderer.printHeader("PRODUCER CONSUMER SIMULATOR");
                    UnsynchronizedOrderBuffer raceconditionBuffer = new UnsynchronizedOrderBuffer(size);
                    Simulation raceconditionSimulation = new Simulation(raceconditionBuffer, nProducerRace, nConsumerRace, nItem);
                    raceconditionSimulation.execute();
                    elapsed = System.currentTimeMillis() - start;
                    itemLost = raceconditionBuffer.getItemLost();
                    itemConsumed = getTotalConsumed(raceconditionSimulation.getConsumer());
                    break;
                case 3:
                    int nProducerMonitor = readInt(in, new String[]{"How many producers?"}, "Producers:", 1, Integer.MAX_VALUE);
                    int nConsumerMonitor = readInt(in, new String[]{"How many consumers?"}, "Consumers:", 1, Integer.MAX_VALUE);
                    TuiRenderer.clearScreen();
                    TuiRenderer.printHeader("PRODUCER CONSUMER SIMULATOR");
                    OrderBuffer monitorBuffer = new OrderBuffer(size);
                    Simulation monitorSimulation = new Simulation(monitorBuffer, nProducerMonitor, nConsumerMonitor, nItem);
                    monitorSimulation.execute();
                    elapsed = System.currentTimeMillis() - start;
                    itemConsumed = getTotalConsumed(monitorSimulation.getConsumer());
                    break;
            }

            TuiRenderer.printStats(nItem, itemConsumed, itemLost, elapsed);

            int again = readInt(in, new String[]{
                "1. Back to menu",
                "2. Exit"
            }, "Choice:", 1, 2);

            if (again == 2) break;
        }

        TuiRenderer.printFooter();
    }

    private static int readInt(Scanner in, String[] lines, String prompt, int min, int max) {
        while (true) {
            TuiRenderer.printBox("", lines);
            System.out.print(prompt + " ");
            try {
                int value = Integer.parseInt(in.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                TuiRenderer.printError("Insert a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                TuiRenderer.printError("Invalid input, insert a number");
            }
        }
    }
    
    private static int getTotalConsumed(ArrayList<Consumer> consumers) {
        int total = 0;
        for (Consumer c : consumers) {
            total += c.getItemConsumed();
        }
        return total;
    }
    
}