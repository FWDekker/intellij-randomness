package com.fwdekker.randomness.word;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.ui.JBPopupHelper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Generates random alphanumerical English words based on the settings in {@link WordSettings}.
 */
public final class WordInsertAction extends DataInsertAction {
    private final WordSettings wordSettings;


    /**
     * Constructs a new {@code WordInsertAction} that uses the singleton {@code WordSettings} instance.
     */
    public WordInsertAction() {
        this.wordSettings = WordSettings.getInstance();
    }

    /**
     * Constructs a new {@code WordInsertAction} that uses the given {@code WordSettings} instance.
     *
     * @param wordSettings the settings to use for generating words
     */
    WordInsertAction(final @NotNull WordSettings wordSettings) {
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
        final ValidationInfo validationInfo = wordSettings.validateActiveDictionaries();
        if (validationInfo != null) {
            JBPopupHelper.showMessagePopup(
                    "Randomness error",
                    validationInfo.message,
                    "Please check your Randomness `word` settings."
            );
            return "";
        }

        final List<String> words = Dictionary.combine(wordSettings.getValidActiveDictionaries())
                .getWordsWithLengthInRange(wordSettings.getMinLength(), wordSettings.getMaxLength());
        if (words.isEmpty()) {
            JBPopupHelper.showMessagePopup(
                    "Randomness error",
                    "There are no words compatible with the current settings.",
                    "Please check your Randomness `word` settings."
            );
            return "";
        }

        final int randomIndex = ThreadLocalRandom.current().nextInt(0, words.size());
        final String randomWord = wordSettings.getCapitalization().getTransform().apply(words.get(randomIndex));

        return wordSettings.getEnclosure() + randomWord + wordSettings.getEnclosure();
    }


    /**
     * Inserts an array of words.
     */
    public final class ArrayAction extends DataArrayInsertAction {
        /**
         * Constructs a new {@code ArrayAction} for words.
         */
        public ArrayAction() {
            super(WordInsertAction.this);
        }


        @Override
        protected String getName() {
            return "Insert Word Array";
        }
    }
}
