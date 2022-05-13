package com.fwdekker.randomness

import java.util.Locale


/**
 * A mode in which a word should be capitalized.
 *
 * @property descriptor the name of the capitalization mode
 * @property transform the function which capitalizes the given string to the mode's format
 */
enum class CapitalizationMode(val descriptor: String, val transform: (String) -> String) {
    /**
     * Does not change the string.
     */
    RETAIN("retain", { string -> string }),

    /**
     * Makes the first character uppercase and all characters after that lowercase.
     */
    SENTENCE("sentence", { string -> string.toSentenceCase() }),

    /**
     * Makes all characters uppercase.
     */
    UPPER("upper", { string -> string.uppercase(Locale.getDefault()) }),

    /**
     * Makes all characters lowercase.
     */
    LOWER("lower", { string -> string.lowercase(Locale.getDefault()) }),

    /**
     * Makes the first letter of each word uppercase.
     */
    FIRST_LETTER("first letter", { string -> string.split(' ').joinToString(" ") { it.toSentenceCase() } }),

    /**
     * Makes each letter randomly uppercase or lowercase.
     */
    RANDOM("random", { string -> string.toCharArray().map { it.toRandomCase() }.joinToString("") });


    /**
     * Returns the descriptor of the capitalization mode.
     *
     * @return the descriptor of the capitalization mode
     */
    override fun toString() = descriptor


    /**
     * Holds static elements.
     */
    companion object {
        /**
         * Returns the capitalization mode with the given name.
         *
         * @param descriptor the descriptor of the capitalization mode to return
         * @return the capitalization mode with the given descriptor
         */
        fun getMode(descriptor: String) =
            values().firstOrNull { it.descriptor == descriptor }
                ?: throw IllegalArgumentException("There does not exist a capitalization mode with name `$descriptor`.")
    }
}


/**
 * Randomly converts this character to uppercase or lowercase.
 *
 * @return the uppercase or lowercase version of this character
 */
private fun Char.toRandomCase() =
    if (kotlin.random.Random.nextBoolean()) this.lowercaseChar()
    else this.uppercaseChar()

/**
 * Turns the first character uppercase while all other characters become lowercase.
 *
 * @return the sentence-case form of this string
 */
private fun String.toSentenceCase() =
    this.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercaseChar() }
