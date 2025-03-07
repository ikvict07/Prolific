package org.nevertouchgrass.prolific.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ColorService {

    private final Random random = new Random(89);

    public String generateBrightPastelColor() {
        int red;
        int green;
        int blue;

        do {
            red = 55 + random.nextInt(100);
            green = 55 + random.nextInt(100);
            blue = 55 + random.nextInt(100);
        } while (calculateLuminance(red, green, blue) > 220);

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public String generateSimilarBrightPastelColor(String baseColor) {
        int red;
        int green;
        int blue;

        int baseRed = Integer.parseInt(baseColor.substring(1, 3), 16);
        int baseGreen = Integer.parseInt(baseColor.substring(3, 5), 16);
        int baseBlue = Integer.parseInt(baseColor.substring(5, 7), 16);

        do {
            red = Math.clamp(baseRed + random.nextInt(51) - 25L, 55, 155);
            green = Math.clamp(baseGreen + random.nextInt(51) - 25L, 55, 155);
            blue = Math.clamp(baseBlue + random.nextInt(51) - 25L, 55, 155);
        } while (calculateLuminance(red, green, blue) > 220);

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public double calculateLuminance(int red, int green, int blue) {
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }
}
