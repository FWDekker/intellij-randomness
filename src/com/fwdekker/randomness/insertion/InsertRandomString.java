package com.fwdekker.randomness.insertion;

import java.util.Random;


/**
 * Generates random alphanumerical strings based on adjustable parameters.
 */
public final class InsertRandomString extends InsertRandomSomething {
    private static final Random RANDOM = new Random();
    /**
     * The characters that may be used in generated strings.
     */
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * The minimum length of a generated string, inclusive.
     */
    private static int minLength = 10;
    /**
     * The maximum length of a generated string, inclusive.
     */
    private static int maxLength = 10;


    /**
     * Returns a random string of alphanumerical characters.
     *
     * @return a random string of alphanumerical characters
     */
    @Override
    String generateString() {
        final int length = minLength + RANDOM.nextInt(maxLength - minLength + 1);

        final char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()));
        }
        return "\"" + new String(text) + "\"";
    }


    /**
     * Returns the minimum length of a generated string, inclusive.
     *
     * @return the minimum length of a generated string, inclusive
     */
    public static int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of a generated string, inclusive.
     *
     * @param minLength the minimum length of a generated string, inclusive
     */
    public static void setMinLength(final int minLength) {
        InsertRandomString.minLength = minLength;
    }

    /**
     * Returns the maximum length of a generated string, inclusive.
     *
     * @return the maximum length of a generated string, inclusive
     */
    public static int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of a generated string, inclusive.
     *
     * @param maxLength the maximum length of a generated string, inclusive
     */
    public static void setMaxLength(final int maxLength) {
        InsertRandomString.maxLength = maxLength;
    }
}
