package me.jacksonhoggard.raydream.util;

/**
 * ANSI color codes for console output formatting.
 * Provides colored text support for different log levels and components.
 */
public final class AnsiColors {
    
    // ANSI Reset
    public static final String RESET = "\u001B[0m";
    
    // Regular Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Bright Colors
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    
    // Background Colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // Text Attributes
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String REVERSE = "\u001B[7m";
    public static final String STRIKETHROUGH = "\u001B[9m";
    
    private AnsiColors() {
        // Prevent instantiation
    }
    
    /**
     * Wraps text with the specified color and resets afterward.
     * @param text the text to colorize
     * @param color the ANSI color code
     * @return the colorized text
     */
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }
    
    /**
     * Wraps text with the specified color and attributes, then resets.
     * @param text the text to colorize
     * @param color the ANSI color code
     * @param attributes additional attributes (bold, underline, etc.)
     * @return the colorized text with attributes
     */
    public static String colorize(String text, String color, String... attributes) {
        StringBuilder builder = new StringBuilder();
        builder.append(color);
        for (String attribute : attributes) {
            builder.append(attribute);
        }
        builder.append(text).append(RESET);
        return builder.toString();
    }
    
    /**
     * Gets the appropriate color for a log level.
     * @param level the log level
     * @return the ANSI color code for the level
     */
    public static String getLogLevelColor(LogLevel level) {
        switch (level) {
            case DEBUG:
                return BRIGHT_BLACK; // Gray
            case INFO:
                return BRIGHT_BLUE;  // Bright Blue
            case WARN:
                return BRIGHT_YELLOW; // Bright Yellow
            case ERROR:
                return BRIGHT_RED;    // Bright Red
            default:
                return WHITE;
        }
    }
    
    /**
     * Checks if the current environment supports ANSI colors.
     * @return true if colors are supported
     */
    public static boolean supportsColors() {
        // Check if we're on Windows and if ANSI support is enabled
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            // Windows 10 version 1511+ supports ANSI colors in console
            // Check if console supports it or if we're in an IDE
            String term = System.getenv("TERM");
            String colorTerm = System.getenv("COLORTERM");
            String ideaInitialDirectory = System.getenv("IDEA_INITIAL_DIRECTORY");
            
            return term != null || colorTerm != null || ideaInitialDirectory != null;
        }
        
        // Unix-like systems generally support ANSI colors
        return !os.contains("windows") || System.getenv("TERM") != null;
    }
}
