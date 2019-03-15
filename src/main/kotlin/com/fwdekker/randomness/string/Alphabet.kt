package com.fwdekker.randomness.string


/**
 * An `Alphabet` represents a collection of symbols.
 *
 * @param description    the description of the alphabet
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


    companion object {
        /**
         * Concatenates the symbols of all the alphabets in the given collection.
         *
         * @param alphabets a collection of alphabets
         * @return the concatenation of all symbols of all the alphabets in the given collection
         */
        fun concatenate(alphabets: Collection<Alphabet>): String {
            return alphabets.joinToString("") { it.symbols }
        }
    }
}
