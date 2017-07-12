package com.fwdekker.randomness.string;

import com.fwdekker.randomness.InsertRandomSomething;

import java.util.Random;


/**
 * Generates random alphanumerical strings based on the settings in {@link StringSettings}.
 */
final class InsertRandomString extends InsertRandomSomething {
    private static final Random RANDOM = new Random();

    private final StringSettings stringSettings = StringSettings.getInstance();


    /**
     * Returns a random string of alphanumerical characters.
     *
     * @return a random string of alphanumerical characters
     */
    @Override
    public String generateString() {
        final int lengthRange = stringSettings.getMaxLength() - stringSettings.getMinLength();
        final int length = stringSettings.getMinLength() + RANDOM.nextInt(lengthRange + 1);

        final char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = generateCharacter();
        }

        return stringSettings.getEnclosure() + new String(text) + stringSettings.getEnclosure();
    }


    /**
     * Returns a random character from the alphabet.
     *
     * @return a random character from the alphabet
     */
    private char generateCharacter() {
        final String alphabet = stringSettings.getAlphabet();

        return alphabet.charAt(RANDOM.nextInt(alphabet.length()));
    }
}
