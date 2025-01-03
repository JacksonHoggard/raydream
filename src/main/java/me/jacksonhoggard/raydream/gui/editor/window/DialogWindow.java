package me.jacksonhoggard.raydream.gui.editor.window;

import me.jacksonhoggard.raydream.SceneManager;
import me.jacksonhoggard.raydream.render.RenderCancelListener;
import me.jacksonhoggard.raydream.util.ProgressListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DialogWindow {

    private static JFrame frame;
    private static String lastDir;

    private static int progress = 0;
    private static final JProgressBar progressBar = new JProgressBar(0, 100);
    private static final ProgressListener progressListener = progress -> {
        DialogWindow.progress = progress;
        progressBar.setValue(progress);
        if(progress >= 100)
            closeFrame();
    };

    public static String openFileChooser(String description, String... extensions) {
        JFileChooser fileChooser = new JFileChooser();
        if(lastDir != null)
            fileChooser.setCurrentDirectory(new File(lastDir));

        FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if(result == JFileChooser.APPROVE_OPTION) {
            lastDir = fileChooser.getSelectedFile().getAbsolutePath();
            return fileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    public static String openFolder(String title) {
        JFileChooser fileChooser = new JFileChooser();
        if(SceneManager.getProjectDir() != null)
            fileChooser.setCurrentDirectory(new File(SceneManager.getProjectDir()));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(title);
        int result = fileChooser.showOpenDialog(null);
        if(result == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile().getAbsolutePath();

        return null;
    }

    public static String openFileSave(String defaultName, String... extensions) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(defaultName, extensions);
        fileChooser.setFileFilter(filter);
        if(SceneManager.getProjectDir() != null)
            fileChooser.setCurrentDirectory(new File(SceneManager.getProjectDir()));

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

    public static void showError(String message, Exception e) {
        closeFrame();
        frame = new JFrame("Error");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.setAlwaysOnTop(true);
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();
        JTextArea textArea = new JTextArea(message + stackTrace);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setForeground(Color.RED);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void showText(String message, int width, int height) {
        closeFrame();
        frame = new JFrame("Info");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLayout(new FlowLayout());
        frame.setAlwaysOnTop(true);
        JLabel label = new JLabel(message, JLabel.CENTER);
        frame.add(label);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void showProgressBar(String title, int width, int height, RenderCancelListener renderCancelListener) {
        closeFrame();
        progress = 0;
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLayout(new FlowLayout());
        frame.setAlwaysOnTop(true);
        progressBar.setValue(progress);
        progressBar.setStringPainted(true);
        frame.add(progressBar);
        frame.setLocationRelativeTo(null);
        JButton closeButton = new JButton("Cancel");
        closeButton.addActionListener(_ -> {
            renderCancelListener.cancel();
            closeFrame();
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                renderCancelListener.cancel();
                closeFrame();
            }
        });
        frame.getContentPane().add(closeButton);
        frame.setVisible(true);
    }

    public static void openImage(String title, String path, int width, int height) {
        closeFrame();
        frame = new JFrame(title);
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
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void closeFrame() {
        if(frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
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
