package com.fwdekker.randomness


/**
 * Removes all elements from this collection and adds all elements from [collection].
 */
fun <E> MutableCollection<E>.setAll(collection: Collection<E>) {
    clear()
    addAll(collection)
}
