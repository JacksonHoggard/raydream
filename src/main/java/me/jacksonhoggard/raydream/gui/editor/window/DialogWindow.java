package me.jacksonhoggard.raydream.gui.editor.window;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class DialogWindow {

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
}
