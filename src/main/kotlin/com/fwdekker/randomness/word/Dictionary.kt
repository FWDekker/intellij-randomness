package com.fwdekker.randomness.word

import com.fwdekker.randomness.Cache
import java.io.File
import java.io.IOException


/**
 * Thrown when a [Dictionary] is found to be invalid and cannot be used in the intended way.
 *
 * @constructor
 * Constructs a new exception.
 *
 * @param message the detail message
 * @param cause the cause
 */
class InvalidDictionaryException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)


/**
 * A collection of words that may become inaccessible at any moment in time.
 */
// TODO Add tests for dictionaries
interface Dictionary {
    /**
     * The words in the dictionary.
     */
    @get:Throws(InvalidDictionaryException::class)
    val words: Set<String>


    /**
     * Throws an [InvalidDictionaryException] iff this [Dictionary] is currently invalid.
     */
    @Throws(InvalidDictionaryException::class)
    fun validate()

    /**
     * Returns `true` iff [validate] does not throw an exception.
     *
     * @return `true` iff [validate] does not throw an exception
     */
    fun isValid(): Boolean =
        try {
            validate()
            true
        } catch (e: InvalidDictionaryException) {
            false
        }
}


/**
 * A [Dictionary] of which the underlying file is a resource in the JAR.
 *
 * @property filename the path to the resource file
 */
class BundledDictionary private constructor(val filename: String) : Dictionary {
    companion object {
        const val DEFAULT_DICTIONARY_FILE = "words_alpha.dic"

        val cache = Cache<String, BundledDictionary> { BundledDictionary(it) }
    }


    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String> by lazy {
        validate()

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


    /**
     * Returns a stream to the resource file.
     */
    @Throws(IOException::class)
    private fun getStream() = BundledDictionary::class.java.classLoader.getResourceAsStream(filename)
}


/**
 * A [Dictionary] of which the underlying file is a regular file.
 *
 * @property filename the path to the file
 */
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


    /**
     * Returns `true` iff [runnable] throws an exception of type [E].
     *
     * @param E the exception type to check for
     * @param runnable the function to execute
     */
    private inline fun <reified E : Exception> throws(runnable: () -> Any) =
        try {
            runnable()
            false
        } catch (e: Exception) {
            e is E
        }
}
