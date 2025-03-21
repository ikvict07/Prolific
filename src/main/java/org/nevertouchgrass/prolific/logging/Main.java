package org.nevertouchgrass.prolific.logging;

import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {

        Consumer<UserLogger.LogMessage> showInUI = Main::showInUI;
        Consumer<UserLogger.LogMessage> showInErrorUI = Main::showInErrorUI;
        Consumer<UserLogger.LogMessage> showInWarningUI = Main::showInWarningUI;

        UserLogger.registerOnInfoListener(showInUI);
        UserLogger.registerOnErrorListener(showInErrorUI);
        UserLogger.registerOnWarningListener(showInWarningUI);

        // Log some messages
        UserLogger.info("Application started");
//        UserLogger.removeListener(showInUI);
        UserLogger.info("User {{}} logged in", "john_doe");

        UserLogger.warn("WARNING!");
        try {
            // Some operation that might fail
            int result = 10 / 0;
        } catch (Exception e) {
            UserLogger.error("Failed to perform calculation", e);
        }
    }

    static void showInUI(UserLogger.LogMessage message) {
        System.out.println("UI: " + message);
    }

    private static void showInWarningUI(UserLogger.LogMessage message) {
        System.out.println("UI: " + message);
    }

    private static void showInErrorUI(UserLogger.LogMessage message) {
        System.out.println("Error UI: " + message);
    }
}
