package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.gui.Window;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();
        Window window = null;

        try {
            window = new Window(context);
            window.init();
            window.run();
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (window != null) {
                window.destroy();
            }
            context.shutdown();
        }
    }
}