package com.fwdekker.randomness.string;

import com.fwdekker.randomness.InsertRandomSomething;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Generates random alphanumerical strings based on the settings in {@link StringSettings}.
 */
public final class InsertRandomString extends InsertRandomSomething {
    private final StringSettings stringSettings;


    /**
     * Constructs a new {@code InsertRandomString} that uses the singleton {@code StringSettings} instance.
     */
    public InsertRandomString() {
        this.stringSettings = StringSettings.getInstance();
    }

    /**
     * Constructs a new {@code InsertRandomString} that uses the given {@code StringSettings} instance.
     *
     * @param stringSettings the settings to use for generating strings
     */
    InsertRandomString(final StringSettings stringSettings) {
        this.stringSettings = stringSettings;
    }


    @Override
    protected String getName() {
        return "Insert Random String";
    }

    /**
     * Returns a random string of alphanumerical characters.
     *
     * @return a random string of alphanumerical characters
     */
    @Override
    public String generateString() {
        final int length = ThreadLocalRandom.current()
                .nextInt(stringSettings.getMinLength(), stringSettings.getMaxLength() + 1);

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
        final String alphabet = Alphabet.concatenate(stringSettings.getAlphabets());
        final int charIndex = ThreadLocalRandom.current().nextInt(alphabet.length());

        return alphabet.charAt(charIndex);
    }
}
