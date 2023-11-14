package com.fwdekker.randomness


/**
 * A lazily-instantiated reference to an object of type [T].
 *
 * @param T the type of the referenced object
 * @param generator generates the referenced object when [unaryPlus] is invoked for the first time
 * @param value do not assign this field in the constructor; this field is placed in the constructor to ensure Kotlin
 * includes it in the automatically-generated [copy] method
 */
data class Box<T : Any>(private val generator: () -> T, private var value: T? = null) {
    /**
     * If this method is invoked for the first time, [generator] is invoked and the result is returned. In subsequent
     * invocations of this method, the previously-generated value is returned each time.
     *
     * @return the referenced value
     */
    operator fun unaryPlus(): T = value ?: generator().also { value = it }
}
