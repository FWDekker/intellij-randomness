package com.fwdekker.randomness.word;

import com.fwdekker.randomness.InsertRandomSomething;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;


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
     * @param wordSettings  the settings to use for generating words
     */
    InsertRandomWord(final @NotNull WordSettings wordSettings) {
        this.wordSettings = wordSettings;
    }


    @Override
    protected String getName() {
        return "Insert Word";
    }

    /**
     * Returns a random alphanumerical English word.
     *
     * @return a random alphanumerical English word
     */
    @Override
    public String generateString() {
        final List<String> words = Dictionary.combine(wordSettings.getActiveDictionaries())
                .getWordsWithLengthInRange(wordSettings.getMinLength(), wordSettings.getMaxLength());
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, words.size());
        final String randomWord = wordSettings.getCapitalization().getTransform().apply(words.get(randomIndex));

        return wordSettings.getEnclosure() + randomWord + wordSettings.getEnclosure();
    }
}
