package me.jacksonhoggard.raydream.gui.editor.window;

import me.jacksonhoggard.raydream.SceneManager;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.render.RenderCancelListener;
import me.jacksonhoggard.raydream.util.Logger;
import me.jacksonhoggard.raydream.util.ProgressListener;

import org.lwjgl.util.tinyfd.TinyFileDialogs;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

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
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

public class DialogWindow {
    private static final Logger logger = ApplicationContext.getInstance().getLoggingService().getLogger(DialogWindow.class);

    private static JFrame frame;
    private static String lastDir;
    private static final boolean IS_MAC = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
    private static volatile boolean macRenderActive = false;
    private static volatile String macRenderTitle = "Render Progress";
    private static volatile RenderCancelListener macRenderCancelListener;

    private static ImageIcon imageIcon;
    private static JLabel imageLabel;

    private static int progress = 0;
    private static JProgressBar progressBar;
    private static final ProgressListener progressListener = new ProgressListener() {
        @Override
        public void progressUpdated(int progress, BufferedImage image) {
            if (IS_MAC) {
            DialogWindow.progress = progress;
            if (progress >= 100) {
                macRenderActive = false;
            }
            return;
        }
        runOnEdtLater(() -> updateProgress(progress, image));
    }
    };

    public static boolean isOpen() {
        return (frame != null && frame.isVisible()) || (IS_MAC && macRenderActive);
    }

    public static String openFileChooser(String description, String... extensions) {
        if (IS_MAC) {
            return openFileChooserTinyFd(description, extensions);
        }
        return runOnEdtResult(() -> openFileChooserSwing(description, extensions));
    }

    public static String openFolder(String title) {
        if (IS_MAC) {
            return openFolderTinyFd(title);
        }
        return runOnEdtResult(() -> openFolderChooserSwing(title));
    }

    public static String openFileSave(String defaultName, String... extensions) {
        if (IS_MAC) {
            return openFileSaveTinyFd(defaultName, extensions);
        }
        return runOnEdtResult(() -> openFileSaveSwing(defaultName, extensions));
    }

    public static boolean openConfirmation(String message) {
        if (IS_MAC) {
            return TinyFileDialogs.tinyfd_messageBox(
                "Confirm Action",
                message,
                "yesno",
                "question",
                false
            );
        }
        return runOnEdtResult(() -> {
            int result = JOptionPane.showConfirmDialog(
                null,
                message,
                "Confirm Action",
                JOptionPane.YES_NO_OPTION
            );
            return result == JOptionPane.YES_OPTION;
        });
    }

    public static void showError(String message, Exception e) {
        logger.error("Error dialog shown: " + message, e);
        if (IS_MAC) {
            TinyFileDialogs.tinyfd_messageBox(
                "Error",
                message + "\n" + e.getMessage(),
                "ok",
                "error",
                false
            );
            return;
        }
        runOnEdt(() -> {
            closeFrameInternal();
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
        });
    }

    public static void showText(String message, int width, int height) {
        if (IS_MAC) {
            TinyFileDialogs.tinyfd_messageBox(
                "Info",
                message,
                "ok",
                "info",
                false
            );
            return;
        }
        runOnEdt(() -> {
            closeFrameInternal();
            frame = new JFrame("Info");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setSize(width, height);
            frame.setLayout(new FlowLayout());
            frame.setAlwaysOnTop(true);
            JLabel label = new JLabel(message, JLabel.CENTER);
            frame.add(label);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void showProgressBar(String title, int width, int height, RenderCancelListener renderCancelListener) {
        if (IS_MAC) {
            macRenderTitle = title;
            macRenderCancelListener = renderCancelListener;
            macRenderActive = true;
            progress = 0;
            return;
        }
        runOnEdt(() -> {
            ensureSwingComponents();
            closeFrameInternal();
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
            frame.getContentPane().add(imageLabel);
            frame.setVisible(true);
        });
    }

    public static void openImage(String title, String path, int width, int height) {
        if (IS_MAC) {
            macRenderActive = false;
            TinyFileDialogs.tinyfd_messageBox(
                "Render Complete",
                "Saved to:\n" + path,
                "ok",
                "info",
                false
            );
            return;
        }
        runOnEdt(() -> {
            closeFrameInternal();
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
        });
    }

    private static void closeFrame() {
        runOnEdt(DialogWindow::closeFrameInternal);
    }

    public static ProgressListener getProgressListener() {
        return progressListener;
    }

    public static boolean isMac() {
        return IS_MAC;
    }

    public static void endMacRender() {
        macRenderActive = false;
        macRenderCancelListener = null;
    }

    public static void renderMacProgressWindow() {
        if (!IS_MAC || !macRenderActive) {
            return;
        }
        ImGui.setNextWindowSize(280, 110);
        ImGui.begin(macRenderTitle, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        ImGui.text("Rendering...");
        ImGui.progressBar(Math.min(progress, 100) / 100.0f, 0, 0);
        if (ImGui.button("Cancel")) {
            if (macRenderCancelListener != null) {
                macRenderCancelListener.cancel();
            }
            macRenderActive = false;
        }
        ImGui.end();
    }

    private static void updateProgress(int progressValue, BufferedImage image) {
        if (frame == null || !frame.isDisplayable()) {
            return;
        }
        ensureSwingComponents();
        DialogWindow.progress = progressValue;
        progressBar.setValue(progressValue);

        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int) (screenSize.width * 0.90); // 90% of screen width
        int maxHeight = (int) (screenSize.height * 0.90); // 90% of screen height

        // Calculate scaled dimensions
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int progressBarHeight = Math.max(progressBar.getSize().height, progressBar.getPreferredSize().height);

        double scale = Math.min((double) maxWidth / imageWidth,
                (double) (maxHeight - progressBarHeight) / imageHeight);

        int scaledWidth = (int) (imageWidth * scale);
        int scaledHeight = (int) (imageHeight * scale);
        int totalHeight = progressBarHeight + scaledHeight;

        // Scale the image to fit the calculated dimensions
        Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        imageIcon.setImage(scaledImage);

        if (!frame.getSize().equals(new Dimension(scaledWidth, totalHeight))) {
            frame.setSize(scaledWidth, totalHeight);
            frame.setLocationRelativeTo(null);
        }

        imageLabel.repaint();

        if (progressValue >= 100) {
            closeFrameInternal();
        }
    }

    private static void closeFrameInternal() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    private static void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute UI action", e);
        }
    }

    private static void runOnEdtLater(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private static void ensureSwingComponents() {
        if (imageIcon == null) {
            imageIcon = new ImageIcon();
        }
        if (imageLabel == null) {
            imageLabel = new JLabel(imageIcon);
        }
        if (progressBar == null) {
            progressBar = new JProgressBar(0, 100);
        }
    }

    private static <T> T runOnEdtResult(Supplier<T> supplier) {
        if (SwingUtilities.isEventDispatchThread()) {
            return supplier.get();
        }
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<RuntimeException> error = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result.set(supplier.get());
                } catch (RuntimeException e) {
                    error.set(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute UI action", e);
        }
        if (error.get() != null) {
            throw error.get();
        }
        return result.get();
    }

    private static String openFileChooserSwing(String description, String... extensions) {
        JFileChooser fileChooser = new JFileChooser();
        if (lastDir != null) {
            fileChooser.setCurrentDirectory(new File(lastDir));
        }

        FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            lastDir = selected.getParent();
            return selected.getAbsolutePath();
        }

        return null;
    }

    private static String openFolderChooserSwing(String title) {
        JFileChooser fileChooser = new JFileChooser();
        if (SceneManager.getProjectDir() != null) {
            fileChooser.setCurrentDirectory(new File(SceneManager.getProjectDir()));
        } else if (lastDir != null) {
            fileChooser.setCurrentDirectory(new File(lastDir));
        }
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(title);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            lastDir = selected.getAbsolutePath();
            return selected.getAbsolutePath();
        }

        return null;
    }

