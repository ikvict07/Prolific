package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PathService} class.
 */
@SpringBootTest(classes = PathService.class)
class PathServiceTests extends BackendTestBase {
    @Autowired
    private PathService pathService;

    @MockitoBean
    private XmlMapper xmlMapper;

    @ParameterizedTest(name = "should normalize {0} to {1} on {2}")
    @CsvSource({
            "jar:/usr/local/bin, /usr/local/bin, linux", 
            "jar:/C:/Users/user, C:/Users/user, Windows", 
            "jar:/Users/user, /Users/user, macOS"
    })
    void should_normalize_url_correctly_on_different_platforms(String input, String expected, String os) {
        // Given
        Path path = Path.of(input);
        Path expectedPath = Path.of(expected);
        System.setProperty("os.name", os);

        // When
        var result = pathService.normalizeUrl(URI.create(path.toString()));

        // Then
        assertThat(result).isEqualTo(expectedPath);
    }

    @Test
    void should_return_correct_project_path() {
        // Given - No specific setup needed

        // When
        var result = pathService.getProjectPath();

        // Then
        assertThat(result)
                .isNotNull()
                .matches(r -> FileSystems.getDefault().getPathMatcher("glob:**/Prolific").matches(r),
                        "Path should end with 'Prolific'");
    }
}
