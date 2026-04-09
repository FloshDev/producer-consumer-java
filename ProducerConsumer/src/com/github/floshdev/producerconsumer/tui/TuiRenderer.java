package com.github.floshdev.producerconsumer.tui;

public class TuiRenderer {

    private TuiRenderer() {}

    private static final int WIDTH = 60;

    public static void clearScreen() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    public static void printHeader(String title) {
        String line = Ansi.H.repeat(WIDTH);
        int padding = (WIDTH - title.length()) / 2;
        String centered = " ".repeat(padding) + title;

        System.out.println(Ansi.ACCENT + Ansi.BOLD + line + Ansi.RESET);
        System.out.println(Ansi.ACCENT + Ansi.BOLD + centered + Ansi.RESET);
        System.out.println(Ansi.ACCENT + Ansi.BOLD + line + Ansi.RESET);
    }

    public static void printBox(String title, String[] lines) {
        int inner = WIDTH - 2;
        int dashes = inner - title.length() - 2;
        int left = dashes / 2;
        int right = dashes - left;

        String top = Ansi.TL + Ansi.H.repeat(left) + " " + title + " " + Ansi.H.repeat(right) + Ansi.TR;
        String bottom = Ansi.BL + Ansi.H.repeat(inner) + Ansi.BR;

        System.out.println(Ansi.BORDER + top + Ansi.RESET);
        for (String line : lines) {
            System.out.println(Ansi.BORDER + Ansi.V + Ansi.RESET + " " + Ansi.TEXT + line + Ansi.RESET);
        }
        System.out.println(Ansi.BORDER + bottom + Ansi.RESET);
    }

    public static void printFooter() {
        System.out.println(Ansi.DIM + Ansi.H.repeat(WIDTH) + Ansi.RESET);
    }

    public static void printError(String message) {
        System.out.println(Ansi.ERROR + "✖ " + message + Ansi.RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(Ansi.SUCCESS + "✔ " + message + Ansi.RESET);
    }
    
    public static void printStats(int produced, int consumed, int lost, long elapsedMs) {
        printBox("SIMULATION STATS", new String[]{
            "Items produced : " + produced,
            "Items consumed : " + consumed,
            "Items lost     : " + lost,
            "Elapsed time   : " + elapsedMs + " ms"
        });
    }
    
}