package com.fwdekker.randomness.string


/**
 * An `Alphabet` represents a named collection of symbols.
 *
 * @param description the description of the alphabet
 * @param symbols the symbols in the alphabet
 */
enum class Alphabet(val description: String, val symbols: String) {
    ALPHABET("Alphabet (a, b, c, ...)", "abcdefghijklmnopqrstuvwxyz"),
    DIGITS("Digits (0, 1, 2, ...)", "0123456789"),
    HEXADECIMAL("Hexadecimal (0, 1, 2, ..., d, e, f)", "0123456789abcdef"),
    MINUS("Minus (-)", "-"),
    UNDERSCORE("Underscore (_)", "_"),
    SPACE("Space ( )", " "),
    SPECIAL("Special (!, @, #, $, %, ^, &, *)", "!@#$%^&*"),
    BRACKETS("Brackets ((, ), [, ], {, }, <, >)", "()[]{}<>");


    override fun toString() = description

    /**
     * Returns a string that contains the symbols from this and [that] alphabet.
     *
     * @param that another alphabet
     * @return a string that contains the symbols from this and [that] alphabet
     */
    operator fun plus(that: Alphabet) = this.symbols + that.symbols

    /**
     * Returns a string that contains the symbols from this alphabet but without the symbols from [that] alphabet.
     *
     * If a symbol from [that] alphabet is not contained in this alphabet, it will not be contained in the output.
     *
     * @param that another alphabet
     * @return a string that contains the symbols from this alphabet but without the symbols from [that] alphabet
     */
    operator fun minus(that: Alphabet) = this.symbols.takeUnless { it in that.symbols }


    companion object {
        /**
         * Concatenates the symbols of all the alphabets in the given collection.
         *
         * @param alphabets a collection of alphabets
         * @return the concatenation of all symbols of all the alphabets in the given collection
         */
        // TODO Inline this function when tests have been rewritten to Kotlin
        fun concatenate(alphabets: Collection<Alphabet>) =
            alphabets.fold("") { acc, alphabet -> acc + alphabet }
    }
}
