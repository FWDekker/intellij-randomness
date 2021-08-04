package com.fwdekker.randomness.literal

import com.fwdekker.randomness.Scheme


/**
 * Contains settings for generating non-random literals.
 *
 * @property literal The literal string.
 */
data class LiteralScheme(var literal: String = DEFAULT_LITERAL) : Scheme<LiteralScheme>() {
    /**
     * Returns a list containing the given number of copies of the literal.
     *
     * @param count the number of copies of the literal to generate
     * @return a list containing the given number of copies of the literal
     */
    override fun generateStrings(count: Int) = List(count) { literal }


    override fun deepCopy() = LiteralScheme(literal)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [literal][literal] field.
         */
        const val DEFAULT_LITERAL = ""
    }
}
