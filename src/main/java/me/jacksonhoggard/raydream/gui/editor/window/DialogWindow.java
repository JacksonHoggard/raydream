package me.jacksonhoggard.raydream.gui.editor.window;

import me.jacksonhoggard.raydream.util.ProgressListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DialogWindow {

    private static JFrame frame;

    private static int progress = 0;
    private static final JProgressBar progressBar = new JProgressBar(0, 100);
    private static final ProgressListener progressListener = progress -> {
        DialogWindow.progress = progress;
        progressBar.setValue(progress);
        if(progress >= 100) {
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    };

    public static String openFileChooser(String description, String extension) {
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extension);
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if(result == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile().getAbsolutePath();

        return null;
    }

    public static String openFileSave(String defaultName) {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setSelectedFile(new File(defaultName));
        int result = fileChooser.showSaveDialog(null);

        if(result == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile().getAbsolutePath();

        return null;
    }

    public static boolean openConfirmation(String message) {
        int result = JOptionPane.showConfirmDialog(
                null,
                message,
                "Confirm Action",
                JOptionPane.YES_NO_OPTION
        );

        return result == JOptionPane.YES_OPTION;
    }

    public static void showProgressBar(String title, int width, int height) {
        progress = 0;
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLayout(new FlowLayout());
        frame.setAlwaysOnTop(true);
        progressBar.setValue(progress);
        progressBar.setStringPainted(true);
        frame.add(progressBar);
        frame.setVisible(true);
    }

    public static void openImage(String title, String path, int width, int height) {
        JFrame frame = new JFrame(title);
        ImageDisplay panel = new ImageDisplay(path);
        frame.add(panel);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                panel.repaint();  // Repaint the panel when resized
            }
        });
        frame.setVisible(true);
    }

    public static ProgressListener getProgressListener() {
        return progressListener;
    }

    private static class ImageDisplay extends JPanel {
        private final BufferedImage image;
        private final int imgWidth;
        private final int imgHeight;
        ImageDisplay(String path) {
            try {
                image = ImageIO.read(new File(path));
                imgWidth = image.getWidth();
                imgHeight = image.getHeight();
            } catch (IOException e) {
                throw new RuntimeException("Error loading image: ", e);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                int panelWidth = this.getWidth();
                int panelHeight = this.getHeight();

                double aspectRatio = (double) imgWidth / imgHeight;
                int newWidth, newHeight;
                if (panelWidth / (double) panelHeight > aspectRatio) {
                    newHeight = panelHeight;
                    newWidth = (int) (newHeight * aspectRatio);
                } else {
                    newWidth = panelWidth;
                    newHeight = (int) (newWidth / aspectRatio);
                }

                int x = (panelWidth - newWidth) / 2;
                int y = (panelHeight - newHeight) / 2;
                g.drawImage(image, x, y, newWidth, newHeight, null);
            }
        }
    }
}
