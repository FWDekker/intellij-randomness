package com.fwdekker.randomness.word;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A dictionary of English words.
 */
public abstract class Dictionary implements PersistentStateComponent<Dictionary>, Comparable<Dictionary> {
    /**
     * The name of the default dictionary file.
     */
    public static final String DEFAULT_DICTIONARY_FILE = "words_alpha.dic";
    /**
     * The default {@code Dictionary} instance.
     */
    private static final Dictionary DEFAULT_DICTIONARY = new ResourceDictionary(DEFAULT_DICTIONARY_FILE);

    /**
     * The filename of the dictionary file.
     */
    private final String dictionary;
    /**
     * A set of all words in the dictionary.
     */
    @Transient
    private final Set<String> words;


    /**
     * Constructs an empty {@code Dictionary}.
     */
    private Dictionary() {
        dictionary = "";
        words = new HashSet<>();
    }

    /**
     * Constructs a new {@code Dictionary} from the given resource file.
     *
     * @param dictionary the filename of the dictionary file
     */
    Dictionary(final String dictionary) {
        this.dictionary = dictionary;

        // Read dictionary into memory
        try (InputStream iStream = getInputStream(dictionary)) {
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


    abstract InputStream getInputStream(final String dictionary) throws IOException;


    /**
     * Returns the filename of the dictionary file.
     *
     * @return the filename of the dictionary file
     */
    public final String getDictionary() {
        return dictionary;
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
        return Objects.equals(this.dictionary, that.dictionary);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(dictionary);
    }

    @Override
    public String toString() {
        return dictionary;
    }

    @Override
    public int compareTo(final @NotNull Dictionary that) {
        if (this.equals(that)) {
            return 0;
        } else if (Integer.compare(this.hashCode(), that.hashCode()) >= 0) {
            return 1;
        } else {
            return -1;
        }
    }

    @Nullable
    @Override
    public Dictionary getState() {
        return this;
    }

    @Override
    public void loadState(final Dictionary state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    public static final class ResourceDictionary extends Dictionary {
        public ResourceDictionary(final String dictionary) {
            super(dictionary);
        }


        @Override
        InputStream getInputStream(final String dictionary) {
            return Dictionary.class.getClassLoader().getResourceAsStream(dictionary);
        }
    }

    public static final class LocalDictionary extends Dictionary {
        public LocalDictionary(final String dictionary) {
            super(dictionary);
        }


        @Override
        InputStream getInputStream(final String dictionary) throws IOException {
            return new FileInputStream(dictionary);
        }
    }

    private static final class SimpleDictionary extends Dictionary {
        SimpleDictionary() {
            super();
        }


        @Override
        InputStream getInputStream(final String dictionary) {
            throw new IllegalStateException("This method should not be called.");
        }
    }
}
