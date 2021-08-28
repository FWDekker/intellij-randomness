package com.fwdekker.randomness.word

import com.fwdekker.randomness.Cache
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


/**
 * Thrown when a [Dictionary] is found to be invalid and cannot be used in the intended way.
 *
 * @param message the detail message
 * @param cause the cause
 */
class InvalidDictionaryException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)


/**
 * A collection of words.
 */
interface Dictionary {
    /**
     * The words in the dictionary.
     */
    @get:Throws(InvalidDictionaryException::class)
    val words: Set<String>


    /**
     * Returns a deep copy of this dictionary.
     *
     * @return a deep copy of this dictionary
     */
    fun deepCopy(): Dictionary
}

/**
 * A dictionary of which the underlying file is a resource in the JAR.
 *
 * @property filename The path to the resource file.
 */
data class BundledDictionary(var filename: String = "") : Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String>
        get() = cache.get(filename)


    override fun deepCopy() = copy()


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
    override fun toString() = filename


    /**
     * Holds static elements.
     */
    companion object {
        /**
         * The location of a simple English dictionary.
         */
        const val SIMPLE_DICTIONARY = "english.dic"

        /**
         * The cache of bundled dictionaries, used to improve word generation times.
         */
        private val cache = Cache<String, Set<String>> { filename ->
            try {
                val stream =
                    BundledDictionary::class.java.classLoader.getResource(filename)?.openStream()
                        ?: throw FileNotFoundException("File not found.")

                stream.bufferedReader()
                    .readLines()
                    .filterNot { it.isBlank() }
                    .filterNot { it.startsWith('#') }
                    .toSet()
            } catch (e: IOException) {
                throw InvalidDictionaryException(e.message, e)
            }
        }

        /**
         * Clears the cache of words, forcing bundled dictionaries to re-read their source files.
         */
        fun clearCache() = cache.clear()
    }
}

/**
 * A dictionary of which the underlying file is a regular file.
 *
 * @property filename The path to the file.
 */
data class UserDictionary(var filename: String = "") : Dictionary {
    @get:Throws(InvalidDictionaryException::class)
    override val words: Set<String>
        get() {
            validate() // Re-validate file, even if cached
            return cache.get(filename)
        }

    /**
     * Does nothing if the underlying file is valid, or throws an exception otherwise.
     *
     * @throws InvalidDictionaryException if the underlying file is invalid
     */
    @Throws(InvalidDictionaryException::class)
    @Suppress("ThrowsCount") // Improves error message specificity
    private fun validate() {
        val file = File(filename)
        if (!file.exists()) throw InvalidDictionaryException("File not found.")
        if (!file.canRead()) throw InvalidDictionaryException("File unreadable.")

        try {
            file.inputStream()
        } catch (e: IOException) {
            throw InvalidDictionaryException(e.message, e)
        }
    }

    override fun deepCopy() = copy()


    /**
     * Returns a human-readable string of the dictionary's filename.
     *
     * @return a human-readable string of the dictionary's filename
     */
    override fun toString() = filename

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


    /**
     * Holds static elements.
     */
    companion object {
        /**
         * The cache of bundled dictionaries, used to improve word generation times.
         */
        private val cache = Cache<String, Set<String>> { filename ->
            File(filename)
                .readLines()
                .filterNot { it.isBlank() }
                .filterNot { it.startsWith('#') }
                .toSet()
        }

        /**
         * Clears the cache of words, forcing user dictionaries to re-read their source files.
         */
        fun clearCache() = cache.clear()
    }
}
