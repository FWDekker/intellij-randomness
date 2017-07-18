package com.fwdekker.randomness.string;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * An {@code Alphabet} represents a collection of symbols.
 */
enum Alphabet {
    UPPERCASE("Uppercase (A, B, C, ...)", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    LOWERCASE("Lowercase (a, b, c, ...)", "abcdefghijklmnopqrstuvwxyz"),
    DIGITS("Digits (0, 1, 2, ...)", "0123456789"),
    MINUS("Minus (-)", "-"),
    UNDERSCORE("Underscore (_)", "_"),
    SPACE("Space ( )", " "),
    SPECIAL("Special (!, @, #, $, %, ^, &, *)", "!@#$%^&*"),
    BRACKETS("Brackets ((, ), [, ], {, }, <, >)", "()[]{}<>");


    /**
     * The name of the alphabet.
     */
    private final String name;
    /**
     * The symbols in the alphabet.
     */
    private final String symbols;


    /**
     * Constructs a new {@code Alphabet}.
     *
     * @param name    the name of the alphabet
     * @param symbols the symbols in the alphabet
     */
    Alphabet(final String name, final String symbols) {
        this.name = name;
        this.symbols = symbols;
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
     * Returns the name of the alphabet.
     *
     * @return the name of the alphabet
     */
    String getName() {
        return name;
    }

    /**
     * Returns the symbols in the alphabet.
     *
     * @return the symbols in the alphabet
     */
    String getSymbols() {
        return symbols;
    }

    @Override
    public String toString() {
        return name;
    }
}
