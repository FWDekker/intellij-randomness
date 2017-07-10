package com.fwdekker.randomness.string;

import com.fwdekker.randomness.InsertRandomSomething;

import java.util.Random;


/**
 * Generates random alphanumerical strings based on the settings in {@link StringSettings}.
 */
public final class InsertRandomString extends InsertRandomSomething {
    private static final Random RANDOM = new Random();


    /**
     * Returns a random string of alphanumerical characters.
     *
     * @return a random string of alphanumerical characters
     */
    @Override
    public String generateString() {
        final int lengthRange = StringSettings.getMaxLength() - StringSettings.getMinLength();
        final int length = StringSettings.getMinLength() + RANDOM.nextInt(lengthRange + 1);

        final char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = generateCharacter();
        }

        return StringSettings.ENCLOSURE + new String(text) + StringSettings.ENCLOSURE;
    }


    /**
     * Returns a random character from the alphabet.
     *
     * @return a random character from the alphabet
     */
    private char generateCharacter() {
        return StringSettings.ALPHABET.charAt(RANDOM.nextInt(StringSettings.ALPHABET.length()));
    }
}
