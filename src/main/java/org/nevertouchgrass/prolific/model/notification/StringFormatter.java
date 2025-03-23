package org.nevertouchgrass.prolific.model.notification;

import java.util.Arrays;

public class StringFormatter {

    private StringFormatter() {}
    public static String formatMessage(String format, Object... args) {
        if (format == null) {
            return "null";
        }

        StringBuilder result = new StringBuilder(format.length() + (args.length * 10));
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

            if (c == '{' && i + 1 < length && format.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    result.append(convertToString(args[argIndex++]));
                } else {
                    result.append("{}");
                }
                i++;
                continue;
            }

            // Regular character
            result.append(c);
        }

        return result.toString();
    }

    private static String convertToString(Object obj) {
        if (obj == null) return "null";
        if (obj.getClass().isArray()) return arrayToString(obj);
        return obj.toString();
    }

    private static String arrayToString(Object array) {
        return Arrays.toString((Object[]) array);
    }

}
