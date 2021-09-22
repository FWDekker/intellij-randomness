package com.fwdekker.randomness


/**
 * Basically a reference, but the value is not instantiated until it is first read.
 *
 * @param T the type of value referred to by this box
 * @property generator Returns the instance of [T] that should be returned by [get] if [set] has not been called before.
 * @property value Do not assign this field in the constructor. Placed in constructor to ensure Kotlin includes it in
 * the automatically generated [copy] method.
 */
data class Box<T>(private val generator: () -> T, private var value: T? = null) {
    /**
     * Returns the value set by [set], or returns the value previously returned by [get], or returns a value created by
     * [generator].
     *
     * @return the value set by [set], or returns the value previously returned by [get], or returns a value created by
     * [generator]
     */
    operator fun unaryPlus(): T = value ?: generator().also { value = it }

    /**
     * Replaces the referred-to value with [value] so that the next call to [get] returns [value].
     *
     * @param value the value to write into the box
     */
    operator fun plusAssign(value: T) {
        this.value = value
    }
}