    private static String openFileSaveSwing(String defaultName, String... extensions) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(defaultName, extensions);
        fileChooser.setFileFilter(filter);
        if (SceneManager.getProjectDir() != null) {
            fileChooser.setCurrentDirectory(new File(SceneManager.getProjectDir()));
        } else if (lastDir != null) {
            fileChooser.setCurrentDirectory(new File(lastDir));
        }

        fileChooser.setSelectedFile(new File(defaultName));
        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            lastDir = selected.getParent();
            return selected.getAbsolutePath();
        }

        return null;
    }

    private static String openFileChooserTinyFd(String description, String... extensions) {
        String defaultDir = getDefaultDirectory();
        String[] patterns = buildFilterPatterns(extensions);
        String result;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filterBuffer = patterns != null ? toPointerBuffer(stack, patterns) : null;
            result = TinyFileDialogs.tinyfd_openFileDialog(
                description,
                defaultDir,
                filterBuffer,
                description,
                false
            );
        }
        if (result == null || result.isBlank()) {
            return null;
        }
        File selected = new File(result);
        File parent = selected.getParentFile();
        if (parent != null) {
            lastDir = parent.getAbsolutePath();
        }
        return selected.getAbsolutePath();
    }

    private static String openFolderTinyFd(String title) {
        String defaultDir = getDefaultDirectory();
        String result = TinyFileDialogs.tinyfd_selectFolderDialog(title, defaultDir);
        if (result == null || result.isBlank()) {
            return null;
        }
        lastDir = result;
        return result;
    }

    private static String openFileSaveTinyFd(String defaultName, String... extensions) {
        String defaultDir = getDefaultDirectory();
        String defaultPath = defaultDir != null ? new File(defaultDir, defaultName).getAbsolutePath() : defaultName;
        String[] patterns = buildFilterPatterns(extensions);
        String result;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filterBuffer = patterns != null ? toPointerBuffer(stack, patterns) : null;
            result = TinyFileDialogs.tinyfd_saveFileDialog(
                defaultName,
                defaultPath,
                filterBuffer,
                defaultName
            );
        }
        if (result == null || result.isBlank()) {
            return null;
        }
        File selected = new File(result);
        File parent = selected.getParentFile();
        if (parent != null) {
            lastDir = parent.getAbsolutePath();
        }
        return selected.getAbsolutePath();
    }

    private static String[] buildFilterPatterns(String... extensions) {
        if (extensions == null || extensions.length == 0) {
            return null;
        }
        String[] patterns = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            String ext = extensions[i] == null ? "" : extensions[i].trim();
            if (ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            if (ext.isEmpty()) {
                patterns[i] = "*";
            } else {
                patterns[i] = "*." + ext.toLowerCase(Locale.ROOT);
            }
        }
        return patterns;
    }

    private static String getDefaultDirectory() {
        if (SceneManager.getProjectDir() != null) {
            return SceneManager.getProjectDir();
        }
        return lastDir;
    }

    private static PointerBuffer toPointerBuffer(MemoryStack stack, String[] patterns) {
        PointerBuffer buffer = stack.mallocPointer(patterns.length);
        for (String pattern : patterns) {
            buffer.put(stack.UTF8(pattern));
        }
        buffer.flip();
        return buffer;
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
                logger.error("Error loading image: " + path, e);
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
