package com.fwdekker.randomness

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
    UPPER({ string, _ -> string.uppercase() }),

    /**
     * Makes all characters lowercase.
     */
    LOWER({ string, _ -> string.lowercase() }),

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
    fun toLocalizedString() = Bundle("shared.capitalization.${toString().replace(' ', '_').lowercase()}")
}


/**
 * @see CapitalizationMode.RANDOM
 */
private fun Char.toRandomCase(random: Random) =
    if (random.nextBoolean()) lowercaseChar()
    else uppercaseChar()

/**
 * @see CapitalizationMode.SENTENCE
 */
private fun String.toSentenceCase() = lowercase().replaceFirstChar { it.uppercaseChar() }

/**
 * Turns the first character lowercase while all other characters remain unchanged.
 */
fun String.lowerCaseFirst() = take(1).lowercase() + drop(1)

/**
 * Turns the first character uppercase while all other characters remain unchanged.
 */
fun String.upperCaseFirst() = take(1).uppercase() + drop(1)

/**
 * Appends [other], ensuring the resulting string is in camel case as long as [this] and [other] are also in camel case.
 */
fun String.camelPlus(other: String) =
    if (this == "") other
    else this + other.upperCaseFirst()
