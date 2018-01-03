package com.fwdekker.randomness.word;

import com.intellij.util.xmlb.annotations.Transient;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * A dictionary of English words.
 */
public abstract class Dictionary {
    /**
     * The name of the default dictionary file.
     */
    public static final String DEFAULT_DICTIONARY_FILE = "words_alpha.dic";
    /**
     * The default {@code Dictionary} instance.
     */
    private static final Dictionary DEFAULT_DICTIONARY = new BundledDictionary(DEFAULT_DICTIONARY_FILE);

    /**
     * The filename of the dictionary file.
     */
    private final String path;
    /**
     * A set of all words in the dictionary.
     */
    @Transient
    private final Set<String> words;


    /**
     * Constructs an empty {@code Dictionary}.
     */
    private Dictionary() {
        path = "";
        words = new HashSet<>();
    }

    /**
     * Constructs a new {@code Dictionary} from the given resource file.
     *
     * @param path the filename of the dictionary file
     */
    Dictionary(final String path) {
        this.path = path;

        // Read dictionary into memory
        try (InputStream iStream = getInputStream(path)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));
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
     * Returns an {@link InputStream} for the given dictionary file.
     * <p>
     * Different kinds of dictionaries may need a different method of accessing the source file.
     *
     * @param dictionary the location of the dictionary file
     * @return an {@link InputStream} for the given dictionary file
     * @throws IOException if the dictionary file could not be read
     */
    abstract InputStream getInputStream(final String dictionary) throws IOException;


    /**
     * Returns the filename of the dictionary file.
     *
     * @return the filename of the dictionary file
     */
    public final String getPath() {
        return path;
    }

    /**
     * Returns all words in the dictionary.
     *
     * @return all words in the dictionary
     */
    public final List<String> getWords() {
        return new ArrayList<>(words);
    }

    /**
     * Returns a list of all words with a length in the given range.
     *
     * @param minLength the minimum word length (inclusive)
     * @param maxLength the maximum word length (inclusive)
     * @return a list of all words with a length in the given range
     */
    public final List<String> getWordsWithLengthInRange(final int minLength, final int maxLength) {
        return words.parallelStream()
                .filter(word -> word.length() >= minLength && word.length() <= maxLength)
                .collect(Collectors.toList());
    }

    /**
     * Returns the length of the longest word.
     *
     * @return the length of the longest word
     */
    public final int longestWordLength() {
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
    public final Dictionary combineWith(final Dictionary that) {
        final Dictionary dictionary = new SimpleDictionary();
        dictionary.words.addAll(this.words);
        dictionary.words.addAll(that.words);
        return dictionary;
    }


    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final Dictionary that = (Dictionary) other;
        return Objects.equals(this.path, that.path);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public final String toString() {
        return path;
    }


    /**
     * A {@code Dictionary} provided by the plugin.
     * <p>
     * Bundled dictionaries are found in the application's resources.
     */
    public static final class BundledDictionary extends Dictionary {
        /**
         * Constructs a new {@link BundledDictionary} for the given dictionary resource.
         *
         * @param dictionary the location of the dictionary resource
         */
        public BundledDictionary(final String dictionary) {
            super(dictionary);
        }


        /**
         * Returns an {@link InputStream} to the given dictionary resource.
         *
         * @param dictionary the location of the dictionary resource
         * @return an {@link InputStream} to the given dictionary resource
         */
        @Override
        InputStream getInputStream(final String dictionary) {
            return Dictionary.class.getClassLoader().getResourceAsStream(dictionary);
        }
    }

    /**
     * A {@code Dictionary} added by the user.
     */
    public static final class CustomDictionary extends Dictionary {
        /**
         * Constructs a new {@code CustomDictionary} for the given dictionary file.
         *
         * @param dictionary the location of the dictionary file
         */
        public CustomDictionary(final String dictionary) {
            super(dictionary);
        }


        /**
         * Returns an {@link InputStream} of the file at the given absolute path.
         *
         * @param dictionary the absolute location of the dictionary file
         * @return an {@link InputStream} of the file at the given absolute path
         * @throws IOException if the given file could not be found
         */
        @Override
        InputStream getInputStream(final String dictionary) throws IOException {
            return new FileInputStream(dictionary);
        }
    }

    /**
     * An (initially) empty {@code Dictionary} without a source file.
     */
    private static final class SimpleDictionary extends Dictionary {
        /**
         * Constructs a new, empty {@code SimpleDictionary}.
         */
        SimpleDictionary() {
            super();
        }


        /**
         * Throws {@link IllegalStateException}.
         *
         * @param dictionary ignored
         * @return never
         */
        @Override
        InputStream getInputStream(final String dictionary) {
            throw new IllegalStateException("This method should not be called.");
        }
    }
}
