package com.fwdekker.randomness.string


/**
 * An `Alphabet` represents a named collection of symbols.
 *
 * @param description the description of the alphabet
 * @param symbols the symbols in the alphabet
 */
data class Alphabet(val description: String, val symbols: String) {
    companion object {
        /**
         * The lowercase English alphabet.
         */
        val ALPHABET = Alphabet("Alphabet (a, b, c, ...)", "abcdefghijklmnopqrstuvwxyz")
        /**
         * The digits 0 through 9.
         */
        val DIGITS = Alphabet("Digits (0, 1, 2, ...)", "0123456789")
        /**
         * The hexadecimal digits 0 through f.
         */
        val HEXADECIMAL = Alphabet("Hexadecimal (0, 1, 2, ..., d, e, f)", "0123456789abcdef")
        /**
         * A minus (`-`).
         */
        val MINUS = Alphabet("Minus (-)", "-")
        /**
         * An underscore (`_`).
         */
        val UNDERSCORE = Alphabet("Underscore (_)", "_")
        /**
         * A whitespace (` `).
         */
        val SPACE = Alphabet("Space ( )", " ")
        /**
         * A collection of special characters.
         */
        val SPECIAL = Alphabet("Special (!, @, #, $, %, ^, &, *)", "!@#$%^&*")
        /**
         * A collection of brackets and parentheses.
         */
        val BRACKETS = Alphabet("Brackets ((, ), [, ], {, }, <, >)", "()[]{}<>")

        /**
         * List of default alphabets.
         */
        val defaultAlphabets = listOf(ALPHABET, DIGITS, HEXADECIMAL, MINUS, UNDERSCORE, SPACE, SPECIAL, BRACKETS)
    }


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
