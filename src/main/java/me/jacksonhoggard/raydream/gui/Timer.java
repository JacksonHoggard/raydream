package me.jacksonhoggard.raydream.gui;

public class Timer {

    private static double lastLoopTime;
    private static double deltaTime;

    public static double getTime() {
        return System.nanoTime() / 1000000000D;
    }

    public static void calculateDeltaTime() {
        double time = getTime();
        deltaTime = time - lastLoopTime;
        lastLoopTime = time;
    }

    public static double getDeltaTime() {
        return deltaTime;
    }

}
