package com.fwdekker.randomness.word

import java.io.File
import java.io.IOException


class InvalidDictionary2Exception(message: String?, cause: Throwable?) : Exception(message, cause)

interface Dictionary2 {
    @Throws(InvalidDictionary2Exception::class)
    fun getWords(): Set<String>

    fun isValid(): Boolean
}

class Cache<K, V>(val creator: (K) -> V) {
    // TODO This cache only caches the dictionary instance, not the words that are read!
    private val cache = mutableMapOf<K, V>()


    fun get(filename: K, useCache: Boolean = true) =
        if (useCache)
            cache.getOrPut(filename) { creator(filename) }
        else
            creator(filename)
                .also { cache[filename] = it }
}

class SimpleDictionary2(private val _words: Set<String>) : Dictionary2 {
    override fun getWords() = _words

    override fun isValid() = true
}

class BundledDictionary2 private constructor(val filename: String) : Dictionary2 {
    // TODO Convert this to a field and use lateinit. The object gets refreshed when the user presses a button in the
    // settings. This makes the cache redundant.
    @Throws(InvalidDictionary2Exception::class)
    override fun getWords() =
        try {
            getStream().bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
                .toSet()
        } catch (e: IOException) {
            throw InvalidDictionary2Exception("Failed to read bundled dictionary into memory.", e)
        }

    override fun isValid() =
        try {
            getStream()
            true
        } catch (e: IOException) {
            false
        }

    override fun toString() = "[bundled] $filename"


    @Throws(IOException::class)
    private fun getStream() = BundledDictionary2::class.java.classLoader.getResourceAsStream(filename)


    companion object {
        const val DEFAULT_DICTIONARY_FILE = "words_alpha.dic"

        val cache = Cache<String, BundledDictionary2> { BundledDictionary2(it) }
    }
}

class UserDictionary2 private constructor(val filename: String) : Dictionary2 {
    @Throws(InvalidDictionary2Exception::class)
    override fun getWords() =
        File(filename).readLines().filter { it.isNotBlank() }.toSet()

    override fun isValid() =
        File(filename).let { file ->
            when {
                !file.exists() -> false
                !file.canRead() -> false
                throws<IOException> { file.inputStream() } -> false
                else -> true
            }
        }

    override fun toString() = "[user] $filename"


    private inline fun <reified E : Exception> throws(runnable: () -> Any) =
        try {
            runnable()
            false
        } catch (e: Exception) {
            e is E
        }


    companion object {
        val cache = Cache<String, UserDictionary2> { UserDictionary2(it) }
    }
}
