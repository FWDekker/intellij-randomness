package com.fwdekker.randomness.array

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

        val partsPerString = random.nextInt(minCount, maxCount + 1)
        val parts = generator(count * partsPerString)
        val stringsWithoutBrackets = parts.chunked(partsPerString) { it.joinToString(separator = separator) }

        val bracketGroups =
            if (!brackets.contains(SUBSTITUTION_MARK_REGEX))
                List(2) { brackets.collapseEscapes() }
            else
                brackets.splitByCapturesOf(SUBSTITUTION_MARK_REGEX)

        return stringsWithoutBrackets.map { bracketGroups.joinToString(separator = it) }
    }


    override fun doValidate() =
        if (minCount < MIN_MIN_COUNT) Bundle("array.error.min_count_too_low", MIN_MIN_COUNT)
        else if (maxCount < minCount) Bundle("array.error.min_count_above_max")
        else if (brackets.contains(Regex("""(?<!\\)(?:\\\\)*(\\)(?![\\@])"""))) Bundle("array.error.unmatched_escape")
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

        /**
         * Regex that matches an unescaped substitution mark, i.e. an `@` that is preceded by an event amount of `\`s.
         */
        private val SUBSTITUTION_MARK_REGEX = Regex("""(?<!\\)(?:\\\\)*(@)""")
    }
}


/**
 * Collapses escaped symbols into the symbols they escape, e.g. with `\\` turned into `\`.
 *
 * @return the string with escaped symbols collapsed
 */
private fun String.collapseEscapes() = this.replace("\\@", "@").replace("\\\\", "\\")

/**
 * Similar to `String#split`, except that it splits only at the capturing groups, rather than at all groups.
 *
 * @param regex the regex with which to split
 */
private fun String.splitByCapturesOf(regex: Regex) =
    regex.findAll(this)
        .flatMap { match -> match.groups.drop(1).mapNotNull { it?.range } }
        .invert(this.length)
        .map { this.slice(it).collapseEscapes() }

/**
 * Assuming `this` is a sequence of consecutive non-overlapping `IntRange`s with all values below [max], returns the
 * sequence of consecutive non-overlapping `IntRange`s that fill the gaps between the `IntRange`s of `this`, starting at
 * `0` and ending at [max].
 *
 * @param max the value at which the last returned `IntRange` should end
 */
private fun Sequence<IntRange>.invert(max: Int) =
    this.fold(listOf(0..max)) { acc, range ->
        acc.dropLast(1) + listOf(
            acc.last().first until range.first,
            (range.last + 1) until max
        )
    }
