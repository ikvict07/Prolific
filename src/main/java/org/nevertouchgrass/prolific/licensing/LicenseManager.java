package org.nevertouchgrass.prolific.licensing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class LicenseManager {

    private static final List<String> generatedKeys = new ArrayList<>();

    static {
        try {
            generateLicenseKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void generateLicenseKeys() throws NoSuchAlgorithmException {
        generatedKeys.add(generateLicenseKey("SampleCorp", "2026-12-31"));
        generatedKeys.add(generateLicenseKey("TechCorp", "2026-12-15"));
        generatedKeys.add(generateLicenseKey("FutureCorp", "2026-12-11"));
    }

    private static String generateLicenseKey(String companyName, String expirationDate) throws NoSuchAlgorithmException {
        String data = companyName + expirationDate;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyLicenseKey(String inputKey) {
        return generatedKeys.contains(inputKey);
    }

    public static void getGeneratedKeys() {
        System.out.println("Generated License Keys:");
        for (String key : generatedKeys) {
            System.out.println(key);
        }
    }
}
