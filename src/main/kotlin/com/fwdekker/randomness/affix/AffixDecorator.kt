package com.fwdekker.randomness.affix

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.SchemeDecorator


/**
 * Decorates a string by adding a prefix and suffix.
 *
 * @property enabled Whether to apply this decorator.
 * @property descriptor The description of the affix. Here, `'\'` is the escape character (which also escapes itself),
 * and each unescaped `'@'` is replaced with the original string. If the descriptor does not contain an unescaped `'@'`,
 * then the entire descriptor is placed both in front of and after the original string. For example, affixing `"word"`
 * with descriptor `"(@)"` gives `"(word)"`, and affixing `"word"` with descriptor `"()"` gives `"()word()"`.
 */
data class AffixDecorator(
    var enabled: Boolean = DEFAULT_ENABLED,
    var descriptor: String = DEFAULT_DESCRIPTOR,
) : SchemeDecorator() {
    override val decorators: List<SchemeDecorator> = emptyList()
    override val name = Bundle("affix.title")


    override fun generateStrings(count: Int) =
        if (enabled) super.generateStrings(count)
        else generator(count)

    override fun generateUndecoratedStrings(count: Int) =
        generator(count).map { undecorated ->
            descriptor
                .fold(Triple("", false, false)) { (output, isEscaped, hasInserted), char ->
                    when (char) {
                        '\\' -> Triple(output + if (isEscaped) '\\' else "", !isEscaped, hasInserted)
                        '@' -> {
                            if (isEscaped) Triple("$output@", false, hasInserted)
                            else Triple("$output$undecorated", false, true)
                        }
                        else -> Triple("$output$char", false, hasInserted)
                    }
                }
                .let { (output, _, hasInserted) ->
                    if (hasInserted) output
                    else "$output$undecorated$output"
                }
        }


    override fun doValidate(): String? =
        if (!descriptor.fold(false) { escaped, char -> if (char == '\\') !escaped else false }) null
        else Bundle("affix.error.trailing_escape")

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
         * The minimum valid value of the [descriptor] field.
         */
        const val DEFAULT_DESCRIPTOR = ""
    }
}
