package com.fwdekker.randomness

import java.util.Locale
import kotlin.random.Random


/**
 * A mode in which a word should be capitalized.
 *
 * @property descriptor The name of the capitalization mode.
 * @property transformer The function which capitalizes the given string to the mode's format.
 */
enum class CapitalizationMode(val descriptor: String, private val transformer: (String, Random) -> String) {
    /**
     * Does not change the string.
     */
    RETAIN("retain", { string, _ -> string }),

    /**
     * Makes the first character uppercase and all characters after that lowercase.
     */
    SENTENCE("sentence", { string, _ -> string.toSentenceCase() }),

    /**
     * Makes all characters uppercase.
     */
    UPPER("upper", { string, _ -> string.uppercase(Locale.getDefault()) }),

    /**
     * Makes all characters lowercase.
     */
    LOWER("lower", { string, _ -> string.lowercase(Locale.getDefault()) }),

    /**
     * Makes the first letter of each word uppercase.
     */
    FIRST_LETTER("first letter", { string, _ -> string.split(' ').joinToString(" ") { it.toSentenceCase() } }),

    /**
     * Makes each letter randomly uppercase or lowercase.
     */
    RANDOM("random", { string, random -> string.toCharArray().map { it.toRandomCase(random) }.joinToString("") }),

    /**
     * Unused in production code.
     */
    DUMMY("dummy", { string, _ -> string }), ;


    /**
     * Invokes [transformer] with [random].
     *
     * @param string the string to transform
     * @param random the random instance to use for transforming
     * @return the returned value of [transformer]
     */
    fun transform(string: String, random: Random = Random.Default) = transformer(string, random)

    /**
     * Returns the [descriptor] of the capitalization mode.
     *
     * @return the [descriptor] of the capitalization mode
     */
    override fun toString() = descriptor


    /**
     * Holds static elements.
     */
    companion object {
        /**
         * Returns the capitalization mode corresponding to [descriptor].
         *
         * @param descriptor the descriptor of the capitalization mode to return
         * @return the capitalization mode corresponding to [descriptor]
         */
        fun getMode(descriptor: String) =
            values().firstOrNull { it.descriptor == descriptor }
                ?: throw IllegalArgumentException(Bundle("shared.capitalization.error.name_not_found", descriptor))
    }
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
