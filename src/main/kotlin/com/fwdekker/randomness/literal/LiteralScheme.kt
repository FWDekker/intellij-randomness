package com.fwdekker.randomness.literal

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.util.xmlb.annotations.Transient
import icons.RandomnessIcons


/**
 * Contains settings for generating non-random literals.
 *
 * @property literal The literal string.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class LiteralScheme(
    var literal: String = DEFAULT_LITERAL,
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    @Transient
    override val name = "Literal"
    override val icons = RandomnessIcons.String

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)


    /**
     * Returns a list containing the given number of copies of the literal.
     *
     * @param count the number of copies of the literal to generate
     * @return a list containing the given number of copies of the literal
     */
    override fun generateUndecoratedStrings(count: Int) = List(count) { literal }


    override fun doValidate() = arrayDecorator.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid))
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
