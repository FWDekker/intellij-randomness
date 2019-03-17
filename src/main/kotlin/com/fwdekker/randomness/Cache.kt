package com.fwdekker.randomness


class Cache<K, V>(val creator: (K) -> V) {
    private val cache = mutableMapOf<K, V>()


    fun get(filename: K, useCache: Boolean = true) =
        if (useCache)
            cache.getOrPut(filename) { creator(filename) }
        else
            creator(filename)
                .also { cache[filename] = it }

    fun clear() = cache.clear()
}
