package com.fwdekker.randomness.word;

import java.util.Arrays;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.function.Function;


/**
 * A mode in which a word should be capitalized.
 */
public enum CapitalizationMode {
    /**
     * Makes the first character uppercase and all characters after that lowercase.
     */
    NORMAL("normal", string -> (string.length() == 0
            ? ""
            : Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase(Locale.getDefault()))),
    /**
     * Makes all characters uppercase.
     */
    UPPER("upper", string -> string.toUpperCase(Locale.getDefault())),
    /**
     * Makes all characters lowercase.
     */
    LOWER("lower", string -> string.toLowerCase(Locale.getDefault()));


    /**
     * The name of the capitalization mode.
     */
    private final String name;
    /**
     * Capitalizes the given word to the mode's format.
     */
    private final Function<String, String> transform;


    /**
     * Constructs a new capitalization mode.
     *
     * @param name      the name of the capitalization mode
     * @param transform the function which capitalizes the given word to the mode's format
     */
    CapitalizationMode(final String name, final Function<String, String> transform) {
        this.name = name;
        this.transform = transform;
    }

    /**
     * Returns the name of the capitalization mode.
     *
     * @return the name of the capitalization mode
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the function which capitalizes the given word to the mode's format.
     *
     * @return the function which capitalizes the given word to the mode's format
     */
    public Function<String, String> getTransform() {
        return transform;
    }

    /**
     * Returns the name of the capitalization mode.
     *
     * @return the name of the capitalization mode
     */
    @Override
    public String toString() {
        return name;
    }


    /**
     * Returns the capitalization mode with the given name.
     *
     * @param name the name of the capitalization mode to return
     * @return the capitalization mode with the given name
     */
    public static CapitalizationMode getMode(final String name) {
        return Arrays.stream(values())
                .filter(value -> value.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("There does not exist a capitalization mode with name "
                                                                      + "`" + name + "`."));
    }
}
