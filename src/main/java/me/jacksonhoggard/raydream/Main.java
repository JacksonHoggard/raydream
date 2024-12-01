package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.gui.Window;

public class Main {
    public static void main(String[] args) {
        Window window = new Window();
        window.init();
        window.run();
        window.destroy();
    }
}