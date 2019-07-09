package com.fwdekker.randomness.word


// TODO Remove these methods once [WordSettingsComponent] is written in Kotlin

/**
 * Returns a [Set] containing all words in `dictionaries`.
 *
 * @param dictionaries the dictionaries to combine
 * @return a [Set] containing all words in `dictionaries`
 */
fun combineDictionaries(dictionaries: Iterable<Dictionary>) =
    dictionaries.filter(Dictionary::isValid)
        .fold(emptySet<String>()) { acc, dictionary -> (acc + dictionary.words).toSet() }

/**
 * Returns the length of the shortest word, or 1 if this set is empty.
 *
 * @return the length of the shortest word, or 1 if this set is empty
 */
fun Set<String>.minLength() = this.map { it.length }.min() ?: 1

/**
 * Returns the length of the longest word, or [Integer.MAX_VALUE] if this set is empty.
 *
 * @return the length of the longest word, or [Integer.MAX_VALUE] if this set is empty
 */
fun Set<String>.maxLength() = this.map { it.length }.max() ?: Integer.MAX_VALUE

/**
 * Combines `a` and `b` into a single [Set].
 *
 * @param T the type of the elements in the sets
 * @param a a set
 * @param b a set
 * @return a [Set] containing all elements from `a` and `b`
 */
fun <T> addSets(a: Set<T>, b: Set<T>) = a + b
