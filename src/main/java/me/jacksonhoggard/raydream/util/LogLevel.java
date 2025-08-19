package me.jacksonhoggard.raydream.util;

/**
 * Enumeration of different log levels for the application.
 * Each level has a priority value where higher values indicate more severe messages.
 */
public enum LogLevel {
    DEBUG(0, "DEBUG"),
    INFO(1, "INFO"),
    WARN(2, "WARN"),
    ERROR(3, "ERROR");

    private final int priority;
    private final String displayName;

    LogLevel(int priority, String displayName) {
        this.priority = priority;
        this.displayName = displayName;
    }

    /**
     * Gets the priority level of this log level.
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets the display name of this log level.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this log level should be logged based on the minimum level.
     * @param minimumLevel the minimum level to log
     * @return true if this level should be logged
     */
    public boolean shouldLog(LogLevel minimumLevel) {
        return this.priority >= minimumLevel.priority;
    }
}
