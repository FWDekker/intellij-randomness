package com.fwdekker.randomness.word

import com.fwdekker.randomness.Cache
import java.io.File
import java.io.IOException


class InvalidDictionaryException(message: String?, cause: Throwable?) : Exception(message, cause)

interface Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    val words: Set<String>

    fun isValid(): Boolean
}

class SimpleDictionary(override val words: Set<String>) : Dictionary {
    override fun isValid() = true
}

class BundledDictionary private constructor(val filename: String) : Dictionary {
    companion object {
        const val DEFAULT_DICTIONARY_FILE = "words_alpha.dic"

        val cache = Cache<String, BundledDictionary> { BundledDictionary(it) }
    }

    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String> by lazy {
        try {
            getStream().bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
                .toSet()
        } catch (e: IOException) {
            throw InvalidDictionaryException("Failed to read bundled dictionary into memory.", e)
        }
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
    private fun getStream() = BundledDictionary::class.java.classLoader.getResourceAsStream(filename)
}

class UserDictionary private constructor(val filename: String) : Dictionary {
    companion object {
        val cache = Cache<String, UserDictionary> { UserDictionary(it) }
    }

    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String> by lazy {
        File(filename).readLines().filter { it.isNotBlank() }.toSet()
    }


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
}
