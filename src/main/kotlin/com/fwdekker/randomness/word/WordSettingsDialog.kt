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

fun <T> addSets(a: Set<T>, b: Set<T>) = a + b
