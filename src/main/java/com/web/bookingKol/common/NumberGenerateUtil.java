package com.web.bookingKol.common;

import java.security.SecureRandom;

public class NumberGenerateUtil {
    private static final SecureRandom random = new SecureRandom();

    private static String generate(String prefix) {
        int number = random.nextInt(100_000_000);
        return String.format("%s-%08d", prefix, number);
    }

    public static String generateSecureRandomRequestNumber() {
        return generate("RQ");
    }

    public static String generateSecureRandomContractNumber() {
        return generate("CT");
    }

    public static String generateSecureRandomPurchasedCourseNumber() {
        return generate("PC");
    }
}
