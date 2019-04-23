package com.fwdekker.randomness.word

import com.fwdekker.randomness.Cache
import java.io.File
import java.io.IOException


class InvalidDictionaryException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)


// TODO Add tests for dictionaries
interface Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    val words: Set<String>


    @Throws(InvalidDictionaryException::class)
    fun validate()

    fun isValid(): Boolean =
        try {
            validate()
            true
        } catch (e: InvalidDictionaryException) {
            false
        }
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


    @Throws(InvalidDictionaryException::class)
    override fun validate() {
        try {
            getStream()
        } catch (e: IOException) {
            throw InvalidDictionaryException("Could not read dictionary file.", e)
        }
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
        validate()
        File(filename).readLines().filter { it.isNotBlank() }.toSet()
    }


    @Throws(InvalidDictionaryException::class)
    override fun validate() =
        File(filename).let { file ->
            when {
                !file.exists() ->
                    throw InvalidDictionaryException("Dictionary file does not exist.")
                !file.canRead() ->
                    throw InvalidDictionaryException("Dictionary file could not be read.")
                throws<IOException> { file.inputStream() } ->
                    throw InvalidDictionaryException("Dictionary file could not be opened.")
                else ->
                    Unit
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
