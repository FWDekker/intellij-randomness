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
    private final String DICTIONARY_FILE = "words_alpha.txt";

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
        try (InputStream resource = getClass().getClassLoader().getResourceAsStream(DICTIONARY_FILE)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            final List<String> words = reader.lines()
                    .filter(word -> word.length() >= wordSettings.getMinLength()
                            && word.length() <= wordSettings.getMaxLength())
                    .collect(Collectors.toList());

            final int randomIndex = ThreadLocalRandom.current().nextInt(0, words.size() - 1);
            return words.get(randomIndex);
        } catch (final IOException e) {
            throw new RuntimeException("Could not generate random word.", e);
        }
    }
}
