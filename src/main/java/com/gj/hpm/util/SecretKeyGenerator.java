package com.gj.hpm.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecretKeyGenerator {

    public static byte[] generateRandomKey(int keyLength) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        byte[] keyBytes = new byte[keyLength];
        secureRandom.nextBytes(keyBytes);
        return keyBytes;
    }
}
