package com.fwdekker.randomness


// TODO Inline these methods once UI has been migrated to Kotlin

/**
 * Returns the first non-null value in [values], or null if all values are null.
 *
 * @param T the type of elements to check for nullness
 * @param values the values to check for nullness
 * @return the first non-null value in [values], or null if all values are null.
 */
fun <T : Any> firstNonNull(vararg values: T?) = values.firstOrNull { it != null }

/**
 * Returns `true` iff `this` and [that] have the same elements in the same order.
 *
 * @param T the type of elements in the collections
 * @param that a collection that may have the same elements in the same order as `this`
 * @return `true` iff `this` and [that] have the same elements in the same order
 */
fun <T : Any> Collection<T>.orderedEquals(that: Collection<T>) =
    this.size == that.size && this.zip(that).all { it.first == it.second }
