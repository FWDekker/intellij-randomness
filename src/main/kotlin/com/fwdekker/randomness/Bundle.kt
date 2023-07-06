package com.fwdekker.randomness

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
     * Throws an exception if no string with [key] can be found.
     *
     * @param key the key of the string to return
     * @return the string at [key]
     */
    operator fun invoke(key: String): String = RESOURCE_BUNDLE.getString(key)

    /**
     * Returns the string at [key] formatted with [arguments].
     *
     * Throws an exception if no string with [key] can be found.
     *
     * @param key the key of the string to return
     * @param arguments the arguments to insert into the template
     * @return the string at [key] formatted with [arguments]
     */
    operator fun invoke(key: String, vararg arguments: Any?): String = this(key).format(*arguments)
}
