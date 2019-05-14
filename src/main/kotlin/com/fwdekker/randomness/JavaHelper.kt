package com.fwdekker.randomness


/**
 * Returns the first non-null value in [values], or null if all values are null.
 *
 * @param T the type of elements to check for nullness
 * @param values the values to check for nullness
 * @return the first non-null value in [values], or null if all values are null.
 */
fun <T : Any> firstNonNull(vararg values: T?) = values.firstOrNull { it != null }
