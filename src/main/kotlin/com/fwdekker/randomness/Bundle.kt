package com.fwdekker.randomness

import java.util.MissingResourceException
import java.util.ResourceBundle


/**
 * Simple accessor for working with internationalized strings.
 */
object Bundle {
    /**
     * The main bundle for Randomness.
     */
    private val RESOURCE_BUNDLE = ResourceBundle.getBundle("randomness")


    /**
     * Returns the string at [key].
     *
     * @param key the key of the string to return
     * @return the string at [key]
     * @throws MissingResourceException if no string with [key] can be found
     */
    @Throws(MissingResourceException::class)
    operator fun invoke(key: String): String = RESOURCE_BUNDLE.getString(key)

    /**
     * Returns the string at [key] formatted with [arguments].
     *
     * @param key the key of the string to return
     * @param arguments the arguments to insert into the template
     * @return the string at [key] formatted with [arguments]
     * @throws MissingResourceException if no string with [key] can be found
     */
    @Throws(MissingResourceException::class)
    operator fun invoke(key: String, vararg arguments: Any?): String = this(key).format(*arguments)
}


/**
 * Returns `true` if [format] is a format string for `this` string, optionally after inserting [args] into [format].
 *
 * @throws java.util.MissingFormatArgumentException if [args] has fewer arguments than required for [format]
 */
fun String.matchesFormat(format: String, vararg args: String) =
    // TODO: Simplify documentation everywhere throughout the project to remove redundant `@param` specifications
    Regex("%[0-9]+\\\$[Ss]").findAll(format)
        .toList()
        .reversed()
        .fold(format) { acc, match ->
            if (match.value.drop(1).dropLast(2).toInt() > args.size)
                acc.replaceRange(match.range, ".*")
            else
                acc
        }
        .format(*args)
        .let { Regex(it) }
        .matches(this)
