package com.fwdekker.randomness.string;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * An {@code Alphabet} represents a collection of symbols.
 */
enum Alphabet {
    UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "Uppercase (A, B, C, ...)"),
    LOWERCASE("abcdefghijklmnopqrstuvwxyz", "Lowercase (a, b, c, ...)"),
    DIGITS("0123456789", "Digits (0, 1, 2, ...)"),
    MINUS("-", "Minus (-)"),
    UNDERSCORE("_", "Underscore (_)"),
    SPACE(" ", "Space ( )"),
    SPECIAL("!@#$%^&*", "Special (!, @, #, $, %, ^, &, *)"),
    BRACKETS("()[]{}<>", "Brackets ((, ), [, ], {, }, <, >)");


    /**
     * The symbols in the alphabet.
     */
    private final String symbols;
    /**
     * A human-readable description of the alphabet.
     */
    private final String description;


    /**
     * Constructs a new {@code Alphabet}.
     *
     * @param symbols     the symbols in the alphabet
     * @param description a human-readable description of the alphabet
     */
    Alphabet(final String symbols, final String description) {
        this.symbols = symbols;
        this.description = description;
    }


    /**
     * Concatenates the symbols of all the alphabets in the given collection.
     *
     * @param alphabets a collection of alphabets
     * @return the concatenation of all symbols of all the alphabets in the given collection
     */
    static String concatenate(final Collection<Alphabet> alphabets) {
        return alphabets.stream()
                .map(Alphabet::getSymbols)
                .collect(Collectors.joining());
    }


    /**
     * Returns the symbols in the alphabet.
     *
     * @return the symbols in the alphabet
     */
    String getSymbols() {
        return symbols;
    }

    /**
     * Returns a human-readable description of the alphabet.
     *
     * @return a human-readable description of the alphabet
     */
    String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
