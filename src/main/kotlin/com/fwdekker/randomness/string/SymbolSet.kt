package com.fwdekker.randomness.string


/**
 * A `SymbolSet` represents a named collection of symbols.
 *
 * @param name the name of the symbol set
 * @param symbols the symbols in the symbol set
 */
data class SymbolSet(val name: String, val symbols: String) {
    companion object {
        /**
         * The lowercase English alphabet.
         */
        val ALPHABET = SymbolSet("Alphabet (a, b, c, ...)", "abcdefghijklmnopqrstuvwxyz")
        /**
         * The digits 0 through 9.
         */
        val DIGITS = SymbolSet("Digits (0, 1, 2, ...)", "0123456789")
        /**
         * The hexadecimal digits 0 through f.
         */
        val HEXADECIMAL = SymbolSet("Hexadecimal (0, 1, 2, ..., d, e, f)", "0123456789abcdef")
        /**
         * A minus (`-`).
         */
        val MINUS = SymbolSet("Minus (-)", "-")
        /**
         * An underscore (`_`).
         */
        val UNDERSCORE = SymbolSet("Underscore (_)", "_")
        /**
         * A whitespace (` `).
         */
        val SPACE = SymbolSet("Space ( )", " ")
        /**
         * A collection of special characters.
         */
        val SPECIAL = SymbolSet("Special (!, @, #, $, %, ^, &, *)", "!@#$%^&*")
        /**
         * A collection of brackets and parentheses.
         */
        val BRACKETS = SymbolSet("Brackets ((, ), [, ], {, }, <, >)", "()[]{}<>")

        /**
         * List of default symbol sets.
         */
        val defaultSymbolSets = listOf(ALPHABET, DIGITS, HEXADECIMAL, MINUS, UNDERSCORE, SPACE, SPECIAL, BRACKETS)
    }


    /**
     * Returns the `name` field.
     *
     * @return the `name` field
     */
    override fun toString() = name
}


/**
 * Concatenates the symbols of all the symbol sets, removing duplicate characters.
 *
 * @return the concatenation of all symbols of all the symbol sets, excluding duplicate characters
 */
fun Iterable<SymbolSet>.sum() =
    this.fold("") { acc, symbolSet -> acc + (symbolSet.symbols.takeUnless { it in acc } ?: "") }
