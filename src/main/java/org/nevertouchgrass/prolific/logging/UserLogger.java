package org.nevertouchgrass.prolific.logging;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class UserLogger {

    private static final Map<LogLevel, List<Consumer<LogMessage>>> listeners = new EnumMap<>(LogLevel.class);

    //region Listeners

    //region add
    public static void registerOnInfoListener(Consumer<LogMessage> listener) {
        registerListener(LogLevel.INFO, listener);
    }
    public static void registerOnWarningListener(Consumer<LogMessage> listener) {
        registerListener(LogLevel.WARNING, listener);
    }
    public static void registerOnErrorListener(Consumer<LogMessage> listener) {
        registerListener(LogLevel.ERROR, listener);
    }
    public static void registerListener(LogLevel level, Consumer<LogMessage> listener) {
        listeners.computeIfAbsent(level, k -> new ArrayList<>()).add(listener);
    }
    //endregion

    //region remove
    public static void removeListener(Consumer<LogMessage> listener) {
        // Iterate over the entries in the listeners map
        for (Map.Entry<LogLevel, List<Consumer<LogMessage>>> entry : listeners.entrySet()) {
            // Remove the listener from the list of consumers for this LogLevel
            entry.getValue().remove(listener);

            // If the list is now empty, remove the entry from the map
            if (entry.getValue().isEmpty()) {
                listeners.remove(entry.getKey());
            }
        }
    }
    //endregion

    //endregion

    //region log info
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    public static void info(String format, Object... args) {
        log(LogLevel.INFO, format, args);
    }
    //endregion

    //region log warning
    public static void warn(String message) {
        log(LogLevel.WARNING, message);
    }

    public static void warn(String format, Object... args) {
        log(LogLevel.WARNING, format, args);
    }
    //endregion

    //region log error
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public static void error(String format, Object... args) {
        log(LogLevel.ERROR, format, args);
    }

    public static void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }
    //endregion

    //region base log
    public static void log(LogLevel level, String message) {
        notifyListeners(new LogMessage(level, message));
    }
    public static void log(LogLevel level, String format,Object... args) {
        notifyListeners(new LogMessage(level, formatMessage(format, args)));
    }
    public static void log(LogLevel level, String message, Throwable throwable) {
        String fullMessage = message;
        //*/
        if (throwable != null) {
            fullMessage += ": " + throwable.getMessage();
        }
        /*/ // With full stack
        if (throwable != null) {
            // Get the stack trace from the Throwable and include it in the message
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw); // This will write the stack trace to the StringWriter
            fullMessage += "\n" + sw.toString(); // Append stack trace to the message
        }
        //*/
        notifyListeners(new LogMessage(level, fullMessage, throwable));
    }
    //endregion

    //region notifyListeners
    private static void notifyListeners(LogMessage message) {
        List<Consumer<LogMessage>> levelListeners = listeners.get(message.getLevel());
        if (levelListeners != null) {
            for (Consumer<LogMessage> listener : levelListeners) {
                try {
                    listener.accept(message);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    };
    //endregion

    //region Format Message
    public static String formatMessage(String format, Object... args) {
        if (format == null) {
            return "null";
        }

        StringBuilder result = new StringBuilder(format.length() + (args.length * 10)); // Estimate capacity
        int argIndex = 0;
        int length = format.length();

        for (int i = 0; i < length; i++) {
            char c = format.charAt(i);

            // Check for escaped braces
            if (c == '{' && i + 1 < length && format.charAt(i + 1) == '{') {
                result.append('{');
                continue;
            }
            if (c == '}' && i - 1 > -1 && format.charAt(i - 1) == '}') {
                result.append('}');
                continue;
            }

            // Handle placeholder replacement
            if (c == '{' && i + 1 < length && format.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    result.append(toString(args[argIndex++])); // Replace with argument
                } else {
                    result.append("{}"); // Not enough arguments, leave placeholder
                }
                i++; // Skip over '}'
                continue;
            }

            // Regular character
            result.append(c);
        }

        return result.toString();
    }
    // Helper method to handle null safely
    private static String toString(Object obj) {
        if (obj == null) return "null";
        if (obj.getClass().isArray()) return arrayToString(obj);
        return obj.toString();
    }
    // Convert an array to string representation.
    private static String arrayToString(Object array) {
        if (array instanceof Object[]) {
            return Arrays.toString((Object[]) array);
        } else if (array instanceof boolean[]) {
            return Arrays.toString((boolean[]) array);
        } else if (array instanceof byte[]) {
            return Arrays.toString((byte[]) array);
        } else if (array instanceof char[]) {
            return Arrays.toString((char[]) array);
        } else if (array instanceof short[]) {
            return Arrays.toString((short[]) array);
        } else if (array instanceof int[]) {
            return Arrays.toString((int[]) array);
        } else if (array instanceof long[]) {
            return Arrays.toString((long[]) array);
        } else if (array instanceof float[]) {
            return Arrays.toString((float[]) array);
        } else if (array instanceof double[]) {
            return Arrays.toString((double[]) array);
        }
        return array.toString();
    }
    //endregion

    //region LogMessage + LogLevel

    public enum LogLevel {
        INFO, WARNING, ERROR
    }

    public static class LogMessage {
        private final LogLevel level;
        private final String message;
        private final long timestamp;
        private final Throwable throwable;

        public LogMessage(LogLevel level, String message) {
            this(level, message, null);
        }

        public LogMessage(LogLevel level, String message, Throwable throwable) {
            this.level = level;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
            this.throwable = throwable;
        }

        public LogLevel getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public String getFormattedTimestamp() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return sdf.format(new Date(timestamp));
        }

        @Override
        public String toString() {
            return getFormattedTimestamp() + " [" + level + "] " + message;
        }
    }

    //endregion
}