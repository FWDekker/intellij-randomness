package com.fwdekker.randomness.literal

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.array.ArraySchemeDecorator


/**
 * Contains settings for generating non-random literals.
 *
 * @property literal The literal string.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class LiteralScheme(
    var literal: String = DEFAULT_LITERAL,
    override var arrayDecorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme<LiteralScheme>() {
    /**
     * Returns a list containing the given number of copies of the literal.
     *
     * @param count the number of copies of the literal to generate
     * @return a list containing the given number of copies of the literal
     */
    override fun generateUndecoratedStrings(count: Int) = List(count) { literal }


    override fun deepCopy() = copy()


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
