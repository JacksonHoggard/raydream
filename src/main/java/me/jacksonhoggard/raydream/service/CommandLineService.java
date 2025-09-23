package me.jacksonhoggard.raydream.service;

import java.io.IOException;
import java.nio.file.Path;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.gui.editor.window.DialogWindow;
import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.util.io.CLSceneReader;
import me.jacksonhoggard.raydream.util.io.CLSceneReader.SceneDetails;
import me.jacksonhoggard.raydream.util.io.CLSceneReader.Settings;

public class CommandLineService {

    private final CLSceneReader sceneReader;

    public CommandLineService() {
        this.sceneReader = new CLSceneReader();
    }

    public void parseArguments(String[] args) {
        for (String arg : args) {
            // Example: handle --help and --version flags
            switch(arg) {
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
                case "--version":
                    System.out.println("RayDream version " + ApplicationConfig.VERSION);
                    System.exit(0);
                    break;
                case "--render":
                    renderCommandLine(args);
                    System.exit(0);
                    break;
            }
        }
    }

    private static void printHelp() {
        System.out.println("RayDream Command Line Options:");
        System.out.println("  --help          Show this help message");
        System.out.println("  --version       Show application version");
        System.out.println("  --render        Render scene from command line");
    }

    private void renderCommandLine(String[] args) {
        // Placeholder for command line rendering logic
        System.out.println("Rendering scene from command line...");

        String scenePath = parseScenePathFromArgs(args);
        if(scenePath == null) {
            System.err.println("Error: No scene file specified. Use --scene <path> to specify a scene.");
            return;
        }

        String outputPath = "output.png"; // Default output path
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("--output")) {
                outputPath = args[i + 1];
                if(!(outputPath.endsWith(".jpg") || outputPath.endsWith(".png") || outputPath.endsWith(".jpeg")))
                    outputPath += ".png";
                break;
            }
        }

        // Setup scene, camera, and rendering parameters from dream file
        SceneDetails sceneDetails;
        try {
            sceneDetails = sceneReader.read(scenePath);
        } catch (IOException e) {
            System.err.println("Error reading scene file: ");
            e.printStackTrace();
            return;
        }

        // Render the scene
        Scene scene = sceneDetails.scene;
        Settings settings = sceneDetails.settings;
        int samples = settings.samples;
        int bounces = settings.bounces;
        int threads = settings.threads;
        int imgWidth = settings.width;
        int imgHeight = settings.height;
        
        DialogWindow.showProgressBar("Render Progress", 250, 70, Scene.getRenderCancelListener());
        try {
            scene.render(outputPath, samples, bounces, threads, DialogWindow.getProgressListener());
        } catch (IOException e) {
            System.err.println("Failed to render scene: " + e.getMessage());
            return;
        }
        if(!Scene.getRenderCancelListener().isCanceled())
            DialogWindow.openImage(Path.of(outputPath).getFileName().toString(), outputPath, imgWidth, imgHeight);
        while(DialogWindow.isOpen()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String parseScenePathFromArgs(String[] args) {
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("--scene")) {
                return args[i + 1];
            }
        }
        return null;
    }
}