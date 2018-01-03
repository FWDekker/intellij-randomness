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
     * The name of the default dictionary file.
     */
    private static final String DEFAULT_DICTIONARY_FILE = "words_alpha.dic";
    /**
     * The default {@code Dictionary} instance.
     */
    private static final Dictionary DEFAULT_DICTIONARY = new Dictionary(DEFAULT_DICTIONARY_FILE);

    /**
     * A list of all words in the dictionary.
     */
    private final List<String> words;


    /**
     * Constructs a new {@code Dictionary} from the given resource file.
     *
     * @param dictionary the filename of the dictionary file
     */
    public Dictionary(final String dictionary) {
        // Read dictionary into memory
        try (InputStream resource = Dictionary.class.getClassLoader().getResourceAsStream(dictionary)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            words = reader.lines().collect(Collectors.toList());
            reader.close();
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to read dictionary into memory.", e);
        }

        if (words.isEmpty()) {
            throw new IllegalArgumentException("Dictionary must be non-empty.");
        }
    }

    /**
     * Returns the default {@code Dictionary} instance.
     *
     * @return the default {@code Dictionary} instance
     */
    public static Dictionary getDefaultDictionary() {
        return DEFAULT_DICTIONARY;
    }


    /**
     * Returns a list of all words with a length in the given range.
     *
     * @param minLength the minimum word length (inclusive)
     * @param maxLength the maximum word length (inclusive)
     * @return a list of all words with a length in the given range
     */
    public List<String> getWordsWithLengthInRange(final int minLength, final int maxLength) {
        return words.parallelStream()
                .filter(word -> word.length() >= minLength && word.length() <= maxLength)
                .collect(Collectors.toList());
    }

    /**
     * Returns the length of the longest word.
     *
     * @return the length of the longest word
     */
    public int longestWordLength() {
        return words.parallelStream()
                .mapToInt(String::length)
                .max()
                .orElseThrow(() -> new IllegalStateException("Dictionary should not be empty."));
    }
}
