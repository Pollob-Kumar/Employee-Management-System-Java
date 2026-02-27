package com.ems.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class PasswordUtil {
    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_LEN = 16;
    private static final int KEY_LEN_BITS = 256; // 32 bytes

    private PasswordUtil() {}

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LEN];
        RNG.nextBytes(salt);
        return salt;
    }

    public static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LEN_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }
}