package me.jacksonhoggard.raydream.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Application-wide logging system that provides structured logging with different levels.
 * Supports both console output and file output with configurable formatting and colors.
 */
public class Logger {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final boolean COLORS_SUPPORTED = AnsiColors.supportsColors();
    
    private final String name;
    private final LogLevel minimumLevel;
    private final AtomicBoolean enableConsole;
    private final AtomicBoolean enableFile;
    private final AtomicBoolean enableColors;
    private final ConcurrentLinkedQueue<String> logBuffer;

    /**
     * Creates a new logger with the specified name and minimum log level.
     * @param name the logger name (typically the class name)
     * @param minimumLevel the minimum level to log
     * @param enableConsole whether to enable console output
     * @param enableFile whether to enable file output
     */
    public Logger(String name, LogLevel minimumLevel, boolean enableConsole, boolean enableFile) {
        this.name = name;
        this.minimumLevel = minimumLevel;
        this.enableConsole = new AtomicBoolean(enableConsole);
        this.enableFile = new AtomicBoolean(enableFile);
        this.enableColors = new AtomicBoolean(COLORS_SUPPORTED && enableConsole);
        this.logBuffer = new ConcurrentLinkedQueue<>();
    }

    /**
     * Logs a debug message.
     * @param message the message to log
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    /**
     * Logs a debug message with an exception.
     * @param message the message to log
     * @param throwable the exception to log
     */
    public void debug(String message, Throwable throwable) {
        log(LogLevel.DEBUG, message, throwable);
    }

    /**
     * Logs an info message.
     * @param message the message to log
     */
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    /**
     * Logs an info message with an exception.
     * @param message the message to log
     * @param throwable the exception to log
     */
    public void info(String message, Throwable throwable) {
        log(LogLevel.INFO, message, throwable);
    }

