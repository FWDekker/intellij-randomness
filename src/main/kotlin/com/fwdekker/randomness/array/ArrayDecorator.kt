package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.DecoratorScheme
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.affix.AffixDecorator


/**
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @property enabled `true` if and only if arrays should be generated instead of singular values.
 * @property minCount The minimum number of elements to generate, inclusive.
 * @property maxCount The maximum number of elements to generate, inclusive.
 * @property separatorEnabled Whether to separate elements using [separator].
 * @property separator The string to place between generated elements.
 * @property affixDecorator The affixation to apply to the generated values.
 */
data class ArrayDecorator(
    var enabled: Boolean = DEFAULT_ENABLED,
    var minCount: Int = DEFAULT_MIN_COUNT,
    var maxCount: Int = DEFAULT_MAX_COUNT,
    var separatorEnabled: Boolean = DEFAULT_SEPARATOR_ENABLED,
    var separator: String = DEFAULT_SEPARATOR,
    var affixDecorator: AffixDecorator = DEFAULT_AFFIX_DECORATOR,
) : DecoratorScheme() {
    override val name = Bundle("array.title")
    override val icon get() = if (enabled) OverlayedIcon(OverlayIcon.ARRAY) else null
    override val decorators = listOf(affixDecorator)


    override fun generateStrings(count: Int) =
        if (enabled) super.generateStrings(count)
        else generator(count)

    override fun generateUndecoratedStrings(count: Int): List<String> {
        val partsPerString = random.nextInt(minCount, maxCount + 1)
        return generator(count * partsPerString)
            .chunked(partsPerString) { it.joinToString(if (separatorEnabled) separator.replace("\\n", "\n") else "") }
    }


    override fun doValidate() =
        if (minCount < MIN_MIN_COUNT) Bundle("array.error.min_count_too_low", MIN_MIN_COUNT)
        else if (maxCount < minCount) Bundle("array.error.min_count_above_max")
        else affixDecorator.doValidate()

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
         * The minimum valid value of the [minCount] field.
         */
        const val MIN_MIN_COUNT = 1

        /**
         * The default value of the [minCount] field.
         */
        const val DEFAULT_MIN_COUNT = 3

        /**
         * The default value of the [maxCount] field.
         */
        const val DEFAULT_MAX_COUNT = 3

        /**
         * The default value of the [separatorEnabled] field.
         */
        const val DEFAULT_SEPARATOR_ENABLED = true

        /**
         * The default value of the [separator] field.
         */
        const val DEFAULT_SEPARATOR = ", "

        /**
         * The default value of the [affixDecorator] field.
         */
        val DEFAULT_AFFIX_DECORATOR = AffixDecorator(enabled = true, descriptor = "[@]")
    }
}
