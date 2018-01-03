package com.fwdekker.randomness.word;

import com.fwdekker.randomness.InsertRandomSomething;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


/**
 * Generates random alphanumerical English words based on the settings in {@link WordSettings}.
 */
public final class InsertRandomWord extends InsertRandomSomething {
    private final WordSettings wordSettings;


    /**
     * Constructs a new {@code InsertRandomWord} that uses the singleton {@code WordSettings} instance.
     */
    public InsertRandomWord() {
        this.wordSettings = WordSettings.getInstance();
    }

    /**
     * Constructs a new {@code InsertRandomWord} that uses the given {@code WordSettings} instance.
     *
     * @param wordSettings the settings to use for generating words
     */
    InsertRandomWord(final WordSettings wordSettings) {
        this.wordSettings = wordSettings;
    }


    /**
     * Returns a random alphanumerical English word.
     *
     * @return a random alphanumerical English word
     */
    @Override
    public String generateString() {
        final List<String> words = Dictionary
                .getWordsWithLengthInRange(wordSettings.getMinLength(), wordSettings.getMaxLength());
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, words.size());
        return wordSettings.getEnclosure() + words.get(randomIndex) + wordSettings.getEnclosure();
    }
}
