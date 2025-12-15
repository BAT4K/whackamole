package com.whackamole;

/**
 * A non-JavaFX class to act as the main entry point for the JAR file.
 * This is a standard workaround for the "JavaFX runtime components are missing"
 * error when launching a fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        WhackAMoleGame.main(args);
    }
}