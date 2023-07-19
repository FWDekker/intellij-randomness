package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.SchemeDecorator


/**
 * Forces generated strings to be exactly [length] characters.
 *
 * @property enabled Whether to apply this decorator.
 * @property length The enforced length.
 * @property filler The character to pad strings that are too short with.
 */
data class FixedLengthDecorator(
    var enabled: Boolean = DEFAULT_ENABLED,
    var length: Int = DEFAULT_LENGTH,
    var filler: String = DEFAULT_FILLER,
) : SchemeDecorator() {
    override val name = Bundle("fixed_length.title")
    override val decorators: List<SchemeDecorator> = emptyList()


    override fun generateStrings(count: Int) =
        if (enabled) super.generateStrings(count)
        else generator(count)

    override fun generateUndecoratedStrings(count: Int): List<String> =
        generator(count).map { it.take(length).padStart(length, filler[0]) }


    override fun doValidate() =
        if (length < MIN_LENGTH) Bundle("fixed_length.error.length_too_low", MIN_LENGTH)
        else if (filler.length != 1) Bundle("fixed_length.error.filler_length")
        else null

    override fun deepCopy(retainUuid: Boolean) = copy().also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [enabled] field.
         */
        const val DEFAULT_ENABLED = false

        /**
         * The minimum valid value of the [length] field.
         */
        const val MIN_LENGTH = 1

        /**
         * The default value of the [length] field.
         */
        const val DEFAULT_LENGTH = 3

        /**
         * The default value of the [filler] field.
         */
        const val DEFAULT_FILLER = "0"
    }
}
