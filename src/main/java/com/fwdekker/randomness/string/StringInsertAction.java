package com.fwdekker.randomness.string;

import com.fwdekker.randomness.DataInsertAction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;


/**
 * Generates random alphanumerical strings based on the settings in {@link StringSettings}.
 */
public final class StringInsertAction extends DataInsertAction {
    private final StringSettings stringSettings;


    /**
     * Constructs a new {@code StringInsertAction} that uses the singleton {@code StringSettings} instance.
     */
    public StringInsertAction() {
        this.stringSettings = StringSettings.getInstance();
    }

    /**
     * Constructs a new {@code StringInsertAction} that uses the given {@code StringSettings} instance.
     *
     * @param stringSettings the settings to use for generating strings
     */
    StringInsertAction(final @NotNull StringSettings stringSettings) {
        this.stringSettings = stringSettings;
    }


    @Override
    protected String getName() {
        return "Insert String";
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

        final String capitalizedText = stringSettings.getCapitalization().getTransform().apply(new String(text));
        return stringSettings.getEnclosure() + capitalizedText + stringSettings.getEnclosure();
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
