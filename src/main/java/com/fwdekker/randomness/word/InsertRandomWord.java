package com.fwdekker.randomness.word;

import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.array.ArraySettings;
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
     * @param arraySettings the settings to use for generating arrays
     * @param wordSettings  the settings to use for generating words
     */
    InsertRandomWord(final @NotNull ArraySettings arraySettings, final @NotNull WordSettings wordSettings) {
        super(arraySettings);

        this.wordSettings = wordSettings;
    }


    @Override
    protected String getName() {
        return "Insert Random Word";
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new WordSettingsAction();
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
