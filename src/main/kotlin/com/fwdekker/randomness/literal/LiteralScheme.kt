package com.fwdekker.randomness.literal

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.util.xmlb.annotations.Transient
import java.awt.Color


/**
 * Contains settings for generating non-random literals.
 *
 * @property literal The literal string.
 * @property capitalization The capitalization mode of the literal.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class LiteralScheme(
    var literal: String = DEFAULT_LITERAL,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    @get:Transient
    override val name = Bundle("literal.title")
    override val typeIcon = BASE_ICON

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)


    /**
     * Returns a list containing the given number of copies of the literal.
     *
     * @param count the number of copies of the literal to generate
     * @return a list containing the given number of copies of the literal
     */
    override fun generateUndecoratedStrings(count: Int) = List(count) { capitalization.transform(literal, random) }


    override fun doValidate() = arrayDecorator.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid))
            .also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for literals.
         */
        val BASE_ICON = TypeIcon(RandomnessIcons.SCHEME, "0x", listOf(Color(248, 19, 19, 154)))


        /**
         * The default value of the [literal] field.
         */
        const val DEFAULT_LITERAL = ""

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN
    }
}
