package com.fwdekker.randomness

import java.util.Locale
import kotlin.random.Random


/**
 * A mode in which a word should be capitalized.
 *
 * @property transform The function which capitalizes the given string to the mode's format.
 */
enum class CapitalizationMode(val transform: (String, Random) -> String) {
    /**
     * Does not change the string.
     */
    RETAIN({ string, _ -> string }),

    /**
     * Makes the first character uppercase and all characters after that lowercase.
     */
    SENTENCE({ string, _ -> string.toSentenceCase() }),

    /**
     * Makes all characters uppercase.
     */
    UPPER({ string, _ -> string.uppercase(Locale.getDefault()) }),

    /**
     * Makes all characters lowercase.
     */
    LOWER({ string, _ -> string.lowercase(Locale.getDefault()) }),

    /**
     * Makes the first letter of each word uppercase.
     */
    FIRST_LETTER({ string, _ -> string.split(' ').joinToString(" ") { it.toSentenceCase() } }),

    /**
     * Makes each letter randomly uppercase or lowercase.
     */
    RANDOM({ string, random -> string.toCharArray().map { it.toRandomCase(random) }.joinToString("") }),

    /**
     * Unused in production code.
     */
    DUMMY({ string, _ -> string }),
}


/**
 * Randomly converts this character to uppercase or lowercase.
 *
 * @param random the source of randomness to use
 * @return the uppercase or lowercase version of this character
 */
private fun Char.toRandomCase(random: Random) =
    if (random.nextBoolean()) this.lowercaseChar()
    else this.uppercaseChar()

/**
 * Turns the first character uppercase while all other characters become lowercase.
 *
 * @return the sentence-case form of this string
 */
private fun String.toSentenceCase() =
    this.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() }
