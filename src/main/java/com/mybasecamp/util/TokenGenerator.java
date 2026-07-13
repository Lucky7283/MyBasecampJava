package com.mybasecamp.util;

import java.security.SecureRandom;

/**
 * Utility for generating session tokens.
 * Uses a randomizer with loop to produce a 64-character hex string,
 * matching the format produced by Node.js crypto.randomBytes(32).toString('hex').
 */
public class TokenGenerator {

    private static final String HEX_CHARS = "0123456789abcdef";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 64;

    /**
     * Generates a random session token using a loop-based randomizer.
     *
     * @return a 64-character hexadecimal token
     */
    public static String generateSessionToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(HEX_CHARS.charAt(RANDOM.nextInt(HEX_CHARS.length())));
        }
        return token.toString();
    }
}
