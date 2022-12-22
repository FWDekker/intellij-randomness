package com.fwdekker.randomness.array

import com.fwdekker.randomness.BracketsDescriptor
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.SchemeDecorator


/**
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @property enabled `true` if and only if arrays should be generated instead of singular values.
 * @property minCount The minimum number of elements to generate, inclusive.
 * @property maxCount The maximum number of elements to generate, inclusive.
 * @property brackets The brackets to surround arrays with.
 * @property customBrackets The brackets defined in the custom option.
 * @property separator The string to place between generated elements.
 * @property customSeparator The separator defined in the custom option.
 * @property isSpaceAfterSeparator `true` if and only if a space should be placed after each separator.
 */
data class ArrayDecorator(
    var enabled: Boolean = DEFAULT_ENABLED,
    var minCount: Int = DEFAULT_MIN_COUNT,
    var maxCount: Int = DEFAULT_MAX_COUNT,
    var brackets: String = DEFAULT_BRACKETS,
    var customBrackets: String = DEFAULT_CUSTOM_BRACKETS,
    var separator: String = DEFAULT_SEPARATOR,
    var customSeparator: String = DEFAULT_CUSTOM_SEPARATOR,
    var isSpaceAfterSeparator: Boolean = DEFAULT_SPACE_AFTER_SEPARATOR,
) : SchemeDecorator() {
    override val decorators: List<SchemeDecorator> = emptyList()
    override val name = Bundle("array.title")
    override val icon: OverlayedIcon?
        get() = if (enabled) OverlayedIcon(OverlayIcon.ARRAY) else null


    override fun generateUndecoratedStrings(count: Int): List<String> {
        if (!enabled) return generator(count)

        val separator = separator + if (isSpaceAfterSeparator && separator !== "\n") " " else ""
        val bracketsDescriptor = BracketsDescriptor(brackets)

        val partsPerString = random.nextInt(minCount, maxCount + 1)
        val parts = generator(count * partsPerString)
        val stringsWithoutBrackets = parts.chunked(partsPerString) { it.joinToString(separator = separator) }

        return stringsWithoutBrackets.map { bracketsDescriptor.interpolate(it) }
    }


    override fun doValidate() =
        if (minCount < MIN_MIN_COUNT) Bundle("array.error.min_count_too_low", MIN_MIN_COUNT)
        else if (maxCount < minCount) Bundle("array.error.min_count_above_max")
        else BracketsDescriptor(brackets).doValidate()

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
         * The default value of the [brackets] field.
         */
        const val DEFAULT_BRACKETS = "[@]"

        /**
         * The default value of the [customBrackets] field.
         */
        const val DEFAULT_CUSTOM_BRACKETS = "listOf(@)"

        /**
         * The default value of the [separator] field.
         */
        const val DEFAULT_SEPARATOR = ","

        /**
         * The default value of the [customSeparator] field.
         */
        const val DEFAULT_CUSTOM_SEPARATOR = ";"

        /**
         * The default value of the [isSpaceAfterSeparator] field.
         */
        const val DEFAULT_SPACE_AFTER_SEPARATOR = true
    }
}
