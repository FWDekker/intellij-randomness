package com.fwdekker.randomness.literal

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.util.xmlb.annotations.Transient
import icons.RandomnessIcons


/**
 * Contains settings for generating non-random literals.
 *
 * @property literal The literal string.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class LiteralScheme(
    var literal: String = DEFAULT_LITERAL,
    override var decorator: ArraySchemeDecorator = ArraySchemeDecorator()
) : Scheme() {
    @Transient
    override val name = "Literal"

    override val icons = RandomnessIcons.String


    /**
     * Returns a list containing the given number of copies of the literal.
     *
     * @param count the number of copies of the literal to generate
     * @return a list containing the given number of copies of the literal
     */
    override fun generateUndecoratedStrings(count: Int) = List(count) { literal }


    override fun doValidate() = decorator.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(decorator = decorator.deepCopy(retainUuid))
            .also { if (retainUuid) it.uuid = this.uuid }


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
