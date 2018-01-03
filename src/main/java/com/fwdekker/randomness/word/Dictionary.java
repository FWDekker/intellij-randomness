package com.fwdekker.randomness.word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
     * A set of all words in the dictionary.
     */
    private final Set<String> words;


    /**
     * Constructs an empty {@code Dictionary}.
     */
    private Dictionary() {
        words = new HashSet<>();
    }

    /**
     * Constructs a new {@code Dictionary} from the given resource file.
     *
     * @param dictionary the filename of the dictionary file
     */
    public Dictionary(final String dictionary) {
        // Read dictionary into memory
        try (InputStream resource = Dictionary.class.getClassLoader().getResourceAsStream(dictionary)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            words = reader.lines().collect(Collectors.toSet());
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
     * Returns all words in the dictionary.
     *
     * @return all words in the dictionary
     */
    public List<String> getWords() {
        return new ArrayList<>(words);
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

    /**
     * Combines this {@code Dictionary} with the given {@code Dictionary} into a new {@code Dictionary}.
     *
     * @param that the {@code Dictionary} to add words from
     * @return a new {@code Dictionary} containing the words from both this and that {@code Dictionary}
     */
    public Dictionary combineWith(final Dictionary that) {
        final Dictionary dictionary = new Dictionary();
        dictionary.words.addAll(this.words);
        dictionary.words.addAll(that.words);
        return dictionary;
    }
}
