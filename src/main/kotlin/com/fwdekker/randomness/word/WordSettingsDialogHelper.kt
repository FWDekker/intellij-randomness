package com.fwdekker.randomness.word


/**
 * Returns a [Set] containing all words in [dictionaries].
 *
 * @param dictionaries the dictionaries to combine
 * @return a [Set] containing all words in [dictionaries]
 */
fun combineDictionaries(dictionaries: Iterable<Dictionary>) =
    dictionaries.filter(Dictionary::isValid)
        .fold(emptySet<String>()) { acc, dictionary -> (acc + dictionary.words).toSet() }

/**
 * Combines [a] and [b] into a single [Set].
 *
 * @param T the type of the elements in the sets
 * @param a a set
 * @param b a set
 * @return a [Set] containing all elements from [a] and [b]
 */
fun <T> addSets(a: Set<T>, b: Set<T>) = a + b
