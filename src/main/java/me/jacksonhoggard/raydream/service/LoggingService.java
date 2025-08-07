package me.jacksonhoggard.raydream.service;

import me.jacksonhoggard.raydream.util.LogLevel;
import me.jacksonhoggard.raydream.util.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for managing application-wide logging.
 * Provides centralized logger creation, configuration, and file output management.
 */
public class LoggingService {
    private static final String DEFAULT_LOG_DIRECTORY = "logs";
    private static final String LOG_FILE_PATTERN = "raydream_%s.log";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final ConcurrentHashMap<String, Logger> loggers;
    private final LogLevel globalMinimumLevel;
    private final AtomicBoolean consoleEnabled;
    private final AtomicBoolean fileEnabled;
    private final AtomicBoolean colorsEnabled;
    private final Path logDirectory;
    private final ScheduledExecutorService fileWriteExecutor;

    /**
     * Creates a new logging service with the specified configuration.
     * @param globalMinimumLevel the minimum log level for all loggers
     * @param consoleEnabled whether console output is enabled by default
     * @param fileEnabled whether file output is enabled by default
     */
    public LoggingService(LogLevel globalMinimumLevel, boolean consoleEnabled, boolean fileEnabled) {
        this.loggers = new ConcurrentHashMap<>();
        this.globalMinimumLevel = globalMinimumLevel;
        this.consoleEnabled = new AtomicBoolean(consoleEnabled);
        this.fileEnabled = new AtomicBoolean(fileEnabled);
        this.colorsEnabled = new AtomicBoolean(true); // Enable colors by default
        this.logDirectory = Paths.get(DEFAULT_LOG_DIRECTORY);
        this.fileWriteExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LogFileWriter");
            t.setDaemon(true);
            return t;
        });

        // Initialize log directory
        try {
            Files.createDirectories(logDirectory);
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }

        // Start periodic file writing
        if (fileEnabled) {
            fileWriteExecutor.scheduleAtFixedRate(this::writeLogsToFile, 5, 5, TimeUnit.SECONDS);
        }
    }

    /**
     * Gets or creates a logger for the specified class.
     * @param clazz the class to create a logger for
     * @return the logger instance
     */
    public Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    /**
     * Gets or creates a logger with the specified name.
     * @param name the logger name
     * @return the logger instance
     */
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, n -> {
            Logger logger = new Logger(n, globalMinimumLevel, consoleEnabled.get(), fileEnabled.get());
            logger.setColorsEnabled(colorsEnabled.get());
            return logger;
        });
    }

    /**
     * Sets the global console output setting for all loggers.
     * @param enabled whether to enable console output
     */
    public void setGlobalConsoleEnabled(boolean enabled) {
        consoleEnabled.set(enabled);
        loggers.values().forEach(logger -> logger.setConsoleEnabled(enabled));
    }

    /**
     * Sets the global file output setting for all loggers.
     * @param enabled whether to enable file output
     */
    public void setGlobalFileEnabled(boolean enabled) {
        fileEnabled.set(enabled);
        loggers.values().forEach(logger -> logger.setFileEnabled(enabled));
        
        if (enabled && !fileWriteExecutor.isShutdown()) {
            fileWriteExecutor.scheduleAtFixedRate(this::writeLogsToFile, 5, 5, TimeUnit.SECONDS);
        }
    }

    /**
     * Sets the global colored output setting for all loggers.
     * @param enabled whether to enable colored output
     */
    public void setGlobalColorsEnabled(boolean enabled) {
        colorsEnabled.set(enabled);
        loggers.values().forEach(logger -> logger.setColorsEnabled(enabled));
    }

    /**
     * Writes all buffered logs to the current log file.
     */
    private void writeLogsToFile() {
        if (!fileEnabled.get()) {
            return;
        }

        String today = LocalDateTime.now().format(FILE_DATE_FORMAT);
        Path logFile = logDirectory.resolve(String.format(LOG_FILE_PATTERN, today));

        try {
            boolean fileExists = Files.exists(logFile);
            
            try (BufferedWriter writer = Files.newBufferedWriter(logFile, 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.APPEND)) {
                
                // Write header if this is a new file
                if (!fileExists) {
                    writer.write("=== RayDream Application Log - " + today + " ===\n");
                    writer.newLine();
                }

                // Write all buffered logs from all loggers
                for (Logger logger : loggers.values()) {
                    String[] bufferedLogs = logger.getBufferedLogs();
                    for (String logEntry : bufferedLogs) {
                        writer.write(logEntry);
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    /**
     * Forces an immediate write of all buffered logs to file.
     */
    public void flushLogs() {
        writeLogsToFile();
    }

    /**
     * Gets the current log directory path.
     * @return the log directory path
     */
    public Path getLogDirectory() {
        return logDirectory;
    }

    /**
     * Gets the global minimum log level.
     * @return the global minimum log level
     */
    public LogLevel getGlobalMinimumLevel() {
        return globalMinimumLevel;
    }

    /**
     * Checks if console output is globally enabled.
     * @return true if console output is enabled
     */
    public boolean isConsoleEnabled() {
        return consoleEnabled.get();
    }

    /**
     * Checks if file output is globally enabled.
     * @return true if file output is enabled
     */
    public boolean isFileEnabled() {
        return fileEnabled.get();
    }

    /**
     * Checks if colored output is globally enabled.
     * @return true if colored output is enabled
     */
    public boolean isColorsEnabled() {
        return colorsEnabled.get();
    }

    /**
     * Gets the number of active loggers.
     * @return the number of active loggers
     */
    public int getLoggerCount() {
        return loggers.size();
    }

    /**
     * Shuts down the logging service and releases resources.
     */
    public void shutdown() {
        // Write any remaining logs
        flushLogs();
        
        // Shutdown the executor
        fileWriteExecutor.shutdown();
        try {
            if (!fileWriteExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                fileWriteExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            fileWriteExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
