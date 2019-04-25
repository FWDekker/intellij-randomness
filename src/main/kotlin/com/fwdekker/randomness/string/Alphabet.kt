package com.fwdekker.randomness.string


/**
 * An `Alphabet` represents a named collection of symbols.
 *
 * @param description the description of the alphabet
 * @param symbols the symbols in the alphabet
 */
enum class Alphabet(val description: String, val symbols: String) {
    /**
     * The lowercase English alphabet.
     */
    ALPHABET("Alphabet (a, b, c, ...)", "abcdefghijklmnopqrstuvwxyz"),
    /**
     * The digits 0 through 9.
     */
    DIGITS("Digits (0, 1, 2, ...)", "0123456789"),
    /**
     * The hexadecimal digits 0 through f.
     */
    HEXADECIMAL("Hexadecimal (0, 1, 2, ..., d, e, f)", "0123456789abcdef"),
    /**
     * A minus (`-`).
     */
    MINUS("Minus (-)", "-"),
    /**
     * An underscore (`_`).
     */
    UNDERSCORE("Underscore (_)", "_"),
    /**
     * A whitespace (` `).
     */
    SPACE("Space ( )", " "),
    /**
     * A collection of special characters.
     */
    SPECIAL("Special (!, @, #, $, %, ^, &, *)", "!@#$%^&*"),
    /**
     * A collection of brackets and parentheses.
     */
    BRACKETS("Brackets ((, ), [, ], {, }, <, >)", "()[]{}<>");


    /**
     * Returns the `description` field.
     *
     * @return the `description` field
     */
    override fun toString() = description
}


/**
 * Concatenates the symbols of all the alphabets, removing duplicate characters.
 *
 * @return the concatenation of all symbols of all the alphabets, excluding duplicate characters
 */
fun Iterable<Alphabet>.sum() =
    this.fold("") { acc, alphabet -> acc + (alphabet.symbols.takeUnless { it in acc } ?: "") }
