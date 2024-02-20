package com.gj.hpm.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecretKeyGenerator {

    // public static void main(String[] args) throws NoSuchAlgorithmException {
    // // Generate a 32-byte (256-bit) random key
    // byte[] keyBytes = generateRandomKey(32);

    // // Encode the key in Base64 to get a string representation
    // String secretKey = Base64.getEncoder().encodeToString(keyBytes);

    // }

    public static byte[] generateRandomKey(int keyLength) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        byte[] keyBytes = new byte[keyLength];
        secureRandom.nextBytes(keyBytes);
        return keyBytes;
    }
}
