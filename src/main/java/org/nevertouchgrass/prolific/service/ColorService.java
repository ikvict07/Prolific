package org.nevertouchgrass.prolific.service;

import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service that manages color creation
 */
@Service
public class ColorService {

    public int getSeedForProject(Project project) {
        return Objects.hash(project.getTitle(), project.getType(), project.getPath()) * project.getPath().length();
    }
    public String generateBrightPastelColor(int hash) {
        int red = 55 + Math.abs((hash) % 150);
        int green = 55 + Math.abs((hash / 7) % 150);
        int blue = 55 + Math.abs((hash / 13) % 150);

        while (calculateLuminance(red, green, blue) < 100 || calculateLuminance(red, green, blue) > 220) {
            if (calculateLuminance(red, green, blue) < 100) {
                red = Math.min(255, red + 20);
                green = Math.min(255, green + 20);
                blue = Math.min(255, blue + 20);
            } else {
                red = Math.max(55, red - 20);
                green = Math.max(55, green - 20);
                blue = Math.max(55, blue - 20);
            }
        }

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public String generateGradientBoxStyle(String baseColor, int hash) {
        String highlightColor = generateSimilarBrightPastelColor(baseColor, hash);

        return String.format(
                "-fx-background-color: linear-gradient(from 0%% 0%% to 100%% 0%%, transparent 0%%, %4s99 30%%, transparent 100%%);",
                highlightColor);
    }

    public String extractPrimaryColor(String style) {
        int startIndex = style.indexOf("#");
        int endIndex = style.indexOf(" ", startIndex);
        return style.substring(startIndex, endIndex);
    }

    public String generateRandomColorStyle(int hash) {
        String color1 = generateBrightPastelColor(hash);
        String color2 = generateSimilarBrightPastelColor(color1, hash);

        return String.format("-fx-background-color: linear-gradient(to bottom, %s 0%%, %s 100%%);", color1, color2);
    }

    public String generateSimilarBrightPastelColor(String baseColor, int hash) {
        int baseRed = Integer.parseInt(baseColor.substring(1, 3), 16);
        int baseGreen = Integer.parseInt(baseColor.substring(3, 5), 16);
        int baseBlue = Integer.parseInt(baseColor.substring(5, 7), 16);

        int redOffset = (hash % 51) - 25;
        int greenOffset = ((hash / 11) % 51) - 25;
        int blueOffset = ((hash / 17) % 51) - 25;

        int red = Math.clamp(baseRed + redOffset, 55, 155);
        int green = Math.clamp(baseGreen + greenOffset, 55, 155);
        int blue = Math.clamp(baseBlue + blueOffset, 55, 155);

        while (calculateLuminance(red, green, blue) > 220) {
            red = Math.max(55, red - 10);
            green = Math.max(55, green - 10);
            blue = Math.max(55, blue - 10);
        }

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public double calculateLuminance(int red, int green, int blue) {
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }
}