    /**
     * Logs a warning message.
     * @param message the message to log
     */
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    /**
     * Logs a warning message with an exception.
     * @param message the message to log
     * @param throwable the exception to log
     */
    public void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    /**
     * Logs an error message.
     * @param message the message to log
     */
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    /**
     * Logs an error message with an exception.
     * @param message the message to log
     * @param throwable the exception to log
     */
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    /**
     * Core logging method that handles the actual logging logic.
     * @param level the log level
     * @param message the message to log
     * @param throwable optional exception to log
     */
    private void log(LogLevel level, String message, Throwable throwable) {
        if (!level.shouldLog(minimumLevel)) {
            return;
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = formatLogEntry(timestamp, level, message, throwable);
        String coloredLogEntry = enableColors.get() ? formatColoredLogEntry(timestamp, level, message, throwable) : logEntry;

        // Add to buffer for potential file writing (always use non-colored version)
        if (enableFile.get()) {
            logBuffer.offer(logEntry);
        }

        // Output to console if enabled (use colored version if colors are enabled)
        if (enableConsole.get()) {
            String outputEntry = enableColors.get() ? coloredLogEntry : logEntry;
            if (level == LogLevel.ERROR || level == LogLevel.WARN) {
                System.err.println(outputEntry);
            } else {
                System.out.println(outputEntry);
            }
        }
    }

    /**
     * Formats a log entry with timestamp, level, logger name, message, and optional exception.
     * @param timestamp the formatted timestamp
     * @param level the log level
     * @param message the log message
     * @param throwable optional exception
     * @return the formatted log entry
     */
    private String formatLogEntry(String timestamp, LogLevel level, String message, Throwable throwable) {
        StringBuilder entry = new StringBuilder();
        entry.append("[").append(timestamp).append("] ");
        entry.append("[").append(level.getDisplayName()).append("] ");
        entry.append("[").append(name).append("] ");
        entry.append(message);

        if (throwable != null) {
            entry.append("\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            entry.append(sw.toString());
        }

        return entry.toString();
    }

    /**
     * Formats a colored log entry with timestamp, level, logger name, message, and optional exception.
     * @param timestamp the formatted timestamp
     * @param level the log level
     * @param message the log message
     * @param throwable optional exception
     * @return the formatted colored log entry
     */
    private String formatColoredLogEntry(String timestamp, LogLevel level, String message, Throwable throwable) {
        StringBuilder entry = new StringBuilder();
        
        // Timestamp in dim gray
        entry.append(AnsiColors.colorize("[", AnsiColors.DIM))
             .append(AnsiColors.colorize(timestamp, AnsiColors.BRIGHT_BLACK))
             .append(AnsiColors.colorize("] ", AnsiColors.DIM));
        
        // Log level with appropriate color
        String levelColor = AnsiColors.getLogLevelColor(level);
        entry.append(AnsiColors.colorize("[", AnsiColors.DIM))
             .append(AnsiColors.colorize(level.getDisplayName(), levelColor, AnsiColors.BOLD))
             .append(AnsiColors.colorize("] ", AnsiColors.DIM));
        
        // Logger name in cyan
        entry.append(AnsiColors.colorize("[", AnsiColors.DIM))
             .append(AnsiColors.colorize(name, AnsiColors.CYAN))
             .append(AnsiColors.colorize("] ", AnsiColors.DIM));
        
        // Message in appropriate color based on level
        if (level == LogLevel.ERROR) {
            entry.append(AnsiColors.colorize(message, AnsiColors.BRIGHT_RED));
        } else if (level == LogLevel.WARN) {
            entry.append(AnsiColors.colorize(message, AnsiColors.BRIGHT_YELLOW));
        } else if (level == LogLevel.INFO) {
            entry.append(AnsiColors.colorize(message, AnsiColors.WHITE));
        } else { // DEBUG
            entry.append(AnsiColors.colorize(message, AnsiColors.BRIGHT_BLACK));
        }

        if (throwable != null) {
            entry.append("\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            // Exception stack trace in red for errors, yellow for warnings
            String exceptionColor = (level == LogLevel.ERROR) ? AnsiColors.RED : AnsiColors.YELLOW;
            entry.append(AnsiColors.colorize(sw.toString(), exceptionColor));
        }

        return entry.toString();
    }

    /**
     * Gets the logger name.
     * @return the logger name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the minimum log level.
     * @return the minimum log level
     */
    public LogLevel getMinimumLevel() {
        return minimumLevel;
    }

    /**
     * Enables or disables console output.
     * @param enabled whether to enable console output
     */
    public void setConsoleEnabled(boolean enabled) {
        enableConsole.set(enabled);
    }

    /**
     * Enables or disables file output.
     * @param enabled whether to enable file output
     */
    public void setFileEnabled(boolean enabled) {
        enableFile.set(enabled);
    }

    /**
     * Enables or disables colored console output.
     * @param enabled whether to enable colored output
     */
    public void setColorsEnabled(boolean enabled) {
        enableColors.set(enabled && COLORS_SUPPORTED);
    }

    /**
     * Gets all buffered log entries and clears the buffer.
     * @return array of log entries
     */
    public String[] getBufferedLogs() {
        String[] logs = logBuffer.toArray(new String[0]);
        logBuffer.clear();
        return logs;
    }

    /**
     * Checks if console output is enabled.
     * @return true if console output is enabled
     */
    public boolean isConsoleEnabled() {
        return enableConsole.get();
    }

    /**
     * Checks if file output is enabled.
     * @return true if file output is enabled
     */
    public boolean isFileEnabled() {
        return enableFile.get();
    }

    /**
     * Checks if colored output is enabled.
     * @return true if colored output is enabled
     */
    public boolean isColorsEnabled() {
        return enableColors.get();
    }

    /**
     * Checks if the environment supports ANSI colors.
     * @return true if colors are supported
     */
    public static boolean areColorsSupported() {
        return COLORS_SUPPORTED;
    }
}
