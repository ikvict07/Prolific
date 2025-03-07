package org.nevertouchgrass.prolific.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nevertouchgrass.prolific.config.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = TestConfiguration.class)
class PathServiceTests {
    private final PathService ps = new PathService();

    @ParameterizedTest
    @CsvSource({"jar:/usr/local/bin, /usr/local/bin, linux", "jar:/C:/Users/user, C:/Users/user, Windows", "jar:/Users/user, /Users/user, macOS"})
    void shouldResolvePathOnAllPlatforms(String input, String expected, String os) {
        // Given
        Path path = Path.of(input);
        Path expectedPath = Path.of(expected);
        System.setProperty("os.name", os);
        // When
        var result = ps.normalizeUrl(URI.create(path.toString()));
        // Then
        assertThat(result).isEqualTo(expectedPath);

    }


    @Test
    void shouldReturnPath() {
        // Given
        // When
        var result = ps.getProjectPath();
        // Then
        assertThat(result).matches(r -> FileSystems.getDefault().getPathMatcher("glob:**/Prolific").matches(r));
    }
}
