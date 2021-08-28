package com.fwdekker.randomness.array

import com.fwdekker.randomness.SchemeDecorator


/**
 * The user-configurable collection of schemes applicable to generating arrays.
 *
 * @property enabled True if and only if arrays should be generated instead of singular values.
 * @property count The number of elements to generate.
 * @property brackets The brackets to surround arrays with.
 * @property separator The string to place between generated elements.
 * @property isSpaceAfterSeparator True if and only if a space should be placed after each separator.
 */
data class ArraySchemeDecorator(
    var enabled: Boolean = DEFAULT_ENABLED,
    var count: Int = DEFAULT_COUNT,
    var brackets: String = DEFAULT_BRACKETS,
    var separator: String = DEFAULT_SEPARATOR,
    var isSpaceAfterSeparator: Boolean = DEFAULT_SPACE_AFTER_SEPARATOR
) : SchemeDecorator() {
    override val decorator: Nothing? = null

    override val name = "Array"


    override fun generateUndecoratedStrings(count: Int): List<String> =
        if (!enabled)
            generator(count)
        else
            generator(count * this.count)
                .chunked(this.count) { strings ->
                    strings.joinToString(
                        separator = this.separator + if (isSpaceAfterSeparator && this.separator !== "\n") " " else "",
                        prefix = brackets.getOrNull(0)?.toString() ?: "",
                        postfix = brackets.getOrNull(1)?.toString() ?: ""
                    )
                }


    override fun doValidate() =
        if (count < MIN_COUNT) "Minimum count should be at least $MIN_COUNT, but is $count."
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
         * The default value of the [count] field.
         */
        const val DEFAULT_COUNT = 5

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