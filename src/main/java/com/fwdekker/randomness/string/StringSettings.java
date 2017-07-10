package com.fwdekker.randomness.string;


/**
 * Contains settings for generating random strings.
 */
final class StringSettings {
    /**
     * The characters that may be used for generated strings.
     */
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int DEFAULT_MIN_LENGTH = 10;
    private static final int DEFAULT_MAX_LENGTH = 10;

    /**
     * The minimum length of a generated string, inclusive.
     */
    private static int minLength = DEFAULT_MIN_LENGTH;
    /**
     * The maximum length of a generated string, inclusive.
     */
    private static int maxLength = DEFAULT_MAX_LENGTH;
    /**
     * The string that encloses the generated string on both sides.
     */
    private static String enclosure = "\"";
    /**
     * True if generated strings should be enclosed with quotation marks.
     */
    private static boolean quotationMarksEnabled = true;


    /**
     * Private to prevent instantiation.
     */
    private StringSettings() {
    }


    /**
     * Returns the minimum length of a generated string, inclusive.
     *
     * @return the minimum length of a generated string, inclusive
     */
    static int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of a generated string, inclusive.
     *
     * @param minLength the minimum length of a generated string, inclusive
     */
    static void setMinLength(final int minLength) {
        StringSettings.minLength = minLength;
    }

    /**
     * Returns the maximum length of a generated string, inclusive.
     *
     * @return the maximum length of a generated string, inclusive
     */
    static int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of a generated string, inclusive.
     *
     * @param maxLength the maximum length of a generated string, inclusive
     */
    static void setMaxLength(final int maxLength) {
        StringSettings.maxLength = maxLength;
    }

    /**
     * Returns the string that encloses the generated string on both sides.
     *
     * @return the string that encloses the generated string on both sides
     */
    static String getEnclosure() {
        return enclosure;
    }

    /**
     * Returns true if generated strings should be enclosed with quotation marks.
     *
     * @return true if generated strings should be enclosed with quotation marks
     */
    static boolean isQuotationMarksEnabled() {
        return quotationMarksEnabled;
    }

    /**
     * Sets the quotation marks property.
     *
     * @param quotationMarksEnabled true if generated strings should be enclosed with quotation marks
     */
    static void setQuotationMarksEnabled(final boolean quotationMarksEnabled) {
        StringSettings.quotationMarksEnabled = quotationMarksEnabled;
        StringSettings.enclosure = quotationMarksEnabled ? "\"" : "";
    }
}
