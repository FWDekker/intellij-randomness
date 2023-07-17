package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.affix.AffixDecorator


/**
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @property enabled `true` if and only if arrays should be generated instead of singular values.
 * @property minCount The minimum number of elements to generate, inclusive.
 * @property maxCount The maximum number of elements to generate, inclusive.
 * @property affixDecorator The affixation to apply to the generated values.
 * @property separator The string to place between generated elements.
 * @property isSpaceAfterSeparator `true` if and only if a space should be placed after each separator.
 */
data class ArrayDecorator(
    var enabled: Boolean = DEFAULT_ENABLED,
    var minCount: Int = DEFAULT_MIN_COUNT,
    var maxCount: Int = DEFAULT_MAX_COUNT,
    var affixDecorator: AffixDecorator = DEFAULT_AFFIX_DECORATOR,
    var separator: String = DEFAULT_SEPARATOR,
    var isSpaceAfterSeparator: Boolean = DEFAULT_SPACE_AFTER_SEPARATOR,
) : SchemeDecorator() {
    override val decorators: List<SchemeDecorator> = listOf(affixDecorator)
    override val name = Bundle("array.title")
    override val icon: OverlayedIcon?
        get() = if (enabled) OverlayedIcon(OverlayIcon.ARRAY) else null


    override fun generateStrings(count: Int) =
        if (enabled) super.generateStrings(count)
        else generator(count)

    override fun generateUndecoratedStrings(count: Int): List<String> {
        val separator = separator.replace("\\n", "\n") + if (isSpaceAfterSeparator && separator != "\\n") " " else ""
        val partsPerString = random.nextInt(minCount, maxCount + 1)
        val parts = generator(count * partsPerString)
        return parts.chunked(partsPerString) { it.joinToString(separator = separator) }
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
         * The default value of the [affixDecorator] field.
         */
        val DEFAULT_AFFIX_DECORATOR = AffixDecorator(enabled = true, descriptor = "[@]")

        /**
         * The default value of the [separator] field.
         */
        const val DEFAULT_SEPARATOR = ","

        /**
         * The default value of the [isSpaceAfterSeparator] field.
         */
        const val DEFAULT_SPACE_AFTER_SEPARATOR = true
    }
}
