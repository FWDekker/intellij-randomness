package com.fwdekker.randomness.word;

import com.intellij.openapi.ui.ValidationInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
     * The unique identifier of the dictionary.
     */
    private final String uid;
    /**
     * The human-readable name of the dictionary.
     */
    private final String name;
    /**
     * A set of all words in the dictionary.
     */
    private final Set<String> words;


    /**
     * Constructs an empty {@code Dictionary}.
     */
    private Dictionary() {
        uid = UUID.randomUUID().toString();
        name = uid;
        words = new HashSet<>();
    }

    /**
     * Constructs a new {@code Dictionary} from the given resource file.
     *
     * @param uid   the unique identifier of the dictionary
     * @param name  the human-readable name of the dictionary
     * @param input the {@code InputStream} containing the dictionary's contents
     */
    protected Dictionary(final String uid, final String name, final InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("Failed to read dictionary into memory.");
        }

        this.uid = uid;
        this.name = name;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            words = reader.lines().collect(Collectors.toSet());
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to read dictionary into memory.", e);
        }

        if (words.isEmpty()) {
            throw new IllegalArgumentException("Dictionary must be non-empty.");
        }
    }


    /**
     * Returns the unique identifier of the dictionary.
     *
     * @return the unique identifier of the dictionary
     */
    public final String getUid() {
        return uid;
    }

    /**
     * Returns the human-readable name of the dictionary.
     *
     * @return the human-readable name of the dictionary
     */
    public final String getName() {
        return name;
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
     * Returns the shortest word in this {@code Dictionary}.
     *
     * @return the shortest word in this {@code Dictionary}
     */
    public final String getShortestWord() {
        return words.parallelStream()
                .min(Comparator.comparingInt(String::length))
                .orElseThrow(() -> new IllegalStateException("Dictionary should not be empty."));
    }

    /**
     * Returns the longest word in this {@code Dictionary}.
     *
     * @return the longest word in this {@code Dictionary}
     */
    public final String getLongestWord() {
        return words.parallelStream()
                .max(Comparator.comparingInt(String::length))
                .orElseThrow(() -> new IllegalStateException("Dictionary should not be empty."));
    }

    /**
     * Detects whether this dictionary is still valid.
     * <p>
     * Depending on the underlying model, the dictionary may become invalid or even disappear. This method detects such
     * problems.
     *
     * @return {@code null} if this dictionary is valid, or a {@code ValidationInfo} explaining why it is invalid
     */
    public abstract ValidationInfo validate();


    /**
     * Combines the given {@code Dictionary Dictionary(s)} into a single {@code Dictionary}.
     *
     * @param dictionaries the {@code Dictionary Dictionary(s)} to combine
     * @return a {@code Dictionary} containing all words in the given {@code Dictionary Dictionary(s)}
     */
    public static Dictionary combine(final Collection<Dictionary> dictionaries) {
        final Dictionary combinedDictionary = new SimpleDictionary();

        dictionaries.forEach(dictionary -> combinedDictionary.words.addAll(dictionary.words));

        return combinedDictionary;
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
        return Objects.equals(this.uid, that.uid);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(uid);
    }


    /**
     * A {@code Dictionary} provided by the plugin.
     * <p>
     * Bundled dictionaries are found in the application's resources.
     */
    public static final class BundledDictionary extends Dictionary {
        /**
         * A cache of previously created {@code BundledDictionary(s)}.
         */
        private static final Map<String, BundledDictionary> CACHE = new HashMap<>();


        /**
         * Constructs a new {@link BundledDictionary} for the given dictionary resource.
         *
         * @param path the path to the dictionary resource
         */
        private BundledDictionary(final String path) {
            super(path, new File(path).getName(), getInputStream(path));
        }

        /**
         * Constructs a new {@code BundledDictionary} for the given dictionary resource, or returns the previously
         * created instance of this resource if there is one.
         *
         * @param path the path to the dictionary resource
         * @return a new {@code BundledDictionary} for the given dictionary resource, or returns the previously
         * created instance of this resource if there is one
         */
        public static BundledDictionary get(final String path) {
            synchronized (BundledDictionary.class) {
                if (!CACHE.containsKey(path)) {
                    CACHE.put(path, new BundledDictionary(path));
                }
            }

            return CACHE.get(path);
        }


        @Override
        public ValidationInfo validate() {
            try {
                getInputStream(getUid());
            } catch (final IllegalArgumentException e) {
                return new ValidationInfo("The dictionary resource for " + getName() + " does not exist.");
            }

            return null;
        }

        @Override
        public String toString() {
            return "[bundled] " + getName();
        }


        /**
         * Returns an {@link InputStream} to the given dictionary resource.
         *
         * @param dictionary the location of the dictionary resource
         * @return an {@link InputStream} to the given dictionary resource
         */
        private static InputStream getInputStream(final String dictionary) {
            return Dictionary.class.getClassLoader().getResourceAsStream(dictionary);
        }
    }

    /**
     * A {@code Dictionary} added by the user.
     */
    public static final class UserDictionary extends Dictionary {
        /**
         * A cache of previously created {@code BundledDictionary(s)}.
         */
        private static final Map<String, UserDictionary> CACHE = new HashMap<>();


        /**
         * Constructs a new {@code UserDictionary} for the given dictionary file.
         *
         * @param path the absolute path to the dictionary file
         */
        private UserDictionary(final String path) {
            super(path, new File(path).getName(), getInputStream(path));
        }

        /**
         * Constructs a new {@code UserDictionary} for the given dictionary file, or returns the previously created
         * instance of this file if there is one.
         *
         * @param path the absolute path to the dictionary file
         * @return a new {@code UserDictionary} for the given dictionary file, or returns the previously created
         * instance of this file if there is one
         */
        public static UserDictionary get(final String path) {
            synchronized (UserDictionary.class) {
                if (!CACHE.containsKey(path)) {
                    CACHE.put(path, new UserDictionary(path));
                }
            }

            return CACHE.get(path);
        }


        @Override
        public ValidationInfo validate() {
            final File file = new File(getUid());
            if (!file.exists()) {
                return new ValidationInfo("The dictionary file for " + getName() + " does not exist.");
            }
            if (!file.canRead()) {
                return new ValidationInfo("The dictionary file for " + getName() + " exists, but could not be read.");
            }

            return null;
        }

        @Override
        public String toString() {
            return "[custom] " + getName();
        }


        /**
         * Returns an {@link InputStream} of the file at the given absolute path.
         *
         * @param dictionary the absolute location of the dictionary file
         * @return an {@link InputStream} of the file at the given absolute path
         */
        private static InputStream getInputStream(final String dictionary) {
            try {
                return new FileInputStream(dictionary);
            } catch (final FileNotFoundException e) {
                throw new IllegalArgumentException("Failed to read dictionary into memory.", e);
            }
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


        @Override
        public ValidationInfo validate() {
            return null;
        }
    }
}
