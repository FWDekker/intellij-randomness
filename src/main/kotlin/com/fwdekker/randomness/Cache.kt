package com.fwdekker.randomness


/**
 * A simple thread-safe cache of objects.
 *
 * @param K the type of keys
 * @param V the type of values
 * @property creator a function that maps a key to a value, used to instantiate a value when it is requested and not in
 * the cache
 */
class Cache<K, V>(private val creator: (K) -> V) {
    private val cache = mutableMapOf<K, V>()


    /**
     * Returns the value that corresponds to `key`.
     *
     * If it does not exist, it is instantiated and then returned. If `useCache` is set to false, a new value is always
     * instantiated, even if there already is a value for `key`.
     *
     * @param key the key to look up the value with
     * @param useCache whether to return the existing value if it exists
     * @return the value that corresponds to `key`
     */
    @Synchronized
    fun get(key: K, useCache: Boolean = true) =
        if (useCache)
            cache.getOrPut(key) { creator(key) }
        else
            creator(key)
                .also { cache[key] = it }

    /**
     * Removes all keys and values from the cache.
     */
    @Synchronized
    fun clear() = cache.clear()
}
