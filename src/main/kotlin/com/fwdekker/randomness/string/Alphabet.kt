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
}


/**
 * Concatenates the symbols of all the alphabets.
 *
 * @return the concatenation of all symbols of all the alphabets
 */
fun Iterable<Alphabet>.sum() =
    this.fold("") { acc, alphabet -> acc + (alphabet.symbols.takeUnless { it in acc } ?: "") }
