package com.fwdekker.randomness.string


/**
 * A `SymbolSet` represents a named collection of symbols.
 *
 * @param name the name of the symbol set
 * @param symbols the symbols in the symbol set
 */
data class SymbolSet(var name: String, var symbols: String) {
    companion object {
        /**
         * Symbols that look like other symbols.
         *
         * To be precise, this string contains the symbols `0`, `1`, `l`, `I`, `O`, `|`, and `﹒`.
         */
        const val lookAlikeCharacters = "01lLiIoO|﹒"

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
 * Converts a collection of symbol sets to a map from the symbol sets' names to the respective symbols.
 *
 * @return a map from the symbol sets' names to the respective symbols
 */
fun Collection<SymbolSet>.toMap() = this.map { (name, symbols) -> name to symbols }.toMap()

/**
 * Converts a map to a list of symbol sets, using the key as the name and the value as the symbols.
 *
 * @return a list of symbol sets
 */
fun Map<String, String>.toSymbolSets() = this.map { (name, symbols) -> SymbolSet(name, symbols) }.toList()

/**
 * Concatenates the symbols of all the symbol sets, removing duplicate characters.
 *
 * @param excludeLookAlikeSymbols whether to remove symbols that occur in [SymbolSet.lookAlikeCharacters]
 * @return the concatenation of all symbols of all the symbol sets, excluding duplicate characters
 */
fun Iterable<SymbolSet>.sum(excludeLookAlikeSymbols: Boolean = false) =
    this.fold("") { acc, symbolSet -> acc + symbolSet.symbols }.toSet().joinToString("")
        .let { sum ->
            if (excludeLookAlikeSymbols) sum.filterNot { it in SymbolSet.lookAlikeCharacters }
            else sum
        }
