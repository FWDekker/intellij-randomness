package com.fwdekker.randomness.string

import com.vdurmont.emoji.EmojiParser


/**
 * Represents a named collection of symbols.
 *
 * @property name The name of the symbol set.
 * @property symbols The symbols in the symbol set.
 */
data class SymbolSet(var name: String = "", var symbols: String = "") {
    /**
     * Returns [name].
     *
     * @return [name]]
     */
    override fun toString() = name


    /**
     * Holds constants.
     */
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
}


/**
 * Combines the symbols of all the symbol sets, optionally removing duplicate characters.
 *
 * This method respects emoji sequences and will not remove duplicate characters if these characters are essential to
 * displaying the embedded emoji correctly.
 *
 * @param excludeLookAlikeSymbols whether to remove symbols that occur in [SymbolSet.lookAlikeCharacters]
 * @return a list of all symbols in all active symbol sets, optionally excluding duplicate characters
 */
fun Iterable<SymbolSet>.sum(excludeLookAlikeSymbols: Boolean = false): List<String> =
    this.fold("") { acc, symbolSet -> acc + symbolSet.symbols }
        .let { Pair(EmojiParser.extractEmojis(it).distinct(), EmojiParser.removeAllEmojis(it).toList().distinct()) }
        .let { (emoji, noEmoji) -> Pair(emoji, noEmoji.map { it.toString() }) }
        .let { (emoji, noEmoji) ->
            emoji +
                if (excludeLookAlikeSymbols) noEmoji.filterNot { it in SymbolSet.lookAlikeCharacters }
                else noEmoji
        }
