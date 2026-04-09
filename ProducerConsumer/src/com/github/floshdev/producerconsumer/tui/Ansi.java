package com.github.floshdev.producerconsumer.tui;

public class Ansi {

    private Ansi() {}

    // Reset & stili
    public static final String RESET = "\033[0m";
    public static final String BOLD  = "\033[1m";

    // Colori testo (true color)
    public static final String ACCENT  = "\033[38;2;232;184;75m";
    public static final String TEXT    = "\033[38;2;212;212;212m";
    public static final String DIM     = "\033[38;2;122;122;122m";
    public static final String BORDER  = "\033[38;2;58;58;58m";
    public static final String ERROR   = "\033[38;2;232;91;75m";
    public static final String SUCCESS = "\033[38;2;75;232;122m";

    // Box-drawing
    public static final String H  = "─";
    public static final String V  = "│";
    public static final String TL = "┌";
    public static final String TR = "┐";
    public static final String BL = "└";
    public static final String BR = "┘";
}