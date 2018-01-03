package com.fwdekker.randomness.word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A dictionary of English words.
 */
public final class Dictionary {
    /**
     * The name of the dictionary file.
     */
    public static final String DICTIONARY_FILE = "words_alpha.dictionary";

    /**
     * A list of all words in the dictionary.
     */
    private static final List<String> WORDS;


    static {
        // Read dictionary into memory
        try (InputStream resource = Dictionary.class.getClassLoader().getResourceAsStream(DICTIONARY_FILE)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            WORDS = reader.lines().collect(Collectors.toList());
            reader.close();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read dictionary into memory.", e);
        }
    }


    /**
     * Private constructor to prevent instantiation.
     */
    private Dictionary() {
        // Do nothing
    }


    /**
     * Returns a list of all words with a length in the given range.
     *
     * @param minLength the minimum word length (inclusive)
     * @param maxLength the maximum word length (inclusive)
     * @return a list of all words with a length in the given range
     */
    public static List<String> getWordsWithLengthInRange(final int minLength, final int maxLength) {
        return WORDS.parallelStream()
                .filter(word -> word.length() >= minLength && word.length() <= maxLength)
                .collect(Collectors.toList());
    }

    /**
     * Returns the length of the longest word.
     *
     * @return the length of the longest word
     */
    public static int longestWordLength() {
        return WORDS.parallelStream()
                .mapToInt(String::length)
                .max()
                .orElseThrow(() -> new IllegalStateException("Dictionary should not be empty."));
    }
}
