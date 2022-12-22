package com.fwdekker.randomness


/**
 * A description of brackets surrounding a string.
 *
 * @property descriptor The description of brackets into which a string can be inserted with [interpolate]. Here, `'\'`
 * is the escape character (which also escapes itself), and each unescaped `'@'` is replaced with the inserted string.
 * If the descriptor does not contain an unescaped `'@'`, then the entire descriptor is placed both in front of and
 * after the inserted string. For example, inserting `"word"` into `"(@)"` gives `"(word)"`, and inserting `"word"` into
 * `"()"` gives `"()word()"`.
 */
data class BracketsDescriptor(val descriptor: String) {
    /**
     * Validates the [descriptor], and indicates whether and why it is invalid.
     *
     * @return `null` if the [descriptor] is valid, or a string explaining why the [descriptor] is invalid
     */
    fun doValidate(): String? =
        if (!descriptor.fold(false) { escaped, char -> if (char == '\\') !escaped else false }) null
        else Bundle("brackets.error.trailing_escape")

    /**
     * Replaces each unescaped `'@'` in [descriptor] with [insert], and interprets escaped characters.
     *
     * @param insert the string to insert into [descriptor]
     * @return the [descriptor] with [insert] inserted and escape characters interpreted
     * @throws DataGenerationException if the [descriptor] is invalid according to [doValidate]
     * @see descriptor
     */
    fun interpolate(insert: String): String {
        doValidate()?.also { throw DataGenerationException(it) }

        return descriptor
            .fold(Triple("", false, false)) { (builtString, escaped, inserted), char ->
                when (char) {
                    '\\' -> Triple(builtString + if (escaped) char else "", !escaped, inserted)
                    '@' -> Triple(builtString + if (escaped) "@" else insert, false, inserted || !escaped)
                    else -> Triple(builtString + char, false, inserted)
                }
            }
            .let { (builtString, _, inserted) ->
                if (inserted) builtString
                else builtString + insert + builtString
            }
    }
}
