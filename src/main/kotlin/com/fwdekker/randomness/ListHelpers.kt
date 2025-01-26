package com.fwdekker.randomness


/**
 * Returns the [index]th element of [this], wrapping around circularly so that index `-1` is equivalent to
 * `this.size - 1`.
 */
fun <E> List<E>.getMod(index: Int): E = this[(index % size + size) % size]

/**
 * Removes all elements from this collection and adds all elements from [collection].
 */
fun <E> MutableCollection<E>.setAll(collection: Collection<E>) {
    clear()
    addAll(collection)
}
