package com.fwdekker.randomness.array

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.SchemeDecorator


/**
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @property enabled True if and only if arrays should be generated instead of singular values.
 * @property minCount The minimum number of elements to generate.
 * @property maxCount The maximum number of elements to generate.
 * @property brackets The brackets to surround arrays with.
 * @property separator The string to place between generated elements.
 * @property isSpaceAfterSeparator True if and only if a space should be placed after each separator.
 */
data class ArrayDecorator(
    var enabled: Boolean = DEFAULT_ENABLED,
    var minCount: Int = DEFAULT_MIN_COUNT,
    var maxCount: Int = DEFAULT_MAX_COUNT,
    var brackets: String = DEFAULT_BRACKETS,
    var separator: String = DEFAULT_SEPARATOR,
    var isSpaceAfterSeparator: Boolean = DEFAULT_SPACE_AFTER_SEPARATOR
) : SchemeDecorator() {
    override val decorators: List<SchemeDecorator> = emptyList()
    override val name = Bundle("array.title")
    override val icon: OverlayedIcon?
        get() = if (enabled) OverlayedIcon(OverlayIcon.ARRAY) else null


    override fun generateUndecoratedStrings(count: Int): List<String> {
        if (!enabled) return generator(count)

        val countPerString = random.nextInt(minCount, maxCount + 1)
        val generatedParts = generator(count * countPerString)
        val separator = this.separator + if (isSpaceAfterSeparator && this.separator !== "\n") " " else ""

        return generatedParts.chunked(countPerString) {
            it.joinToString(
                separator = separator,
                prefix = brackets.getOrNull(0)?.toString() ?: "",
                postfix = brackets.getOrNull(1)?.toString() ?: ""
            )
        }
    }


    override fun doValidate() =
        if (minCount > maxCount) "Minimum count should be greater than maximum count."
        else if (minCount < MIN_COUNT) "Minimum count should be at least $MIN_COUNT, but is $minCount."
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
         * The minimum valid value of the [count] field.
         */
        const val MIN_COUNT = 1

        /**
         * The default value of the [minCount] field.
         */
        const val DEFAULT_MIN_COUNT = 3

        /**
         * The default value of the [maxCount] field.
         */
        const val DEFAULT_MAX_COUNT = 5

        /**
         * The default value of the [brackets] field.
         */
        const val DEFAULT_BRACKETS = "[]"

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
