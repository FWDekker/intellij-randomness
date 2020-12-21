package com.fwdekker.randomness.word

import com.fwdekker.randomness.Cache
import java.io.File
import java.io.IOException


/**
 * Thrown when a [Dictionary] is found to be invalid and cannot be used in the intended way.
 *
 * @param message the detail message
 * @param cause the cause
 */
class InvalidDictionaryException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)


/**
 * A collection of words that may become inaccessible at any moment in time.
 */
interface Dictionary {
    /**
     * The words in the dictionary.
     */
    @get:Throws(InvalidDictionaryException::class)
    val words: Set<String>


    /**
     * Throws an [InvalidDictionaryException] iff this dictionary is currently invalid.
     *
     * @throws InvalidDictionaryException if this dictionary is currently invalid
     */
    @Throws(InvalidDictionaryException::class)
    fun validate()

    /**
     * Returns `true` iff [validate] does not throw an exception.
     *
     * @return `true` iff [validate] does not throw an exception
     */
    @Suppress("SwallowedException") // That's exactly how this function should work
    fun isValid(): Boolean =
        try {
            validate()
            true
        } catch (e: InvalidDictionaryException) {
            false
        }
}


/**
 * A `Dictionary` of which the underlying file is a resource in the JAR.
 *
 * @property filename the path to the resource file
 */
class BundledDictionary private constructor(val filename: String) : Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String> by lazy {
        validate()

        try {
            getStream().bufferedReader()
                .readLines()
                .filterNot { it.isBlank() }
                .filterNot { it.startsWith('#') }
                .toSet()
        } catch (e: IOException) {
            throw InvalidDictionaryException("Failed to read bundled dictionary into memory.", e)
        }
    }


    @Throws(InvalidDictionaryException::class)
    override fun validate() {
        getStream() ?: throw InvalidDictionaryException("Failed to read bundled dictionary into memory.")
    }

    /**
     * Returns `true` iff this dictionary's filename equals [other]'s filename.
     *
     * @param other an object
     * @return `true` iff this dictionary's filename equals [other]'s filename
     */
    override fun equals(other: Any?) = other is BundledDictionary && this.filename == other.filename

    /**
     * Returns the hash code of the filename.
     *
     * @return the hash code of the filename
     */
    override fun hashCode() = filename.hashCode()

    /**
     * Returns a human-readable string of the dictionary's filename.
     *
     * @return a human-readable string of the dictionary's filename
     */
    override fun toString() = "[bundled] $filename"


    /**
     * Returns a stream to the resource file.
     */
    private fun getStream() = BundledDictionary::class.java.classLoader.getResourceAsStream(filename)


    companion object {
        /**
         * The location of a simple English dictionary.
         */
        const val SIMPLE_DICTIONARY = "english_simple.dic"

        /**
         * The cache of bundled dictionaries, used to improve word generation times.
         */
        val cache = Cache<String, BundledDictionary> { BundledDictionary(it) }
    }
}


/**
 * A `Dictionary` of which the underlying file is a regular file.
 *
 * @property filename the path to the file
 */
class UserDictionary private constructor(val filename: String) : Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String> by lazy {
        validate()
        File(filename).readLines()
            .filterNot { it.isBlank() }
            .filterNot { it.startsWith('#') }
            .toSet()
    }


    @Throws(InvalidDictionaryException::class)
    override fun validate() {
        try {
            File(filename).inputStream()
        } catch (e: IOException) {
            throw InvalidDictionaryException("Failed to read user dictionary into memory.", e)
        }
    }

    /**
     * Returns a human-readable string of the dictionary's filename.
     *
     * @return a human-readable string of the dictionary's filename
     */
    override fun toString() = "[user] $filename"

    /**
     * Returns `true` iff this dictionary's filename equals [other]'s filename.
     *
     * @param other an object
     * @return `true` iff this dictionary's filename equals [other]'s filename
     */
    override fun equals(other: Any?) = other is UserDictionary && this.filename == other.filename

    /**
     * Returns the hash code of the filename.
     *
     * @return the hash code of the filename
     */
    override fun hashCode() = filename.hashCode()


    companion object {
        /**
         * The cache of bundled dictionaries, used to improve word generation times.
         */
        val cache = Cache<String, UserDictionary> { UserDictionary(it) }
    }
}
