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
    ;


    /**
     * Returns the localized string name of this mode.
     */
    fun toLocalizedString() =
        Bundle("shared.capitalization.${toString().replace(' ', '_').lowercase(Locale.getDefault())}")
}


/**
 * Randomly converts this character to uppercase or lowercase using [random] as a source of randomness.
 */
private fun Char.toRandomCase(random: Random) =
    if (random.nextBoolean()) this.lowercaseChar()
    else this.uppercaseChar()

/**
 * Turns the first character uppercase while all other characters become lowercase.
 */
private fun String.toSentenceCase() =
    this.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() }
