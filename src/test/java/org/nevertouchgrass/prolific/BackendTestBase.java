package org.nevertouchgrass.prolific;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

/**
 * Base class for backend tests that provides common mock objects and utilities.
 * Uses a custom display name generator to improve test readability.
 */
@SuppressWarnings("unused")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BackendTestBase {
    /**
     * Helper method to get the project root directory path.
     * @return The project root directory path as a String
     */
    protected String getProjectRootPath() {
        return System.getProperty("user.dir");
    }
}
